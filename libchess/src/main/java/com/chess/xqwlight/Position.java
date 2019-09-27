/*
Position.java - Source Code for XiangQi Wizard Light, Part I

XiangQi Wizard Light - a Chinese Chess Program for Java ME
Designed by Morning Yellow, Version: 1.25, Last Modified: Mar. 2008
Copyright (C) 2004-2008 www.elephantbase.net

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package com.chess.xqwlight;

import com.chess.data.AbstractArea;
import com.chess.data.Board;
import com.chess.data.Fort;
import com.chess.data.LegalSpan;
import com.chess.data.Location;
import com.chess.data.PieceValue;
import com.chess.data.Pin;
import com.chess.data.Square;

import java.io.InputStream;
import java.util.Random;

public class Position {
	public static final int MATE_VALUE = 10000;
	public static final int BAN_VALUE = MATE_VALUE - 100;
	public static final int WIN_VALUE = MATE_VALUE - 200;
	public static final int NULL_SAFE_MARGIN = 400;
	public static final int NULL_OKAY_MARGIN = 200;
	public static final int DRAW_VALUE = 20;
	public static final int ADVANCED_VALUE = 3;

	public static final int MAX_MOVE_NUM = 256;
	public static final int MAX_GEN_MOVES = 128;
	public static final int MAX_BOOK_SIZE = 16384;

	public static final int PIECE_KING = 0;
	public static final int PIECE_ADVISOR = 1;
	public static final int PIECE_BISHOP = 2;
	public static final int PIECE_KNIGHT = 3;
	public static final int PIECE_ROOK = 4;
	public static final int PIECE_CANNON = 5;
	public static final int PIECE_PAWN = 6;

	private final AbstractArea board = new Board();
	private final AbstractArea fort = new Fort();
	private final LegalSpan legalSpan = new LegalSpan();
	private final Pin pin = new Pin();
	private final PieceValue pieceValue = new PieceValue();

	private final Square square = new Square();

	public static final int[] KING_DELTA = {-16, -1, 1, 16};
	public static final int[] ADVISOR_DELTA = {-17, -15, 15, 17};
	public static final int[][] KNIGHT_DELTA = {{-33, -31}, {-18, 14}, {-14, 18}, {31, 33}};
	public static final int[][] KNIGHT_CHECK_DELTA = {{-33, -18}, {-31, -14}, {14, 31}, {18, 33}};
	public static final int[] MVV_VALUE = {50, 10, 10, 30, 40, 30, 20, 0};

	public static int SQUARE_FLIP(int sq) {
		return 254 - sq;
	}

	public static int SQUARE_FORWARD(int sq, int sd) {
		return sq - 16 + (sd << 5);
	}

	public static boolean HOME_HALF(int sq, int sd) {
		return (sq & 0x80) != (sd << 7);
	}

	public static boolean AWAY_HALF(int sq, int sd) {
		return (sq & 0x80) == (sd << 7);
	}

	public static boolean SAME_HALF(int sqSrc, int sqDst) {
		return ((sqSrc ^ sqDst) & 0x80) == 0;
	}

	public static boolean SAME_RANK(int sqSrc, int sqDst) {
		return ((sqSrc ^ sqDst) & 0xf0) == 0;
	}

	public static boolean SAME_FILE(int sqSrc, int sqDst) {
		return ((sqSrc ^ sqDst) & 0x0f) == 0;
	}

	public static int SIDE_TAG(int sd) {
		return 8 + (sd << 3);
	}

	public static int OPP_SIDE_TAG(int sd) {
		return 16 - (sd << 3);
	}

	public static int MVV_LVA(int pc, int lva) {
		return MVV_VALUE[pc & 7] - lva;
	}

	public static final String FEN_PIECE = "        KABNRCP kabnrcp ";

	public static int PreGen_zobristKeyPlayer;
	public static int PreGen_zobristLockPlayer;
	public static int[][] PreGen_zobristKeyTable = new int[14][256];
	public static int[][] PreGen_zobristLockTable = new int[14][256];

	public static Random random = new Random();

	public static int bookSize = 0;
	public static int[] bookLock = new int[MAX_BOOK_SIZE];
	public static short[] bookMove = new short[MAX_BOOK_SIZE];
	public static short[] bookValue = new short[MAX_BOOK_SIZE];

	public static volatile boolean bookLoadOk = false;

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

	public int sdPlayer;
	public int zobristKey;
	public int zobristLock;
	public int vlWhite, vlBlack;
	public int moveNum, distance;

	public int[] mvList = new int[MAX_MOVE_NUM];
	public int[] pcList = new int[MAX_MOVE_NUM];
	public int[] keyList = new int[MAX_MOVE_NUM];
	public boolean[] chkList = new boolean[MAX_MOVE_NUM];

	public void clearBoard() {
		sdPlayer = 0;
		square.clear();
		zobristKey = zobristLock = 0;
		vlWhite = vlBlack = 0;
	}

	public void setIrrev() {
		mvList[0] = pcList[0] = 0;
		chkList[0] = checked();
		moveNum = 1;
		distance = 0;
	}

	public void addPiece(int sq, int pc, boolean del) {
		int pcAdjust;
		if (del) {
		    square.clear(sq);
        } else {
		    square.set(sq, pc);
        }

		if (pc < 16) {
			pcAdjust = pc - 8;
			vlWhite += pieceValue.getValue(pcAdjust, sq, del);
		} else {
			pcAdjust = pc - 16;
			vlBlack += pieceValue.getValue(pcAdjust, SQUARE_FLIP(sq), del);
			pcAdjust += 7;
		}
		zobristKey ^= PreGen_zobristKeyTable[pcAdjust][sq];
		zobristLock ^= PreGen_zobristLockTable[pcAdjust][sq];
	}

	public void addPiece(int sq, int pc) {
		addPiece(sq, pc, false);
	}

	public void delPiece(int sq, int pc) {
		addPiece(sq, pc, true);
	}

	public void movePiece() {
		int sqSrc = Board.SRC(mvList[moveNum]);
		int sqDst = Board.DST(mvList[moveNum]);
		pcList[moveNum] = getPc(sqDst);
		if (pcList[moveNum] > 0) {
			delPiece(sqDst, pcList[moveNum]);
		}
		int pc = getPc(sqSrc);
		delPiece(sqSrc, pc);
		addPiece(sqDst, pc);
	}

	public void undoMovePiece() {
		int sqSrc = Board.SRC(mvList[moveNum]);
		int sqDst = Board.DST(mvList[moveNum]);
		int pc = getPc(sqDst);
		delPiece(sqDst, pc);
		addPiece(sqSrc, pc);
		if (pcList[moveNum] > 0) {
			addPiece(sqDst, pcList[moveNum]);
		}
	}

	public void changeSide() {
		sdPlayer = 1 - sdPlayer;
		zobristKey ^= PreGen_zobristKeyPlayer;
		zobristLock ^= PreGen_zobristLockPlayer;
	}

	public boolean makeMove(int mv) {
		keyList[moveNum] = zobristKey;
		mvList[moveNum] = mv;
		movePiece();
		if (checked()) {
			undoMovePiece();
			return false;
		}
		changeSide();
		chkList[moveNum] = checked();
		moveNum ++;
		distance ++;
		return true;
	}

	public void undoMakeMove() {
		moveNum --;
		distance --;
		changeSide();
		undoMovePiece();
	}

	public void nullMove() {
		keyList[moveNum] = zobristKey;
		changeSide();
		mvList[moveNum] = pcList[moveNum] = 0;
		chkList[moveNum] = false;
		moveNum ++;
		distance ++;
	}

	public void undoNullMove() {
		moveNum --;
		distance --;
		changeSide();
	}

	private boolean validateFen(String fen, int index) {
        if (index == fen.length()) {
            setIrrev();
            return false;
        }

        return true;
    }

    private void addPiece(Location location, char c, int offset) {
        int pt = Board.addPiece(location, c);
        if (pt >= 0) {
            addPiece(Board.getSq(location), pt + offset);
        }
    }

	public void fromFen(String fen) {
        clearBoard();

        if (!validateFen(fen, 0)) {
			return;
		}

        Location location = Board.getLeftTop();
        int index = 0;
		char c = fen.charAt(index);
		while (c != ' ') {
			if (c == '/') {
			    if (!Board.nextRow(location)) {
			        break;
                }
			} else if (c >= '1' && c <= '9') {
			    location.xShift(c);
			} else if (c >= 'A' && c <= 'Z') {
                addPiece(location, c, 8);
			} else if (c >= 'a' && c <= 'z') {
                addPiece(location, c, 16);
			}

			index++;
			if (!validateFen(fen, index)) {
				return;
			}

			c = fen.charAt(index);
		}

		index ++;
		if (!validateFen(fen, index)) {
			return;
		}

		if (sdPlayer == (fen.charAt(index) == 'b' ? 0 : 1)) {
			changeSide();
		}
		setIrrev();
	}

	public String toFen() {
		StringBuffer fen = new StringBuffer();
		for (int y = Board.top(); y <= Board.bottom(); y ++) {
			int k = 0;
			for (int x = Board.left(); x <= Board.right(); x ++) {
				int pc = getPc(Board.xy(x, y));
				if (pc > 0) {
					if (k > 0) {
						fen.append((char) ('0' + k));
						k = 0;
					}
					fen.append(FEN_PIECE.charAt(pc));
				} else {
					k ++;
				}
			}
			if (k > 0) {
				fen.append((char) ('0' + k));
			}
			fen.append('/');
		}

		fen.setCharAt(fen.length() - 1, ' ');
		fen.append(sdPlayer == 0 ? 'w' : 'b');
		return fen.toString();
	}

	public int generateAllMoves(int[] mvs) {
		return generateMoves(mvs, null);
	}

	public int generateMoves(int[] mvs, int[] vls) {
		int moves = 0;
		int pcSelfSide = SIDE_TAG(sdPlayer);
		int pcOppSide = OPP_SIDE_TAG(sdPlayer);
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
					if (!(board.contains(sqDst) && HOME_HALF(sqDst, sdPlayer) && square.isEmpty(sqDst))) {
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
				int sqDst = SQUARE_FORWARD(sqSrc, sdPlayer);
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
				if (AWAY_HALF(sqSrc, sdPlayer)) {
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
		int pcSelfSide = SIDE_TAG(sdPlayer);
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
			if (AWAY_HALF(sqDst, sdPlayer) && (sqDst == sqSrc - 1 || sqDst == sqSrc + 1)) {
				return true;
			}
			return sqDst == SQUARE_FORWARD(sqSrc, sdPlayer);
		default:
			return false;
		}
	}

	public boolean checked() {
		int pcSelfSide = SIDE_TAG(sdPlayer);
		int pcOppSide = OPP_SIDE_TAG(sdPlayer);
		for (int sqSrc = 0; sqSrc < 256; sqSrc ++) {
			if (getPc(sqSrc) != pcSelfSide + PIECE_KING) {
				continue;
			}
			if (getPc(SQUARE_FORWARD(sqSrc, sdPlayer)) == pcOppSide + PIECE_PAWN) {
				return true;
			}
			for (int delta = -1; delta <= 1; delta += 2) {
				if (getPc(sqSrc + delta) == pcOppSide + PIECE_PAWN) {
					return true;
				}
			}
			for (int i = 0; i < 4; i ++) {
				if (!square.isEmpty(sqSrc + ADVISOR_DELTA[i])) {
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

	public boolean isMate() {
		int[] mvs = new int[MAX_GEN_MOVES];
		int moves = generateAllMoves(mvs);
		for (int i = 0; i < moves; i ++) {
			if (makeMove(mvs[i])) {
				undoMakeMove();
				return false;
			}
		}
		return true;
	}

	public int mateValue() {
		return distance - MATE_VALUE;
	}

	public int banValue() {
		return distance - BAN_VALUE;
	}

	public int drawValue() {
		return (distance & 1) == 0 ? -DRAW_VALUE : DRAW_VALUE;
	}

	public int evaluate() {
		int vl = (sdPlayer == 0 ? vlWhite - vlBlack : vlBlack - vlWhite) + ADVANCED_VALUE;
		return vl == drawValue() ? vl - 1 : vl;
	}

	public boolean nullOkay() {
		return (sdPlayer == 0 ? vlWhite : vlBlack) > NULL_OKAY_MARGIN;
	}

	public boolean nullSafe() {
		return (sdPlayer == 0 ? vlWhite : vlBlack) > NULL_SAFE_MARGIN;
	}

	public boolean inCheck() {
		return chkList[moveNum - 1];
	}

	public boolean captured() {
		return pcList[moveNum - 1] > 0;
	}

	public int repValue(int vlRep) {
		int vlReturn = ((vlRep & 2) == 0 ? 0 : banValue()) + ((vlRep & 4) == 0 ? 0 : -banValue());
		return vlReturn == 0 ? drawValue() : vlReturn;
	}

	public int repStatus() {
		return repStatus(1);
	}

	public int repStatus(int recur_) {
		int recur = recur_;
		boolean selfSide = false;
		boolean perpCheck = true;
		boolean oppPerpCheck = true;
		int index = moveNum - 1;
		while (mvList[index] > 0 && pcList[index] == 0) {
			if (selfSide) {
				perpCheck = perpCheck && chkList[index];
				if (keyList[index] == zobristKey) {
					recur --;
					if (recur == 0) {
						return 1 + (perpCheck ? 2 : 0) + (oppPerpCheck ? 4 : 0);
					}
				}
			} else {
				oppPerpCheck = oppPerpCheck && chkList[index];
			}
			selfSide = !selfSide;
			index --;
		}
		return 0;
	}

	public Position mirror() {
		Position pos = new Position();
		pos.clearBoard();
		square.mirror(pos);
		if (sdPlayer == 1) {
			pos.changeSide();
		}
		return pos;
	}

	public int bookMove() {
		if (bookSize == 0) {
			return 0;
		}
		boolean mirror = false;
		int lock = zobristLock >>> 1; // Convert into Unsigned
		int index = Util.binarySearch(lock, bookLock, 0, bookSize);
		if (index < 0) {
			mirror = true;
			lock = mirror().zobristLock >>> 1; // Convert into Unsigned
			index = Util.binarySearch(lock, bookLock, 0, bookSize);
		}
		if (index < 0) {
			return 0;
		}
		index --;
		while (index >= 0 && bookLock[index] == lock) {
			index --;
		}
		int[] mvs = new int[MAX_GEN_MOVES];
		int[] vls = new int[MAX_GEN_MOVES];
		int value = 0;
		int moves = 0;
		index ++;
		while (index < bookSize && bookLock[index] == lock) {
			int mv = 0xffff & bookMove[index];
			mv = (mirror ? Board.MIRROR_MOVE(mv) : mv);
			if (legalMove(mv)) {
				mvs[moves] = mv;
				vls[moves] = bookValue[index];
				value += vls[moves];
				moves ++;
				if (moves == MAX_GEN_MOVES) {
					break;
				}
			}
			index ++;
		}
		if (value == 0) {
			return 0;
		}
		value = Math.abs(random.nextInt()) % value;
		for (index = 0; index < moves; index ++) {
			value -= vls[index];
			if (value < 0) {
				break;
			}
		}
		return mvs[index];
	}

	public int historyIndex(int mv) {
		return ((getPc(Board.SRC(mv)) - 8) << 8) + Board.DST(mv);
	}

	public int getPc(int sq) {
	    return square.get(sq);
    }


    public static int CHAR_TO_PIECE(char c) {
        switch (c) {
            case 'K':
            case 'k':
                return PIECE_KING;
            case 'A':
            case 'a':
                return PIECE_ADVISOR;
            case 'B':
            case 'E':
            case 'b':
            case 'e':
                return PIECE_BISHOP;
            case 'H':
            case 'N':
            case 'h':
            case 'n':
                return PIECE_KNIGHT;
            case 'R':
            case 'r':
                return PIECE_ROOK;
            case 'C':
            case 'c':
                return PIECE_CANNON;
            case 'P':
            case 'p':
                return PIECE_PAWN;
            default:
                return -1;
        }
    }
}