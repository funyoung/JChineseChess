package com.chess.data;

public class StartUpFen {
    private static final String[] LABEL = {
            "1 - 无让子",
            "2 - 让左马",
            "3 - 让双马",
            "4 - 让九子"
    };

    private static final String[] STARTUP_FEN = {
            "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1",	//不让子
            "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/R1BAKABNR w - - 0 1",	//让左马
            "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/R1BAKAB1R w - - 0 1",	//让双马
            "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/9/1C5C1/9/RN2K2NR w - - 0 1",	//让九子
    };

    public static String get(int handicap) {
        int index = (handicap >= STARTUP_FEN.length || handicap < 0) ? 0 : handicap;
        return STARTUP_FEN[index];
    }
}
