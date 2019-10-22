package com.chess.game;

import com.chess.data.Player;
import com.chess.data.StartUpFen;
import com.chess.data.Board;
import com.chess.xqwlight.Position;
import com.chess.xqwlight.Search;
import java.util.ArrayDeque;
import java.util.Deque;

public class GameLogic implements IResponse {
    private final IMsgProvider msgProvider;
    private final IGameView mGameView;

    private String currentFen;
    private int sqSelected, mvLast;
    private volatile boolean thinking = false;
    private boolean flipped = false;
    private int level = 0;

    private Player player = new Player();
    private Position pos = new Position(player);
    private Search search = new Search(pos, player, 16);
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
        for (int x = Board.left(); x <= Board.right(); x++) {
            for (int y = Board.top(); y <= Board.bottom(); y++) {
                int sq = Board.xy(x, y);
                sq = (flipped ? Position.SQUARE_FLIP(sq) : sq);
                int xx = Board.x(x);
                int yy = Board.y(y);
                int pc = player.getPc(sq);
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
                if (sq == sqSelected || sq == Board.SRC(mvLast) ||
                        sq == Board.DST(mvLast)) {
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
            currentFen = StartUpFen.get(handicap);
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
        if (player.isComputerFirst(flipped)) {
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
        int sq_ = Board.xyOffset(xx, yy);
        int sq = (flipped ? Position.SQUARE_FLIP(sq_) : sq_);
        if (player.isComputerSide(sq)) {
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
            int mv = Board.MOVE(sqSelected, sq);
            if (!player.legalMove(mv)) {
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
//        int sq = (flipped ? Position.SQUARE_FLIP(sq_) : sq_);
//        int x = Position.FILE_X(sq) - Position.FILE_LEFT;
//        int y = Position.RANK_Y(sq) - Position.RANK_TOP;
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
            @Override
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
        if (pos.isMoveLimit()) {
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
