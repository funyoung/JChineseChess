package com.chess.data;

import com.chess.xqwlight.Position;

/**
 * Abstract Board out from Position
 */
public class Board extends AbstractArea {
    public static final int RANK_TOP = 3;
    public static final int RANK_BOTTOM = 12;
    public static final int FILE_LEFT = 3;
    public static final int FILE_RIGHT = 11;

    private static final byte[] IN_BOARD = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    };

    public static int x(int x) {
        return x - left();
    }

    public static int y(int y) {
        return y - top();
    }

    public static int xy(int x, int y) {
        return x + (y << 4);
    }

    public static int xyOffset(int xx, int yy) {
        return xy(xx + left(), yy + top());
    }

    public static int left() {
        return FILE_LEFT;
    }

    public static int right() {
        return FILE_RIGHT;
    }

    public static int top() {
        return RANK_TOP;
    }

    public static int bottom() {
        return RANK_BOTTOM;
    }

    public static Location getLeftTop() {
        return new Location(left(), top());
    }

    public static boolean nextRow(Location location) {
        return location.nextRow(left(), bottom());
    }

    public static int addPiece(Location location, char c) {
        int pt = -1;
        if (location.x <= right()) {
            pt = Position.CHAR_TO_PIECE(c);
            location.nextCol();
        }

        return pt;
    }

    public static int getSq(Location location) {
        return xy(location.x, location.y);
    }

    @Override
    protected byte[] getArea() {
        return IN_BOARD;
    }


    public static int FILE_FLIP(int x) {
        return 14 - x;
    }

    public static int MIRROR_MOVE(int mv) {
        return MOVE(MIRROR_SQUARE(SRC(mv)), MIRROR_SQUARE(DST(mv)));
    }

    public static int SRC(int mv) {
        return mv & 255;
    }

    public static int DST(int mv) {
        return mv >> 8;
    }

    public static int MOVE(int sqSrc, int sqDst) {
        return sqSrc + (sqDst << 8);
    }

    public static int RANK_FLIP(int y) {
        return 15 - y;
    }

    public static int MIRROR_SQUARE(int sq) {
        return Board.xy(FILE_FLIP(FILE_X(sq)), RANK_Y(sq));
    }

    private static int RANK_Y(int sq) {
        return sq >> 4;
    }

    private static int FILE_X(int sq) {
        return sq & 15;
    }

}
