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
import com.chess.data.Book;
import com.chess.data.Fort;
import com.chess.data.IMove;
import com.chess.data.IPiece;
import com.chess.data.ISearch;
import com.chess.data.LegalSpan;
import com.chess.data.Location;
import com.chess.data.PieceValue;
import com.chess.data.Pin;
import com.chess.data.Player;
import com.chess.data.Square;

import java.util.Random;

public class Position implements IPiece, ISearch, IMove {
	public static final int NULL_SAFE_MARGIN = 400;
	public static final int NULL_OKAY_MARGIN = 200;
	public static final int DRAW_VALUE = 20;
	public static final int ADVANCED_VALUE = 3;

	private final AbstractArea board = new Board();
	private final AbstractArea fort = new Fort();
	private final LegalSpan legalSpan = new LegalSpan();
	private final Pin pin = new Pin();
	private final PieceValue pieceValue = new PieceValue();

	private final Square square = new Square();
	private final Book book = new Book();

	private final Player player;

	protected static final int[] KING_DELTA = {-16, -1, 1, 16};
	protected static final int[] ADVISOR_DELTA = {-17, -15, 15, 17};
	protected static final int[][] KNIGHT_DELTA = {{-33, -31}, {-18, 14}, {-14, 18}, {31, 33}};
	protected static final int[][] KNIGHT_CHECK_DELTA = {{-33, -18}, {-31, -14}, {14, 31}, {18, 33}};
	protected static final int[] MVV_VALUE = {50, 10, 10, 30, 40, 30, 20, 0};

	public Position(Player player) {
		this.player = player;
	}

	public static int SQUARE_FLIP(int sq) {
		return 254 - sq;
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

	public static int MVV_LVA(int pc, int lva) {
		return MVV_VALUE[pc & 7] - lva;
	}

	public static final String FEN_PIECE = "        KABNRCP kabnrcp ";

	public static Random random = new Random();

	public int vlWhite, vlBlack;
	public int moveNum, distance;

	public int[] mvList = new int[MAX_MOVE_NUM];
	public int[] pcList = new int[MAX_MOVE_NUM];
	public boolean[] chkList = new boolean[MAX_MOVE_NUM];

	public void clearBoard() {
		player.clear();
		square.clear();
		book.clear();
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
		book.addPiece(pcAdjust, sq);
	}

	public void addPiece(int sq, int pc) {
		addPiece(sq, pc, false);
	}

	public void delPiece(int sq, int pc) {
		addPiece(sq, pc, true);
	}

	public void movePiece() {
		final int mv = mvList[moveNum];
		int sqSrc = Board.SRC(mv);
		int sqDst = Board.DST(mv);
		final int piece = getPc(sqDst);
		pcList[moveNum] = piece;
		if (piece > 0) {
			delPiece(sqDst, piece);
		}
		int pc = getPc(sqSrc);
		delPiece(sqSrc, pc);
		addPiece(sqDst, pc);
	}

	public void undoMovePiece() {
		final int mv = mvList[moveNum];
		int sqSrc = Board.SRC(mv);
		int sqDst = Board.DST(mv);
		int pc = getPc(sqDst);
		delPiece(sqDst, pc);
		addPiece(sqSrc, pc);
		final int piece = pcList[moveNum];
		if (piece > 0) {
			addPiece(sqDst, piece);
		}
	}

	public void changeSide() {
		player.changeSide();
		book.changeSide();
	}

	public boolean makeMove(int mv) {
		book.setKey(moveNum);
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
		book.setKey(moveNum);
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
		if (Board.checkRightBound(location)) {
			int pt = PieceValue.charToPiece(c);
			if (pt >= 0) {
				addPiece(Board.getSq(location), pt + offset);
			}
			location.nextCol();
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
		while (!Character.isSpaceChar(c)) {
			if (c == '/') {
			    if (!Board.nextRow(location)) {
			        break;
                }
			} else if (Character.isDigit(c) && c != '0') {
			    location.xShift(c);
			} else if (Character.isUpperCase(c)) {
                addPiece(location, c, 8);
			} else if (Character.isLowerCase(c)) {
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

		if (player.isOpposite(fen.charAt(index))) {
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
		fen.append(player.getChar());
		return fen.toString();
	}

	public int generateAllMoves(int[] mvs) {
		return generateMoves(mvs, null);
	}

	public int generateMoves(int[] mvs, int[] vls) {
		int moves = 0;
		int pcSelfSide = player.sideTag();
		int pcOppSide = player.oppSideTag();
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
					if (!(board.contains(sqDst) && player.isHomeHalf(sqDst) && square.isEmpty(sqDst))) {
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
				int sqDst = player.squareForward(sqSrc);
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
				if (player.awayHalf(sqSrc)) {
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
		int pcSelfSide = player.sideTag();
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
			if (player.awayHalf(sqDst) && (sqDst == sqSrc - 1 || sqDst == sqSrc + 1)) {
				return true;
			}
			return sqDst == player.squareForward(sqSrc);
		default:
			return false;
		}
	}

	public boolean checked() {
		int pcSelfSide = player.sideTag();
		int pcOppSide = player.oppSideTag();
		for (int sqSrc = 0; sqSrc < 256; sqSrc ++) {
			if (getPc(sqSrc) != pcSelfSide + PIECE_KING) {
				continue;
			}
			if (getPc(player.squareForward(sqSrc)) == pcOppSide + PIECE_PAWN) {
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
		int vl = (player.isHuman() ? vlWhite - vlBlack : vlBlack - vlWhite) + ADVANCED_VALUE;
		return vl == drawValue() ? vl - 1 : vl;
	}

	public boolean nullOkay() {
		return (player.isHuman() ? vlWhite : vlBlack) > NULL_OKAY_MARGIN;
	}

	public boolean nullSafe() {
		return (player.isHuman() ? vlWhite : vlBlack) > NULL_SAFE_MARGIN;
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
				if (book.checkKey(index)) {
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
		Position pos = new Position(new Player());
		pos.clearBoard();
		square.mirror(pos);

		if (player.isComputer()) {
			pos.changeSide();
		}
		return pos;
	}

	private int getUnSignedLock() {
		return book.getUnSignedLock();
	}

	public int bookMove() {
		if (book.empty()) {
			return 0;
		}

		boolean mirror = false;
		int lock = getUnSignedLock(); // Convert into Unsigned
		int index = book.binarySearch(lock);
		if (index < 0) {
			mirror = true;
			lock = mirror().getUnSignedLock(); // Convert into Unsigned
			index = book.binarySearch(lock);
		}

		if (index < 0) {
			return 0;
		}
		index --;
		while (book.checkLowerLock(index, lock)) {
			index --;
		}
		int[] mvs = new int[MAX_GEN_MOVES];
		int[] vls = new int[MAX_GEN_MOVES];
		int value = 0;
		int moves = 0;
		index ++;
		while (book.checkUpperLock(index, lock)) {
			int mv = book.getMove(index);
			mv = (mirror ? Board.MIRROR_MOVE(mv) : mv);
			if (legalMove(mv)) {
				mvs[moves] = mv;
				vls[moves] = book.getValue(index);
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

	public int getZobristKey() {
		return book.zobristKey;
	}

	public int getZobristLock() {
		return book.zobristLock;
	}
}