package com.chess.data;

import com.chess.xqwlight.Position;

public class Square {
    private final byte[] squares = new byte[256];

    public void clear() {
        for (int sq = 0; sq < squares.length; sq ++) {
            clear(sq);
        }
    }

    public void clear(int sq) {
        squares[sq] = 0;
    }

    public void set(int sq, int pc) {
        squares[sq] = (byte) pc;
    }

    public int get(int sq) {
        return squares[sq];
    }

    public boolean isEmpty(int sq) {
        return get(sq) == 0;
    }

    /*
    todo: to decouple the dependency of Position
     */
    public void mirror(Position pos) {
        for (int sq = 0; sq < squares.length; sq ++) {
            int pc = squares[sq];
            if (pc > 0) {
                pos.addPiece(Board.MIRROR_SQUARE(sq), pc);
            }
        }
    }
}
