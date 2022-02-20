package org.duchchess.move;

import org.duchchess.board.Bitboard;
import org.duchchess.board.Piece;
import org.duchchess.board.Square;

/**
 * 18 bit
 */
public class Move {
    // FROM 6 bits
    public static final int TO_SHIFT = 6; // 6 bits
    public static final int ATTACK_SHIFT = 12; // 3 bits
    public static final int TYPE_SHIFT = 15; // 3 bits

    public static int createCastling(int fromSquare, int toSquare) {
        return fromSquare | (toSquare << TO_SHIFT) | (MoveType.CASTLING << TYPE_SHIFT);
    }

    public static int createPromotion(int fromSquare, int toSquare, int attackedPiece, int promotionPiece) {
        return fromSquare | (toSquare << TO_SHIFT) | (attackedPiece << ATTACK_SHIFT) | ((promotionPiece + 1) << TYPE_SHIFT);
    }

    public static int create(int fromSquare, int toSquare, int attackedPiece) {
        return fromSquare | (toSquare << TO_SHIFT) | (attackedPiece << ATTACK_SHIFT);
    }

    public static int getPromotedPiece(int move) {
        return getType(move) - 1;
    }

    public static int getToSquare(int move) {
        return move >>> TO_SHIFT & 0b111111;
    }

    public static int getFromSquare(int move) {
        return move & 0b111111;
    }

    public static int getAttackedPiece(int move) {
        return move >>> ATTACK_SHIFT & 0b111;
    }

    public static int getType(int move) {
        return move >>> TYPE_SHIFT;
    }

    public static boolean isCastling(int move) {
        return getType(move) == MoveType.CASTLING;
    }

    public static boolean isPassant(int move) {
        return getType(move) == MoveType.PASSANT;
    }

    public static boolean isPromotion(int move) {
        return getType(move) >= MoveType.PROMO_KNIGHT;
    }

    public static boolean isQuiet(int move) {
        return getAttackedPiece(move) == 0;
    }

    public static String makeString(int move) {
        int fromSquare = Move.getFromSquare(move);
        int toSquare = Move.getToSquare(move);
        if (Move.isPromotion(move)) return Square.makeString(fromSquare) + Square.makeString(toSquare)
                + String.valueOf(Piece.FEN_CHARACTERS[Move.getPromotedPiece(move)]).toLowerCase();

        return Square.makeString(fromSquare) + Square.makeString(toSquare);
    }

    // public static String makeUCIString(int move) {}

    public static void draw(int move) {
        Bitboard.draw(Bitboard.getBB(Move.getFromSquare(move)) | Bitboard.getBB(Move.getToSquare(move)));
    }
}
