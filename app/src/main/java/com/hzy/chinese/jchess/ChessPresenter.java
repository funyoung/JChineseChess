package com.hzy.chinese.jchess;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;
import com.blankj.utilcode.util.SnackbarUtils;
import com.blankj.utilcode.util.StringUtils;
import com.chess.game.GameConfig;
import com.chess.game.GameLogic;
import com.chess.game.IGameCallback;
import com.chess.game.IMsgProvider;
import com.chess.xqwlight.Position;
import com.hzy.chinese.jchess.view.GameBoardView;

import java.util.LinkedList;

public class ChessPresenter extends GameLogic implements IGameCallback {
    private static final int[] SOUND_RES_ARRAY = {
            R.raw.click, R.raw.illegal, R.raw.move,
            R.raw.move2, R.raw.capture, R.raw.capture2,
            R.raw.check, R.raw.check2, R.raw.win,
            R.raw.draw, R.raw.loss
    };

    private final GameBoardView mGameBoard;
    private final ProgressBar mGameProgress;

    private SoundPool mSoundPool;
    private LinkedList<Integer> mSoundList;
    private final SharedPreferences mPreference;
    private boolean mSoundEnable;
    private int mHandicapIndex;
    private boolean mComputerFlip;
    private int mPieceStyle;
    private int aiLevel;

    private final Context context;

    public ChessPresenter(Context context, GameBoardView mGameBoard, ProgressBar mGameProgress, SharedPreferences mPreference) {
        super(mGameBoard, provider);

        this.context = context;
        this.mGameBoard = mGameBoard;
        this.mGameProgress = mGameProgress;
        this.mPreference = mPreference;

        mGameBoard.attachPresenter(this);

        loadDefaultConfig();
        initSoundPool();
        initGameLogic();
    }

    private String getString(int id) {
        return context.getString(id);
    }

    private void loadDefaultConfig() {
        mSoundEnable = mPreference.getBoolean(getString(R.string.pref_sound_key), true);
        mHandicapIndex = Integer.parseInt(mPreference.getString(getString(R.string.pref_handicap_key), "0"));
        mComputerFlip = mPreference.getBoolean(getString(R.string.pref_who_first_key), false);
        mPieceStyle = Integer.parseInt(mPreference.getString(getString(R.string.pref_piece_style_key), "0"));
        aiLevel = Integer.parseInt(mPreference.getString(getString(R.string.pref_level_key), "0"));
    }

    private void initSoundPool() {
        mSoundList = new LinkedList<>();
        int poolSize = SOUND_RES_ARRAY.length;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder().setMaxStreams(poolSize).build();
        } else {
            mSoundPool = new SoundPool(poolSize, AudioManager.STREAM_MUSIC, 0);
        }
        for (int res : SOUND_RES_ARRAY) {
            mSoundList.add(mSoundPool.load(context, res, 1));
        }
    }

    private void initGameLogic() {
        setCallback(this);
        setLevel(aiLevel);
        setPieceTheme(mPieceStyle);
        // load last saved game
        String lastFen = mPreference.getString(GameConfig.PREF_LAST_FEN, "");
        if (StringUtils.isEmpty(lastFen)) {
            restart(mComputerFlip, mHandicapIndex);
        } else {
            showMessage(getString(R.string.load_last_game_finish));
            restart(mComputerFlip, lastFen);
        }
    }

    private void showMessage(String message) {
        SnackbarUtils.with(mGameBoard).setDuration(SnackbarUtils.LENGTH_LONG)
                .setMessage(message).show();
    }

    @Override
    public void retract() {
        super.retract();
    }

    @Override
    public void restart() {
        super.restart(mComputerFlip, mHandicapIndex);
        showMessage(getString(R.string.new_game_started));
    }

    public void onResume() {
        loadDefaultConfig();
        setLevel(aiLevel);
        setPieceTheme(mPieceStyle);
        mGameBoard.invalidate();
    }

    public void onDestroy() {
        if (mSoundPool != null) {
            mSoundPool.release();
        }
        mPreference.edit().putString(GameConfig.PREF_LAST_FEN, getCurrentFen()).apply();
    }

    @Override
    public void postPlaySound(final int soundIndex) {
        if (mSoundPool != null && mSoundEnable) {
            int soundId = mSoundList.get(soundIndex);
            mSoundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }

    @Override
    public void postShowMessage(final String message) {
        runOnUiThread(() -> showMessage(message));
    }

    @Override
    public void postShowMessage(int messageId) {
        postShowMessage(getString(messageId));
    }

    @Override
    public void postStartThink() {
        runOnUiThread(() -> mGameProgress.setVisibility(View.VISIBLE));
    }

    @Override
    public void postEndThink() {
        runOnUiThread(() -> mGameProgress.setVisibility(View.GONE));
    }

    private void runOnUiThread(Runnable runnable) {
        mGameBoard.post(runnable);
    }

    @Override
    public void setPieceTheme(int theme) {
        super.setPieceTheme(theme);
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
