package com.jamesswafford.chess4j.utils;

import java.util.List;

import com.jamesswafford.chess4j.Color;
import com.jamesswafford.chess4j.board.Bitboard;
import com.jamesswafford.chess4j.board.Board;
import com.jamesswafford.chess4j.board.CastlingRights;
import com.jamesswafford.chess4j.board.Magic;
import com.jamesswafford.chess4j.board.Move;
import com.jamesswafford.chess4j.board.squares.File;
import com.jamesswafford.chess4j.board.squares.North;
import com.jamesswafford.chess4j.board.squares.Rank;
import com.jamesswafford.chess4j.board.squares.South;
import com.jamesswafford.chess4j.board.squares.Square;
import com.jamesswafford.chess4j.pieces.Bishop;
import com.jamesswafford.chess4j.pieces.King;
import com.jamesswafford.chess4j.pieces.Knight;
import com.jamesswafford.chess4j.pieces.Pawn;
import com.jamesswafford.chess4j.pieces.Piece;
import com.jamesswafford.chess4j.pieces.Queen;
import com.jamesswafford.chess4j.pieces.Rook;

public class BoardUtils {

	public static int getNumPawns(Board board,Color sideToMove) {
		int numPawns = 0;
		
		List<Square> squares = Square.allSquares();
		for (Square sq : squares) {
			Piece p = board.getPiece(sq);
			if (p instanceof Pawn && p.getColor().equals(sideToMove)) {
				numPawns++;
			}
		}
		
		return numPawns;
	}
	
	public static int getNumNonPawns(Board board,Color sideToMove) {
		int n = 0;
		
		List<Square> squares = Square.allSquares();
		for (Square sq : squares) {
			Piece p = board.getPiece(sq);
			if ((p instanceof Queen || p instanceof Rook || p instanceof Bishop || p instanceof Knight) 
				&& p.getColor().equals(sideToMove)) {
				n++;
			}
		}
		
		return n;
	}
	
	public static boolean isDiagonal(Square sq1,Square sq2) {
		return sq1.rank().distance(sq2.rank()) == sq1.file().distance(sq2.file());
	}
	
	private static boolean isGoodPawnMove(Board board,Move m) {
		
		if (m.captured()==null) {
			if (board.getPiece(m.to()) != null) return false;
			if (board.getPlayerToMove()==Color.WHITE) {
				Square nsq = North.getInstance().next(m.from());
				Square nnsq = North.getInstance().next(nsq);
				if (m.to()==nsq || (m.to()==nnsq && board.getPiece(nsq)==null)) return true;
			} else {
				Square ssq = South.getInstance().next(m.from());
				Square sssq = South.getInstance().next(ssq);
				if (m.to()==ssq || (m.to()==sssq && board.getPiece(ssq)==null)) return true;
			}
		} else {
			if ((Bitboard.pawnAttacks[m.from().value()][board.getPlayerToMove().getColor()] 
					& Bitboard.squares[m.to().value()]) == 0) return false;
			
			if (board.getPlayerToMove()==Color.WHITE) {
				if (m.isEpCapture()) {
					if (m.to()==board.getEPSquare() && board.getPiece(South.getInstance().next(m.to()))==Pawn.BLACK_PAWN) 
						return true;
				} else {
					if (board.getPiece(m.to())==m.captured()) return true;
				}
			} else {
				if (m.isEpCapture()) {
					if (m.to()==board.getEPSquare() && board.getPiece(North.getInstance().next(m.to()))==Pawn.WHITE_PAWN) 
						return true;
				} else {
					if (board.getPiece(m.to())==m.captured()) return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean isGoodMove(Board board,Move m) {
		
		Piece mover = board.getPiece(m.from());
		if (mover==null) {
			return false;
		}

		// is the piece the correct color for the player on move?
		if (mover.getColor() != board.getPlayerToMove()) {
			return false;
		}

		// is the piece on the from square correct?
		if (m.piece() != mover) {
			return false;
		}

		if (mover==Pawn.WHITE_PAWN || mover==Pawn.BLACK_PAWN) {
			return isGoodPawnMove(board,m);
		}
		
		// validate capture flag
		if (m.captured() != null) {
			if (board.getPlayerToMove()==Color.WHITE) {
				if (!m.captured().isBlack()) return false;
			} else {
				if (!m.captured().isWhite()) return false;
			}
		} else {
			// not a capture, so destination square should be empty
			if (board.getPiece(m.to()) != null) {
				return false;
			}
		}
		
		if (mover==Knight.WHITE_KNIGHT || mover==Knight.BLACK_KNIGHT) {
			if ((Bitboard.squares[m.to().value()] & Bitboard.knightMoves[m.from().value()]) != 0)
				return true;
		} else if (mover==Bishop.WHITE_BISHOP || mover==Bishop.BLACK_BISHOP) {
			if ((Bitboard.squares[m.to().value()] & Magic.getBishopMoves(board, m.from().value(), ~0L)) != 0)
				return true;
		} else if (mover==Rook.WHITE_ROOK || mover==Rook.BLACK_ROOK) {
			if ((Bitboard.squares[m.to().value()] & Magic.getRookMoves(board, m.from().value(), ~0L)) != 0)
				return true;
		} else if (mover==Queen.WHITE_QUEEN || mover==Queen.BLACK_QUEEN) {
			if ((Bitboard.squares[m.to().value()] & Magic.getQueenMoves(board, m.from().value(), ~0L)) != 0)
				return true;
		} else if (mover==King.WHITE_KING || mover==King.BLACK_KING) {
			if ((Bitboard.squares[m.to().value()] & Bitboard.kingMoves[m.from().value()]) != 0)
				return true;
			
			if (m.isCastle()) {
				if (m.to()==Square.valueOf(File.FILE_G, Rank.RANK_1) 
						&& board.canCastle(CastlingRights.WHITE_KINGSIDE)) {
					return true;
				} else if (m.to()==Square.valueOf(File.FILE_C, Rank.RANK_1) 
						&& board.canCastle(CastlingRights.WHITE_QUEENSIDE)) {
					return true;
				} else if (m.to()==Square.valueOf(File.FILE_G, Rank.RANK_8)	
						&& board.canCastle(CastlingRights.BLACK_KINGSIDE)) {
					return true;
				} else if (m.to()==Square.valueOf(File.FILE_C, Rank.RANK_8)
						&& board.canCastle(CastlingRights.BLACK_QUEENSIDE)) {
					return true;
				}
			}
		}
		
		
		return false;
	}
}
