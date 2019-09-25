package com.chess.data;

public abstract class AbstractArea {
    public final boolean contains(int sq) {
        return getArea()[sq] != 0;
    }

    abstract protected byte[] getArea();
}
