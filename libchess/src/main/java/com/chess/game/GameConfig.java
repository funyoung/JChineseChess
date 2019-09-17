package com.chess.game;

/**
 * Created by HZY on 2018/3/8.
 */

public class GameConfig {

    public static final String DAT_ASSETS_PATH = "book.dat";
    public static final int SPLASH_DELAY_MILLISECONDS = 500;
    protected static final int MAX_HISTORY_SIZE = 512;
    public static final String PREF_LAST_FEN = "PREF_LAST_FEN";

    protected static final int RESP_CLICK = 0;
    protected static final int RESP_ILLEGAL = 1;
    protected static final int RESP_MOVE = 2;
    protected static final int RESP_MOVE2 = 3;
    protected static final int RESP_CAPTURE = 4;
    protected static final int RESP_CAPTURE2 = 5;
    protected static final int RESP_CHECK = 6;
    protected static final int RESP_CHECK2 = 7;
    protected static final int RESP_WIN = 8;
    protected static final int RESP_DRAW = 9;
    protected static final int RESP_LOSS = 10;

    protected static final int PIECE_THEME_UNKNOWN = -1;
    protected static final int PIECE_THEME_CARTOON = 0;
    protected static final int PIECE_THEME_WOOD = 1;
}
