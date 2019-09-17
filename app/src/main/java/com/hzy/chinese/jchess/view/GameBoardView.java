package com.hzy.chinese.jchess.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.chess.game.IMsgProvider;
import com.hzy.chinese.jchess.R;
import com.chess.game.GameLogic;
import com.chess.game.IGameView;
import com.chess.xqwlight.Position;

/**
 * Created by HZY on 2018/3/8.
 */

public class GameBoardView extends View implements IGameView {
    private static final int[] PIECE_RES_WOOD = {
            R.drawable.rk, R.drawable.ra, R.drawable.rb,
            R.drawable.rn, R.drawable.rr, R.drawable.rc,
            R.drawable.rp, R.drawable.bk, R.drawable.ba,
            R.drawable.bb, R.drawable.bn, R.drawable.br,
            R.drawable.bc, R.drawable.bp, R.drawable.selected
    };

    private static final int[] PIECE_RES_CARTOON = {
            R.drawable.rk2, R.drawable.ra2, R.drawable.rb2,
            R.drawable.rn2, R.drawable.rr2, R.drawable.rc2,
            R.drawable.rp2, R.drawable.bk2, R.drawable.ba2,
            R.drawable.bb2, R.drawable.bn2, R.drawable.br2,
            R.drawable.bc2, R.drawable.bp2, R.drawable.selected2
    };

    private Bitmap[] mPiecesBitmap;
    private Canvas mCanvas;
    private RectF mPieceDstRectF;

    private final GameLogic mGameLogic;

    public GameBoardView(Context context) {
        this(context, null);
    }

    public GameBoardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGameLogic = new GameLogic(this, provider);

        mPieceDstRectF = new RectF();
        setBackgroundResource(R.drawable.board);

        int theme = loadThemeFromAttributeSet(context, attrs);
        setPieceTheme(theme);
    }

    private int loadThemeFromAttributeSet(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GameBoardView);
        TypedValue outValue = new TypedValue();
        ta.getValue(R.styleable.GameBoardView_pieceTheme, outValue);
        int theme = outValue.data;
        ta.recycle();
        return theme;
    }

    public GameLogic getGameLogic() {
        return mGameLogic;
    }

    public void setPieceTheme(int theme) {
        mGameLogic.setPieceTheme(theme);
    }

    @Override
    public void onThemeChanged(boolean woodTheme) {
        int[] pieceResArray = woodTheme ? PIECE_RES_WOOD : PIECE_RES_CARTOON;

        mPiecesBitmap = new Bitmap[pieceResArray.length];
        for (int i = 0; i < pieceResArray.length; i++) {
            if (mPiecesBitmap[i] != null && !mPiecesBitmap[i].isRecycled()) {
                mPiecesBitmap[i].recycle();
            }
            mPiecesBitmap[i] = BitmapFactory.decodeResource(getResources(), pieceResArray[i]);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        mGameLogic.onMeasure(widthSize, heightSize);
    }

    @Override
    public void onViewMeasured(int w, int h) {
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mGameLogic.onSizeChanged(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.mCanvas = canvas;
        mGameLogic.drawGameBoard();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mGameLogic.clickSquare(event.getX(), event.getY());
        }

        return true;
    }

    @Override
    public void postRepaint() {
        postInvalidate();
    }

    @Override
    public void drawPiece(int pc, float left, float top, float right, float bottom) {
        if (mCanvas != null) {
            mPieceDstRectF.set(left, top, right, bottom);
            mCanvas.drawBitmap(mPiecesBitmap[pc], null, mPieceDstRectF, null);
        }
    }

    private static final IMsgProvider provider = new IMsgProvider() {
        @Override
        public int getFinalMessage(boolean win) {
            return win ? R.string.congratulations_you_win : R.string.you_lose_and_try_again;
        }

        @Override
        public int getLongTimeMessage(int vlRep) {
            return vlRep > Position.WIN_VALUE ?
                    R.string.play_too_long_as_lose : vlRep < -Position.WIN_VALUE ?
                    R.string.pc_play_too_long_as_lose : R.string.standoff_as_draw;
        }

        @Override
        public int getDrawMessage() {
            return R.string.both_too_long_as_draw;
        }

        @Override
        public int getLastHistoryMessage() {
            return R.string.no_more_histories;
        }
    };
}
