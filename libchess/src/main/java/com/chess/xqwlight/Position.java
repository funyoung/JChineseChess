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

import com.chess.data.Board;
import com.chess.data.Book;
import com.chess.data.ISearch;
import com.chess.data.Location;
import com.chess.data.PieceValue;
import com.chess.data.Player;
import com.chess.data.Rule;

public class Position implements ISearch {
	public static final int NULL_SAFE_MARGIN = 400;
	public static final int NULL_OKAY_MARGIN = 200;
	public static final int DRAW_VALUE = 20;
	public static final int ADVANCED_VALUE = 3;

	private final PieceValue pieceValue = new PieceValue();

	private final Book book = new Book();

	private final Rule rule = new Rule();

	private final Player player;

	public Position(Player player) {
		this.player = player;
	}

	public static int SQUARE_FLIP(int sq) {
		return 254 - sq;
	}

	public static final String FEN_PIECE = "        KABNRCP kabnrcp ";

	public int vlWhite, vlBlack;
	public int distance;

	private int moveNum;

	public void clearBoard() {
		player.clear();
		book.clear();
		vlWhite = vlBlack = 0;
	}

	public void setIrrev() {
		rule.set(0, player.checked());
		moveNum = 1;
		distance = 0;
	}

	public void addPiece(int sq, int pc, boolean del) {
		player.addPiece(sq, pc, del);

		int pcAdjust;
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
		final int mv = rule.getMove(moveNum);
		int sqSrc = Board.SRC(mv);
		int sqDst = Board.DST(mv);
		final int piece = getPc(sqDst);
		rule.setPc(moveNum, piece);
		if (piece > 0) {
			delPiece(sqDst, piece);
		}
		int pc = getPc(sqSrc);
		delPiece(sqSrc, pc);
		addPiece(sqDst, pc);
	}

	public void undoMovePiece() {
		final int mv = rule.getMove(moveNum);
		int sqSrc = Board.SRC(mv);
		int sqDst = Board.DST(mv);
		int pc = getPc(sqDst);
		delPiece(sqDst, pc);
		addPiece(sqSrc, pc);
		final int piece = rule.queryPc(moveNum);
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
		rule.setMove(moveNum, mv);
		movePiece();
		if (player.checked()) {
			undoMovePiece();
			return false;
		}
		changeSide();
		rule.setCheck(moveNum, player.checked());
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
		rule.set(moveNum, false);
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
		return player.generateMoves(mvs, null);
	}

	public boolean isMate() {
		int[] mvs = Book.generateMaxMove();
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
		return rule.isCheck(moveNum - 1);
	}

	public boolean captured() {
		return rule.checkPc(moveNum - 1);
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
		while (rule.checkMoving(index)) {
			if (selfSide) {
				perpCheck = rule.isCheck(index, perpCheck);
				if (book.checkKey(index)) {
					recur --;
					if (recur == 0) {
						return 1 + (perpCheck ? 2 : 0) + (oppPerpCheck ? 4 : 0);
					}
				}
			} else {
				oppPerpCheck = rule.isCheck(index, oppPerpCheck);
			}
			selfSide = !selfSide;
			index --;
		}
		return 0;
	}

	public int getUnSignedLock() {
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
			lock = player.getMirrorLock(); // Convert into Unsigned
			index = book.binarySearch(lock);
		}

		if (index < 0) {
			return 0;
		}
		index --;
		while (book.checkLowerLock(index, lock)) {
			index --;
		}
		int[] mvs = Book.generateMaxMove();
		int[] vls = Book.generateMaxMove();
		int value = 0;
		int moves = 0;
		index ++;
		while (book.checkUpperLock(index, lock)) {
			int mv = book.getMove(index);
			mv = (mirror ? Board.MIRROR_MOVE(mv) : mv);
			if (player.legalMove(mv)) {
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
		value = Math.abs(book.nextInt()) % value;
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

	public int getZobristKey() {
		return book.zobristKey;
	}

	public int getZobristLock() {
		return book.zobristLock;
	}

	public boolean isMoveLimit() {
		return moveNum > 100;
	}

	private int getPc(int sq) {
		return player.getPc(sq);
	}
}