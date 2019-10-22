package com.chess.game;

/**
 * Created by HZY on 2018/3/8.
 */

public class GameConfig implements IResponse {

    public static final String DAT_ASSETS_PATH = "book.dat";
    public static final int SPLASH_DELAY_MILLISECONDS = 500;
    protected static final int MAX_HISTORY_SIZE = 512;
    public static final String PREF_LAST_FEN = "PREF_LAST_FEN";

    protected static final int PIECE_THEME_UNKNOWN = -1;
    protected static final int PIECE_THEME_CARTOON = 0;
    protected static final int PIECE_THEME_WOOD = 1;
}
