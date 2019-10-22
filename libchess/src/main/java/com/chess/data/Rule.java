package com.chess.data;

public class Rule implements IMove {
    private int[] mvList = new int[MAX_MOVE_NUM];
    private int[] pcList = new int[MAX_MOVE_NUM];
    private boolean[] chkList = new boolean[MAX_MOVE_NUM];

    public void set(int moveNum, boolean checked) {
        mvList[moveNum] = pcList[moveNum] = 0;
        chkList[moveNum] = checked;
    }

    public int getMove(int index) {
        return mvList[index];
    }

    public void setMove(int index, int piece) {
        mvList[index] = piece;
    }

    public void setPc(int index, int piece) {
        pcList[index] = piece;
    }

    public int queryPc(int index) {
        return pcList[index];
    }

    public boolean checkPc(int index) {
        return queryPc(index) > 0;
    }

    public boolean checkMoving(int index) {
        return getMove(index) > 0 && queryPc(index) == 0;
    }

    public void setCheck(int index, boolean checked) {
        chkList[index] = checked;
    }

    public boolean isCheck(int index) {
        return chkList[index];
    }

    public boolean isCheck(int index, boolean checked) {
        return checked && isCheck(index);
    }
}
