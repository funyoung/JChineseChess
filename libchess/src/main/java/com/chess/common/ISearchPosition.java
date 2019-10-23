package com.chess.common;

public interface ISearchPosition {
    int MATE_VALUE = 10000;
    int BAN_VALUE = MATE_VALUE - 100;
    int WIN_VALUE = MATE_VALUE - 200;
}
