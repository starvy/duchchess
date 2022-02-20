package org.duchchess.move;

import org.duchchess.Castling;
import org.duchchess.Color;
import org.duchchess.board.Bitboard;
import org.duchchess.board.Board;
import org.duchchess.board.Piece;
import org.duchchess.board.Square;
import org.duchchess.eval.EvalConstants;
import org.duchchess.search.History;

import static org.duchchess.board.Piece.*;

public class MoveGenerator {
    History history;

    public void setHistory(History history) {
        this.history = history;
    }

    /**
     * ATTACKS + QUIET
     */
    public void generate(Board board, MoveList moveList) {
        generateQuiet(board, moveList);
        generateAttacks(board, moveList);
    }

    public void generateAttacks(Board board, MoveList moveList) {
        generatePawnAttackMoves(board, moveList);

        generateDirectAttacks(board, moveList, KNIGHT);
        generateDirectAttacks(board, moveList, KING);

        generateSlidingAttacks(board, moveList, BISHOP);
        generateSlidingAttacks(board, moveList, ROOK);
        generateQueenMoves(board, moveList, board.allPieces);
    }

    public void generateQuiet(Board board, MoveList moveList) {
        generatePawnQuietMoves(board, moveList);

        generateDirectPieceMoves(board, moveList, KNIGHT, board.emptyBB);
        generateDirectPieceMoves(board, moveList, KING, board.emptyBB);

        generateSlidingPieceMoves(board, moveList, BISHOP, board.emptyBB);
        generateSlidingPieceMoves(board, moveList, ROOK, board.emptyBB);
        generateQueenMoves(board, moveList, board.emptyBB);

        generateCastling(board, moveList);

    }

    void generatePawnQuietMoves(Board board, MoveList moveList) {
        long pieces = board.piecesBB[board.colorToMove][Piece.PAWN];
        if (pieces == 0) return;

        while (pieces != 0L) {
            int fromSquare = Long.numberOfTrailingZeros(pieces);
            long fromBB = Bitboard.getBB(fromSquare);
            long pawnOne = (BitboardMoves.pawnMove(board.colorToMove, fromBB) & board.emptyBB);
            addMoves(board, moveList, fromSquare, pawnOne | (BitboardMoves.doublePawnMove(board.colorToMove, fromSquare, pawnOne) & board.emptyBB));
            pieces &= pieces - 1;
        }
    }

    void generatePawnAttackMoves(Board board, MoveList moveList) {
        long pieces = board.piecesBB[board.colorToMove][Piece.PAWN];
        if (pieces == 0) return;

        while (pieces != 0L) {
            int fromSquare = Long.numberOfTrailingZeros(pieces);
            long attackMoves = BitboardMoves.pawnAttacks(board.colorToMove, fromSquare) & board.friendlyPieces[board.oppositeColor];
            addMoves(board, moveList, fromSquare, attackMoves);
            pieces &= pieces - 1;
        }
    }

    void generateDirectAttacks(Board board, MoveList moveList, int piece) {
        generateDirectPieceMoves(board, moveList, piece, board.friendlyPieces[board.oppositeColor]);
    }

    void generateDirectPieceMoves(Board board, MoveList moveList, int piece, long mask) {
        long pieceBB = board.piecesBB[board.colorToMove][piece];
        while (pieceBB != 0) {
            int fromSquare = Long.numberOfTrailingZeros(pieceBB);
            long toBB = BitboardMoves.directPieceMoves(fromSquare, piece) & ~board.friendlyPieces[board.colorToMove] & mask;
            addMoves(board, moveList, fromSquare, toBB);
            pieceBB &= pieceBB - 1;
        }
    }

    void generateSlidingAttacks(Board board, MoveList moveList, int piece) {
        generateSlidingPieceMoves(board, moveList, piece, board.friendlyPieces[board.oppositeColor]);
    }

    void generateSlidingPieceMoves(Board board, MoveList moveList, int piece, long mask) {
        long pieceBB = board.piecesBB[board.colorToMove][piece];
        while (pieceBB != 0) {
            int fromSquare = Long.numberOfTrailingZeros(pieceBB);
            long toBB = BitboardMoves.slidingPieceMoves(fromSquare, piece, board.allPieces, board.friendlyPieces[board.colorToMove]) & mask;
            addMoves(board, moveList, fromSquare, toBB);
            pieceBB &= pieceBB - 1;
        }
    }

    void generateQueenMoves(Board board, MoveList moveList, long mask) {
        long piece = board.piecesBB[board.colorToMove][QUEEN];
        while (piece != 0) {
            int fromSquare = Long.numberOfTrailingZeros(piece);
            long toBB = (Magic.getBishopMoves(fromSquare, board.allPieces, board.friendlyPieces[board.colorToMove]) |
                    Magic.getRookMoves(fromSquare, board.allPieces, board.friendlyPieces[board.colorToMove])) & mask;

            addMoves(board, moveList, fromSquare, toBB);
            piece &= piece - 1;
        }
    }

    public void generateCastling(Board board, MoveList moveList) {
        long kingCastlingBB = Castling.getCastlingKingBB(board);
        while (kingCastlingBB != 0) {
            int kingToSquare = Long.numberOfTrailingZeros(kingCastlingBB);
            int rookFromSquare = Castling.getRookFromSquare(board.colorToMove, kingToSquare);
            if (board.squares[rookFromSquare] != ROOK) {
                board.print();
            }
            if ((Bitboard.BETWEEN_BB[board.kingSquare[board.colorToMove]][rookFromSquare] & board.allPieces) == 0) {
                // Nothing between king and rook => can castle
                addCastlingMove(board, moveList, board.kingSquare[board.colorToMove], kingToSquare);
            }

            kingCastlingBB &= kingCastlingBB - 1;
        }
    }

    private void addCastlingMove(Board board, MoveList moveList, int fromSquare, int toSquare) {
        int move = Move.createCastling(fromSquare, toSquare);
        moveList.addMove(move, history.getHistoryScore(board.colorToMove, move));
    }

    private void addPromotionMove(Board board, MoveList moveList, int fromSquare, int toSquare) {
        for (int promotionPiece = KNIGHT; promotionPiece <= QUEEN; promotionPiece++) {
            if (board.squares[toSquare] != 0) {
                int move = Move.createPromotion(fromSquare, toSquare, board.squares[toSquare], promotionPiece);
                moveList.addMove(move, getMVVLVAScore(board.squares[fromSquare], board.squares[toSquare]));
                continue;
            }
            int move = Move.createPromotion(fromSquare, toSquare, 0, promotionPiece);
            moveList.addMove(move, history.getHistoryScore(board.colorToMove, move));
        }
    }

    private void addMove(Board board, MoveList moveList, int fromSquare, long toBB) {
        int toSquare = Long.numberOfTrailingZeros(toBB);

        if (board.squares[fromSquare] == PAWN) {
            if (board.colorToMove == Color.WHITE && fromSquare >= Square.H7) {
                addPromotionMove(board, moveList, fromSquare, toSquare);
                return;
            } else if (board.colorToMove == Color.BLACK && fromSquare <= Square.A2) {
                addPromotionMove(board, moveList, fromSquare, toSquare);
                return;
            }
        }

        int move = Move.create(fromSquare, toSquare, board.squares[toSquare]);

        int score;
        if (board.squares[toSquare] != 0) {
            score = getMVVLVAScore(board.squares[fromSquare], board.squares[toSquare]);
        } else {
            score = history.getHistoryScore(board.colorToMove, move);
        }

        moveList.addMove(move, score);
    }

    // adds given pseudo legal moves
    private void addMoves(Board board, MoveList moveList, int fromSquare, long toBB) {

        while (toBB != 0L) {
            addMove(board, moveList, fromSquare, toBB);
            toBB &= toBB - 1;
        }
    }

    private int getMVVLVAScore(int attacker, int victim) {
        return EvalConstants.MATERIAL_VALUES[victim] - attacker;
    }
}
