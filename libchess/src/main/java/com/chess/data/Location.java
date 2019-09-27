package com.chess.data;

public class Location {
    protected int x;
    protected int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean nextRow(int left, int bottom) {
        x = left;
        y ++;
        if (y > bottom) {
            return false;
        }

        return true;
    }

    public void xShift(char c) {
        x += (c - '0');
    }

    public void nextCol() {
        x++;
    }
}
