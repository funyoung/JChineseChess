package com.chess.game;

public interface IMsgProvider {
    int getFinalMessage(boolean win);

    int getLongTimeMessage(int vlRep);

    int getDrawMessage();

    int getLastHistoryMessage();
}
