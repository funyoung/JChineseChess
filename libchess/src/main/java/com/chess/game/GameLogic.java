package com.chess.game;

import com.chess.xqwlight.Position;
import com.chess.xqwlight.Search;
import java.util.ArrayDeque;
import java.util.Deque;

import static com.chess.game.GameConfig.RESP_CAPTURE;
import static com.chess.game.GameConfig.RESP_CAPTURE2;
import static com.chess.game.GameConfig.RESP_CHECK;
import static com.chess.game.GameConfig.RESP_CHECK2;
import static com.chess.game.GameConfig.RESP_CLICK;
import static com.chess.game.GameConfig.RESP_DRAW;
import static com.chess.game.GameConfig.RESP_ILLEGAL;
import static com.chess.game.GameConfig.RESP_LOSS;
import static com.chess.game.GameConfig.RESP_MOVE;
import static com.chess.game.GameConfig.RESP_MOVE2;
import static com.chess.game.GameConfig.RESP_WIN;

public class GameLogic {
    private final IMsgProvider msgProvider;
    private final IGameView mGameView;

    private String currentFen;
    private int sqSelected, mvLast;
    private volatile boolean thinking = false;
    private boolean flipped = false;
    private int level = 0;
    private Position pos = new Position();
    private Search search = new Search(pos, 16);
    private Deque<String> mHistoryList = new ArrayDeque<>();
    private IGameCallback mGameCallback;
    private volatile boolean mDrawBoardFinish;

    public GameLogic(IGameView gameView, IMsgProvider provider) {
        this(gameView, provider, null);
    }

    public GameLogic(IGameView gameView, IMsgProvider provider, IGameCallback callback) {
        mGameView = gameView;
        msgProvider = provider;
        mGameCallback = callback;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCallback(IGameCallback callback) {
        this.mGameCallback = callback;
    }

    public void drawGameBoard() {
        for (int x = Position.FILE_LEFT; x <= Position.FILE_RIGHT; x++) {
            for (int y = Position.RANK_TOP; y <= Position.RANK_BOTTOM; y++) {
                int sq = Position.COORD_XY(x, y);
                sq = (flipped ? Position.SQUARE_FLIP(sq) : sq);
                int xx = x - Position.FILE_LEFT;
                int yy = y - Position.RANK_TOP;
                int pc = pos.squares[sq];
                if (pc > 0) {

                    float left = xx * mCellWidth;
                    float top = yy * mCellWidth;
                    float right = left + mCellWidth;
                    float bottom = top + mCellWidth;
                    pc -= 8;
                    if (pc > PIECE_BITMAP_START_INDEX) {
                        pc--;
                    }

                    mGameView.drawPiece(pc, left, top, right, bottom);
                }
                if (sq == sqSelected || sq == Position.SRC(mvLast) ||
                        sq == Position.DST(mvLast)) {
                    float left = xx * mCellWidth;
                    float top = yy * mCellWidth;
                    float right = left + mCellWidth;
                    float bottom = top + mCellWidth;
                    mGameView.drawPiece(PIECE_BITMAP_SELECTED_INDEX, left, top, right, bottom);
                }
            }
        }
        mDrawBoardFinish = true;
    }

    public String getCurrentFen() {
        return currentFen;
    }

    public void restart() {
        restart(false, 0);
    }

    public void restart(boolean flipped, String newFen) {
        if (!thinking) {
            this.flipped = flipped;
            currentFen = newFen;
            mHistoryList.clear();
            startPlay();
        }
    }

    public void restart(boolean flipped, int handicap) {
        if (!thinking) {
            this.flipped = flipped;
            int index = (handicap >= Position.STARTUP_FEN.length || handicap < 0) ? 0 : handicap;
            currentFen = Position.STARTUP_FEN[index];
            mHistoryList.clear();
            startPlay();
        }
    }

    public void retract() {
        if (!thinking) {
            String fen = popHistory();
            if (fen != null) {
                currentFen = fen;
                startPlay();
            }
        }
    }

    private void startPlay() {
        pos.fromFen(currentFen);
        sqSelected = mvLast = 0;
        if (flipped && pos.sdPlayer == 0) {
            thinking();
        } else {
            mGameView.postRepaint();
        }
    }

    /**
     * Do not call this function in main thread
     * it will block the process util UI updated
     */
    private void blockRepaint() {
        mDrawBoardFinish = false;
        mGameView.postRepaint();
        while (!mDrawBoardFinish) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickSquare(float x, float y) {
        if (thinking) {
            return;
        }

        int xx = (int) (x / mCellWidth);
        int yy = (int) (y / mCellWidth);
        int sq_ = Position.COORD_XY(xx + Position.FILE_LEFT, yy + Position.RANK_TOP);
        int sq = (flipped ? Position.SQUARE_FLIP(sq_) : sq_);
        int pc = pos.squares[sq];
        if ((pc & Position.SIDE_TAG(pos.sdPlayer)) != 0) {
            if (sqSelected > 0) {
                drawSquare(sqSelected);
            }
            if (mvLast > 0) {
                drawMove(mvLast);
                mvLast = 0;
            }
            sqSelected = sq;
            drawSquare(sq);
            playSound(RESP_CLICK);
            mGameView.postRepaint();
        } else if (sqSelected > 0) {
            int mv = Position.MOVE(sqSelected, sq);
            if (!pos.legalMove(mv)) {
                return;
            }
            if (!pos.makeMove(mv)) {
                playSound(RESP_ILLEGAL);
                return;
            }
            int response = pos.inCheck() ? RESP_CHECK :
                    pos.captured() ? RESP_CAPTURE : RESP_MOVE;
            if (pos.captured()) {
                pos.setIrrev();
            }
            mvLast = mv;
            sqSelected = 0;
            drawMove(mv);
            playSound(response);
            if (!getResult()) {
                thinking();
            } else {
                mGameView.postRepaint();
            }
        }
    }

    private void drawSquare(int sq_) {
        int sq = (flipped ? Position.SQUARE_FLIP(sq_) : sq_);
        int x = Position.FILE_X(sq) - Position.FILE_LEFT;
        int y = Position.RANK_Y(sq) - Position.RANK_TOP;
        //canvas.postRepaint(x, y, SQUARE_SIZE, SQUARE_SIZE);
    }

    private void drawMove(int mv) {
        //drawSquare(Position.SRC(mv));
        //drawSquare(Position.DST(mv));
    }

    private void playSound(int response) {
        if (mGameCallback != null) {
            mGameCallback.postPlaySound(response);
        }
    }

    private void showMessage(String message) {
        if (mGameCallback != null) {
            mGameCallback.postShowMessage(message);
        }
    }

    private void showMessage(int stringResId) {
        if (mGameCallback != null) {
            mGameCallback.postShowMessage(stringResId);
        }
    }

    private void thinking() {
        thinking = true;
        new Thread() {
            public void run() {
                mGameCallback.postStartThink();
                int mv = mvLast;
                search.prepareSearch();
                blockRepaint();
                mvLast = search.searchMain(100 << level);
                pos.makeMove(mvLast);
                drawMove(mv);
                drawMove(mvLast);
                int response = pos.inCheck() ? RESP_CHECK2 :
                        pos.captured() ? RESP_CAPTURE2 : RESP_MOVE2;
                if (pos.captured()) {
                    pos.setIrrev();
                }
                getResult(response);
                thinking = false;
                mGameView.postRepaint();
                mGameCallback.postEndThink();
            }
        }.start();
    }

    private boolean getResult() {
        return getResult(-1);
    }

    private boolean getResult(int response) {
        if (pos.isMate()) {
            playSound(response < 0 ? RESP_WIN : RESP_LOSS);
            showMessage(msgProvider.getFinalMessage(response < MAX_WIN_RESPONSE_VALUE));
            return true;
        }
        int vlRep = pos.repStatus(3);
        if (vlRep > 0) {
            vlRep = (response < 0 ? pos.repValue(vlRep) : -pos.repValue(vlRep));
            playSound(vlRep > Position.WIN_VALUE ? RESP_LOSS :
                    vlRep < -Position.WIN_VALUE ? RESP_WIN : RESP_DRAW);
            showMessage(msgProvider.getLongTimeMessage(vlRep));
            return true;
        }
        if (pos.moveNum > 100) {
            playSound(RESP_DRAW);
            showMessage(msgProvider.getDrawMessage());
            return true;
        }
        if (response >= 0) {
            playSound(response);
            pushHistory(currentFen);
            currentFen = pos.toFen();
        }
        return false;
    }

    private void pushHistory(String fen) {
        if (mHistoryList.size() >= GameConfig.MAX_HISTORY_SIZE) {
            mHistoryList.poll();
        }
        mHistoryList.offer(fen);
    }

    private String popHistory() {
        if (mHistoryList.size() == 0) {
            showMessage(msgProvider.getLastHistoryMessage());
            playSound(RESP_ILLEGAL);
            return null;
        }
        playSound(RESP_MOVE2);
        return mHistoryList.pollLast();
    }

    private int mPieceTheme = GameConfig.PIECE_THEME_UNKNOWN;
    public void setPieceTheme(int theme) {
        if (theme == mPieceTheme) {
            return;
        }
        mPieceTheme = theme;

        mGameView.onThemeChanged(mPieceTheme == GameConfig.PIECE_THEME_WOOD);
    }

    private static final int WIDTH_CELL_COUNT = 9;
    private static final int HEIGHT_CELL_COUNT = 10;
    public void onMeasure(int widthSize, int heightSize) {
        float widthCell = widthSize * 1.0f / WIDTH_CELL_COUNT;
        float heightCell = heightSize * 1.0f / HEIGHT_CELL_COUNT;
        float cellWidth;
        if (widthCell < 0.1f || heightCell < 0.1f) {
            cellWidth = Math.max(widthCell, heightCell);
        } else {
            cellWidth = Math.min(widthCell, heightCell);
        }

        mGameView.onViewMeasured((int) (cellWidth * WIDTH_CELL_COUNT),
                (int) (cellWidth * HEIGHT_CELL_COUNT));
    }

    private float mCellWidth;
    public void onSizeChanged(int w, int h) {
        mCellWidth = Math.min(w, h) * 1.0f / WIDTH_CELL_COUNT;
    }

    private static final int PIECE_BITMAP_SELECTED_INDEX = 14;
    private static final int PIECE_BITMAP_START_INDEX = 6;
    private static final int MAX_WIN_RESPONSE_VALUE = 0;
}
