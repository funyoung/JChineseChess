package com.chess.data;

public interface ISearch {
    int MATE_VALUE = 10000;
    int BAN_VALUE = MATE_VALUE - 100;
    int WIN_VALUE = MATE_VALUE - 200;

    int MAX_GEN_MOVES = 128;
}
