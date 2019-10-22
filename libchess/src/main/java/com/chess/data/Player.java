package com.chess.data;

import com.chess.xqwlight.Position;

public class Player implements IPiece, IDelta {
    private int sdPlayer;

    private static boolean HOME_HALF(int sq, int sd) {
        return (sq & 0x80) != (sd << 7);
    }

    public static int SQUARE_FORWARD(int sq, int sd) {
        return sq - 16 + (sd << 5);
    }

    public static boolean AWAY_HALF(int sq, int sd) {
        return (sq & 0x80) == (sd << 7);
    }

    public static int SIDE_TAG(int sd) {
        return 8 + (sd << 3);
    }

    public static int OPP_SIDE_TAG(int sd) {
        return 16 - (sd << 3);
    }

    public boolean isHuman() {
        return sdPlayer == 0;
    }

    public boolean isComputer() {
        return sdPlayer == 1;
    }

    public void changeSide() {
        sdPlayer = 1 - sdPlayer;
    }

    public void clear() {
        square.clear();
        sdPlayer = 0;
    }

    public boolean isOpposite(char ch) {
        return sdPlayer == (ch == 'b' ? 0 : 1);
    }

    public char getChar() {
        return sdPlayer == 0 ? 'w' : 'b';
    }

    public boolean isHomeHalf(int sq) {
        return HOME_HALF(sq, sdPlayer);
    }

    public boolean isComputerFirst(boolean flipped) {
        return flipped && isHuman();
    }

    public boolean isComputerSide(int sq) {
        int pc = getPc(sq);
        return (pc & sideTag()) != 0;
    }

    public int sideTag() {
        return SIDE_TAG(sdPlayer);
    }

    public int oppSideTag() {
        return OPP_SIDE_TAG(sdPlayer);
    }

    public int squareForward(int sqSrc) {
        return SQUARE_FORWARD(sqSrc, sdPlayer);
    }

    public boolean awayHalf(int sqSrc) {
        return AWAY_HALF(sqSrc, sdPlayer);
    }

    private static final int[] MVV_VALUE = {50, 10, 10, 30, 40, 30, 20, 0};

    public static boolean SAME_HALF(int sqSrc, int sqDst) {
        return ((sqSrc ^ sqDst) & 0x80) == 0;
    }

    public static boolean SAME_RANK(int sqSrc, int sqDst) {
        return ((sqSrc ^ sqDst) & 0xf0) == 0;
    }

    public static boolean SAME_FILE(int sqSrc, int sqDst) {
        return ((sqSrc ^ sqDst) & 0x0f) == 0;
    }

    public static int MVV_LVA(int pc, int lva) {
        return MVV_VALUE[pc & 7] - lva;
    }

    private final AbstractArea board = new Board();
    private final Square square = new Square();
    private final AbstractArea fort = new Fort();
    private final LegalSpan legalSpan = new LegalSpan();
    private final Pin pin = new Pin();
    public int generateMoves(int[] mvs, int[] vls) {
        int moves = 0;
        int pcSelfSide = sideTag();
        int pcOppSide = oppSideTag();
        for (int sqSrc = 0; sqSrc < 256; sqSrc ++) {
            int pcSrc = getPc(sqSrc);
            if ((pcSrc & pcSelfSide) == 0) {
                continue;
            }
            switch (pcSrc - pcSelfSide) {
                case PIECE_KING:
                    for (int i = 0; i < 4; i ++) {
                        int sqDst = sqSrc + KING_DELTA[i];
                        if (!fort.contains(sqDst)) {
                            continue;
                        }
                        int pcDst = getPc(sqDst);
                        if (vls == null) {
                            if ((pcDst & pcSelfSide) == 0) {
                                mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                moves ++;
                            }
                        } else if ((pcDst & pcOppSide) != 0) {
                            mvs[moves] = Board.MOVE(sqSrc, sqDst);
                            vls[moves] = MVV_LVA(pcDst, 5);
                            moves ++;
                        }
                    }
                    break;
                case PIECE_ADVISOR:
                    for (int i = 0; i < 4; i ++) {
                        int sqDst = sqSrc + ADVISOR_DELTA[i];
                        if (!fort.contains(sqDst)) {
                            continue;
                        }
                        int pcDst = getPc(sqDst);
                        if (vls == null) {
                            if ((pcDst & pcSelfSide) == 0) {
                                mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                moves ++;
                            }
                        } else if ((pcDst & pcOppSide) != 0) {
                            mvs[moves] = Board.MOVE(sqSrc, sqDst);
                            vls[moves] = MVV_LVA(pcDst, 1);
                            moves ++;
                        }
                    }
                    break;
                case PIECE_BISHOP:
                    for (int i = 0; i < 4; i ++) {
                        int sqDst = sqSrc + ADVISOR_DELTA[i];
                        if (!(board.contains(sqDst) && isHomeHalf(sqDst) && square.isEmpty(sqDst))) {
                            continue;
                        }
                        sqDst += ADVISOR_DELTA[i];
                        int pcDst = getPc(sqDst);
                        if (vls == null) {
                            if ((pcDst & pcSelfSide) == 0) {
                                mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                moves ++;
                            }
                        } else if ((pcDst & pcOppSide) != 0) {
                            mvs[moves] = Board.MOVE(sqSrc, sqDst);
                            vls[moves] = MVV_LVA(pcDst, 1);
                            moves ++;
                        }
                    }
                    break;
                case PIECE_KNIGHT:
                    for (int i = 0; i < 4; i ++) {
                        int sqDst = sqSrc + KING_DELTA[i];
                        if (getPc(sqDst) > 0) {
                            continue;
                        }
                        for (int j = 0; j < 2; j ++) {
                            sqDst = sqSrc + KNIGHT_DELTA[i][j];
                            if (!board.contains(sqDst)) {
                                continue;
                            }
                            int pcDst = getPc(sqDst);
                            if (vls == null) {
                                if ((pcDst & pcSelfSide) == 0) {
                                    mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                    moves ++;
                                }
                            } else if ((pcDst & pcOppSide) != 0) {
                                mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                vls[moves] = MVV_LVA(pcDst, 1);
                                moves ++;
                            }
                        }
                    }
                    break;
                case PIECE_ROOK:
                    for (int i = 0; i < 4; i ++) {
                        int delta = KING_DELTA[i];
                        int sqDst = sqSrc + delta;
                        while (board.contains(sqDst)) {
                            int pcDst = getPc(sqDst);
                            if (pcDst == 0) {
                                if (vls == null) {
                                    mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                    moves ++;
                                }
                            } else {
                                if ((pcDst & pcOppSide) != 0) {
                                    mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                    if (vls != null) {
                                        vls[moves] = MVV_LVA(pcDst, 4);
                                    }
                                    moves ++;
                                }
                                break;
                            }
                            sqDst += delta;
                        }
                    }
                    break;
                case PIECE_CANNON:
                    for (int i = 0; i < 4; i ++) {
                        int delta = KING_DELTA[i];
                        int sqDst = sqSrc + delta;
                        while (board.contains(sqDst)) {
                            if (square.isEmpty(sqDst)) {
                                if (vls == null) {
                                    mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                    moves ++;
                                }
                            } else {
                                break;
                            }
                            sqDst += delta;
                        }
                        sqDst += delta;
                        while (board.contains(sqDst)) {
                            int pcDst = getPc(sqDst);
                            if (pcDst > 0) {
                                if ((pcDst & pcOppSide) != 0) {
                                    mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                    if (vls != null) {
                                        vls[moves] = MVV_LVA(pcDst, 4);
                                    }
                                    moves ++;
                                }
                                break;
                            }
                            sqDst += delta;
                        }
                    }
                    break;
                case PIECE_PAWN:
                    int sqDst = squareForward(sqSrc);
                    if (board.contains(sqDst)) {
                        int pcDst = getPc(sqDst);
                        if (vls == null) {
                            if ((pcDst & pcSelfSide) == 0) {
                                mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                moves ++;
                            }
                        } else if ((pcDst & pcOppSide) != 0) {
                            mvs[moves] = Board.MOVE(sqSrc, sqDst);
                            vls[moves] = MVV_LVA(pcDst, 2);
                            moves ++;
                        }
                    }
                    if (awayHalf(sqSrc)) {
                        for (int delta = -1; delta <= 1; delta += 2) {
                            sqDst = sqSrc + delta;
                            if (board.contains(sqDst)) {
                                int pcDst = getPc(sqDst);
                                if (vls == null) {
                                    if ((pcDst & pcSelfSide) == 0) {
                                        mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                        moves ++;
                                    }
                                } else if ((pcDst & pcOppSide) != 0) {
                                    mvs[moves] = Board.MOVE(sqSrc, sqDst);
                                    vls[moves] = MVV_LVA(pcDst, 2);
                                    moves ++;
                                }
                            }
                        }
                    }
                    break;
            }
        }
        return moves;
    }

    public boolean legalMove(int mv) {
        int sqSrc = Board.SRC(mv);
        int pcSrc = getPc(sqSrc);
        int pcSelfSide = sideTag();
        if ((pcSrc & pcSelfSide) == 0) {
            return false;
        }

        int sqDst = Board.DST(mv);
        int pcDst = getPc(sqDst);
        if ((pcDst & pcSelfSide) != 0) {
            return false;
        }

        switch (pcSrc - pcSelfSide) {
            case PIECE_KING:
                return fort.contains(sqDst) && legalSpan.isKingSpan(sqSrc, sqDst);
            case PIECE_ADVISOR:
                return fort.contains(sqDst) && legalSpan.isAdviserSpan(sqSrc, sqDst);
            case PIECE_BISHOP:
                return SAME_HALF(sqSrc, sqDst) && legalSpan.isBishopSpan(sqSrc, sqDst) &&
                        square.isEmpty(pin.getBishopPin(sqSrc, sqDst));
            case PIECE_KNIGHT:
                int sqPin = pin.getKnightPin(sqSrc, sqDst);
                return sqPin != sqSrc && square.isEmpty(sqPin);
            case PIECE_ROOK:
            case PIECE_CANNON:
                int delta;
                if (SAME_RANK(sqSrc, sqDst)) {
                    delta = (sqDst < sqSrc ? -1 : 1);
                } else if (SAME_FILE(sqSrc, sqDst)) {
                    delta = (sqDst < sqSrc ? -16 : 16);
                } else {
                    return false;
                }
                sqPin = sqSrc + delta;
                while (sqPin != sqDst && square.isEmpty(sqPin)) {
                    sqPin += delta;
                }
                if (sqPin == sqDst) {
                    return pcDst == 0 || pcSrc - pcSelfSide == PIECE_ROOK;
                }
                if (pcDst == 0 || pcSrc - pcSelfSide == PIECE_ROOK) {
                    return false;
                }
                sqPin += delta;
                while (sqPin != sqDst && square.isEmpty(sqPin)) {
                    sqPin += delta;
                }
                return sqPin == sqDst;
            case PIECE_PAWN:
                if (awayHalf(sqDst) && (sqDst == sqSrc - 1 || sqDst == sqSrc + 1)) {
                    return true;
                }
                return sqDst == squareForward(sqSrc);
            default:
                return false;
        }
    }

    public void addPiece(int sq, int pc, boolean del) {
        if (del) {
            square.clear(sq);
        } else {
            square.set(sq, pc);
        }
    }

    public boolean isEmpty(int sq) {
        return square.isEmpty(sq);
    }

    public int getPc(int sq) {
        return square.get(sq);
    }

    public boolean checked() {
        int pcSelfSide = sideTag();
        int pcOppSide = oppSideTag();
        for (int sqSrc = 0; sqSrc < 256; sqSrc ++) {
            if (getPc(sqSrc) != pcSelfSide + PIECE_KING) {
                continue;
            }
            if (getPc(squareForward(sqSrc)) == pcOppSide + PIECE_PAWN) {
                return true;
            }
            for (int delta = -1; delta <= 1; delta += 2) {
                if (getPc(sqSrc + delta) == pcOppSide + PIECE_PAWN) {
                    return true;
                }
            }
            for (int i = 0; i < 4; i ++) {
                if (!isEmpty(sqSrc + ADVISOR_DELTA[i])) {
                    continue;
                }
                for (int j = 0; j < 2; j ++) {
                    int pcDst = getPc(sqSrc + KNIGHT_CHECK_DELTA[i][j]);
                    if (pcDst == pcOppSide + PIECE_KNIGHT) {
                        return true;
                    }
                }
            }
            for (int i = 0; i < 4; i ++) {
                int delta = KING_DELTA[i];
                int sqDst = sqSrc + delta;
                while (board.contains(sqDst)) {
                    int pcDst = getPc(sqDst);
                    if (pcDst > 0) {
                        if (pcDst == pcOppSide + PIECE_ROOK || pcDst == pcOppSide + PIECE_KING) {
                            return true;
                        }
                        break;
                    }
                    sqDst += delta;
                }
                sqDst += delta;
                while (board.contains(sqDst)) {
                    int pcDst = getPc(sqDst);
                    if (pcDst > 0) {
                        if (pcDst == pcOppSide + PIECE_CANNON) {
                            return true;
                        }
                        break;
                    }
                    sqDst += delta;
                }
            }
            return false;
        }
        return false;
    }

    public int getMirrorLock() {
        Position pos = new Position(new Player());
        pos.clearBoard();
        for (int sq = 0; sq < square.length(); sq ++) {
            int pc = square.get(sq);
            if (pc > 0) {
                pos.addPiece(Board.MIRROR_SQUARE(sq), pc);
            }
        }

        if (isComputer()) {
            pos.changeSide();
        }
        return pos.getUnSignedLock();
    }
}
