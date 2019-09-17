package com.chess.game;


public interface IGameView {

    void postRepaint();

    void drawPiece(int pc, float left, float top, float right, float bottom);

    void onThemeChanged(boolean woodTheme);

    void onViewMeasured(int w, int h);
}
