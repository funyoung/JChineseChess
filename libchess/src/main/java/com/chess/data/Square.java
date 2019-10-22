package com.chess.data;

public class Square {
    private final byte[] squares = new byte[256];

    public void clear() {
        for (int sq = 0; sq < length(); sq ++) {
            clear(sq);
        }
    }

    public void clear(int sq) {
        set(sq, 0);
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

    public int length() {
        return squares.length;
    }
}
