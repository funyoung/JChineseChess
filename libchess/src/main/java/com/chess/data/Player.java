package com.chess.data;

public class Player {
    private int sdPlayer;

    private static boolean HOME_HALF(int sq, int sd) {
        return (sq & 0x80) != (sd << 7);
    }

    public static int SQUARE_FORWARD(int sq, int sd) {
        return sq - 16 + (sd << 5);
    }

    public static boolean AWAY_HALF(int sq, int sd) {
        return (sq & 0x80) == (sd << 7);
    }

    public static int SIDE_TAG(int sd) {
        return 8 + (sd << 3);
    }

    public static int OPP_SIDE_TAG(int sd) {
        return 16 - (sd << 3);
    }

    public boolean isHuman() {
        return sdPlayer == 0;
    }

    public boolean isComputer() {
        return sdPlayer == 1;
    }

    public void changeSide() {
        sdPlayer = 1 - sdPlayer;
    }

    public void clear() {
        sdPlayer = 0;
    }

    public boolean isOpposite(char ch) {
        return sdPlayer == (ch == 'b' ? 0 : 1);
    }

    public char getChar() {
        return sdPlayer == 0 ? 'w' : 'b';
    }

    public boolean isHomeHalf(int sq) {
        return HOME_HALF(sq, sdPlayer);
    }

    public boolean isComputerFirst(boolean flipped) {
        return flipped && isHuman();
    }

    public boolean isComputerSide(int pc) {
        return (pc & sideTag()) != 0;
    }

    public int sideTag() {
        return SIDE_TAG(sdPlayer);
    }

    public int oppSideTag() {
        return OPP_SIDE_TAG(sdPlayer);
    }

    public int squareForward(int sqSrc) {
        return SQUARE_FORWARD(sqSrc, sdPlayer);
    }

    public boolean awayHalf(int sqSrc) {
        return AWAY_HALF(sqSrc, sdPlayer);
    }
}
