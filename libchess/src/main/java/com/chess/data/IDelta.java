package com.chess.data;

public interface IDelta {
    int[] KING_DELTA = {-16, -1, 1, 16};
    int[] ADVISOR_DELTA = {-17, -15, 15, 17};
    int[][] KNIGHT_DELTA = {{-33, -31}, {-18, 14}, {-14, 18}, {31, 33}};
    int[][] KNIGHT_CHECK_DELTA = {{-33, -18}, {-31, -14}, {14, 31}, {18, 33}};
}
