package org.duchchess.eval;

import org.duchchess.Color;
import org.duchchess.board.Bitboard;
import org.duchchess.board.Board;
import org.duchchess.board.Piece;
import org.duchchess.move.BitboardMoves;
import org.duchchess.move.Move;

public class StaticExchangeEvaluator {
    /**
     * @return true if capture is winning
     */
    public static boolean see(Board board, int move, int threshold) {
        if (Move.isCastling(move)) {
            return 0 >= threshold;
        }
        int fromSquare = Move.getFromSquare(move);
        int toSquare = Move.getToSquare(move);

        int victim = board.squares[toSquare];
        int nextVictim = board.squares[fromSquare];

        long occupied = board.allPieces ^ Bitboard.getBB(toSquare) ^ Bitboard.getBB(fromSquare);

        if (Move.isPassant(move)) {
            occupied ^= Bitboard.getBB(board.epSquare);
            victim = Piece.PAWN;
        }

        int swapScore = EvalConstants.MATERIAL_VALUES[victim] - threshold;
        if (swapScore < 0) {
            return false;
        }

        swapScore -= EvalConstants.MATERIAL_VALUES[nextVictim];
        if (swapScore >= 0) {
            return true;
        }


        long bishopsBB = (board.piecesBB[Color.WHITE][Piece.BISHOP] |
                board.piecesBB[Color.BLACK][Piece.BISHOP] |
                board.piecesBB[Color.WHITE][Piece.QUEEN] |
                board.piecesBB[Color.BLACK][Piece.QUEEN]) & occupied;

        long rooksBB = (board.piecesBB[Color.WHITE][Piece.ROOK] |
                board.piecesBB[Color.BLACK][Piece.ROOK] |
                board.piecesBB[Color.WHITE][Piece.QUEEN] |
                board.piecesBB[Color.BLACK][Piece.QUEEN]) & occupied;

        long attackers = getSquareAttackers(board, occupied, bishopsBB, rooksBB, toSquare);

        int color = board.oppositeColor;

        while (true) {
            long friendlyAttackers = attackers & board.friendlyPieces[color];
            if (friendlyAttackers == 0) {
                break;
            }

            // find weakest attacker
            nextVictim = Piece.KING;
            for (int currentAttacker = Piece.PAWN; currentAttacker <= Piece.KING; currentAttacker++) {
                if ((friendlyAttackers & board.piecesBB[color][currentAttacker]) != 0) {
                    nextVictim = currentAttacker;
                    break;
                }
            }

            fromSquare = Long.numberOfTrailingZeros(friendlyAttackers & board.piecesBB[color][nextVictim]);
            long fromBB = Bitboard.getBB(fromSquare);
            occupied ^= fromBB;

            if (nextVictim == Piece.PAWN || nextVictim == Piece.BISHOP || nextVictim == Piece.QUEEN) {
                bishopsBB &= ~fromBB;

                if (bishopsBB != Bitboard.EMPTY) {
                    attackers |= BitboardMoves.bishopMoves(toSquare, occupied) & bishopsBB;
                }
            }

            if (nextVictim == Piece.ROOK || nextVictim == Piece.QUEEN) {
                rooksBB &= ~fromBB;

                if (rooksBB != Bitboard.EMPTY) {
                    attackers |= BitboardMoves.rookMoves(toSquare, occupied) & rooksBB;
                }
            }

            attackers &= occupied;

            color ^= 1;

            swapScore = -swapScore - 1 - EvalConstants.MATERIAL_VALUES[nextVictim];

            if (swapScore >= 0) {
                if (nextVictim == Piece.KING && (attackers & board.friendlyPieces[color]) != Bitboard.EMPTY) {
                    color ^= 1;
                }
                break;
            }
        }

        return color != board.colorToMove;

    }

    static long getSquareAttackers(Board board, long occupied, long bishopsBB, long rookBB, int square) {
        long result = 0;
        if (bishopsBB != 0) {
            result |= BitboardMoves.bishopMoves(square, occupied) & bishopsBB;
        }
        if (rookBB != 0) {
            result |= BitboardMoves.rookMoves(square, occupied) & rookBB;
        }

        return result |
                (BitboardMoves.pawnAttacks(Color.WHITE, square) & board.piecesBB[Color.BLACK][Piece.PAWN]) |
                (BitboardMoves.pawnAttacks(Color.BLACK, square) & board.piecesBB[Color.WHITE][Piece.PAWN]) |
                (BitboardMoves.knightMoves(square)
                        & (board.piecesBB[Color.WHITE][Piece.KNIGHT] | board.piecesBB[Color.BLACK][Piece.KNIGHT])) |
                (BitboardMoves.kingMoves(square)
                        & (board.piecesBB[Color.WHITE][Piece.KING] | board.piecesBB[Color.BLACK][Piece.KING]));

    }
}
