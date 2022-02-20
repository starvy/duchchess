package org.duchchess;


import org.duchchess.move.BitboardMoves;
import org.duchchess.move.Magic;

import static org.duchchess.board.Piece.*;

public class Check {
    public static boolean isInCheck(final int kingIndex, final int colorToMove, final long[] enemyPieces, final long allPiecesMoved) {
        return (enemyPieces[PAWN] & BitboardMoves.pawnAttacks(colorToMove, kingIndex) |
                enemyPieces[KNIGHT] & BitboardMoves.knightMoves(kingIndex) |
                (enemyPieces[ROOK] | enemyPieces[QUEEN]) & Magic.getRookMoves(kingIndex, allPiecesMoved) |
                (enemyPieces[BISHOP] | enemyPieces[QUEEN]) & Magic.getBishopMoves(kingIndex, allPiecesMoved)
        ) != 0;
    }

    public static boolean isInCheckWithKing(final int kingIndex, final int colorToMove, final long[] enemyPieces, final long allPiecesMoved) {
        return (enemyPieces[PAWN] & BitboardMoves.pawnAttacks(colorToMove, kingIndex) |
                enemyPieces[KNIGHT] & BitboardMoves.knightMoves(kingIndex) |
                (enemyPieces[ROOK] | enemyPieces[QUEEN]) & Magic.getRookMoves(kingIndex, allPiecesMoved) |
                (enemyPieces[BISHOP] | enemyPieces[QUEEN]) & Magic.getBishopMoves(kingIndex, allPiecesMoved) |
                enemyPieces[KING] & BitboardMoves.kingMoves(kingIndex)
        ) != 0;
    }
}
