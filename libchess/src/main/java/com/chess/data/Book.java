package com.chess.data;

import com.chess.xqwlight.RC4;
import com.chess.xqwlight.Util;

import java.io.InputStream;

public class Book {
    private static final int MAX_BOOK_SIZE = 16384;

    private static int PreGen_zobristKeyPlayer;
    private static int PreGen_zobristLockPlayer;
    private static int[][] PreGen_zobristKeyTable = new int[14][256];
    private static int[][] PreGen_zobristLockTable = new int[14][256];

    private static int bookSize = 0;
    private static int[] bookLock = new int[MAX_BOOK_SIZE];
    private static short[] bookMove = new short[MAX_BOOK_SIZE];
    private static short[] bookValue = new short[MAX_BOOK_SIZE];

    private static volatile boolean bookLoadOk = false;

    public int zobristKey;
    public int zobristLock;

    public static boolean loadBook(InputStream in) {
        if (bookLoadOk) {
            return true;
        }
        RC4 rc4 = new RC4(new byte[]{0});
        PreGen_zobristKeyPlayer = rc4.nextLong();
        rc4.nextLong(); // Skip ZobristLock0
        PreGen_zobristLockPlayer = rc4.nextLong();
        for (int i = 0; i < 14; i++) {
            for (int j = 0; j < 256; j++) {
                PreGen_zobristKeyTable[i][j] = rc4.nextLong();
                rc4.nextLong(); // Skip ZobristLock0
                PreGen_zobristLockTable[i][j] = rc4.nextLong();
            }
        }
        if (in != null) {
            try {
                while (bookSize < MAX_BOOK_SIZE) {
                    bookLock[bookSize] = Util.readInt(in) >>> 1;
                    bookMove[bookSize] = (short) Util.readShort(in);
                    bookValue[bookSize] = (short) Util.readShort(in);
                    bookSize++;
                }
                in.close();
                bookLoadOk = true;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    in.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    public void clear() {
        zobristKey = zobristLock = 0;
    }

    public void addPiece(int pcAdjust, int sq) {
        zobristKey ^= PreGen_zobristKeyTable[pcAdjust][sq];
        zobristLock ^= PreGen_zobristLockTable[pcAdjust][sq];
    }

    public void changeSide() {
        zobristKey ^= PreGen_zobristKeyPlayer;
        zobristLock ^= PreGen_zobristLockPlayer;
    }

    public boolean empty() {
        return bookSize == 0;
    }

    public int getUnSignedLock() {
        return zobristLock >>> 1;
    }

    public int binarySearch(int lock) {
        return Util.binarySearch(lock, bookLock, 0, bookSize);
    }

    public boolean checkUpperLock(int index, int lock) {
        return index < bookSize && bookLock[index] == lock;
    }

    public boolean checkLowerLock(int index, int lock) {
        return index >= 0 && bookLock[index] == lock;
    }

    public int getMove(int index) {
        return 0xffff & bookMove[index];
    }

    public int getValue(int index) {
        return bookValue[index];
    }
}
