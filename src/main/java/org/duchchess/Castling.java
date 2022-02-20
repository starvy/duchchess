package org.duchchess;


import org.duchchess.board.Bitboard;
import org.duchchess.board.Board;
import org.duchchess.board.Piece;
import org.duchchess.board.Square;

import static org.duchchess.Color.BLACK;
import static org.duchchess.Color.WHITE;

/**
 * 4 bits white O-O, white O-O-O, black O-O, black O-O-O
 */
public class Castling {
    public static final int WHITE_KING_SIDE = 0b1000;
    public static final int WHITE_QUEEN_SIDE = 0b0100;
    public static final int BLACK_KING_SIDE = 0b0010;
    public static final int BLACK_QUEEN_SIDE = 0b0001;

    public static final int COMBINATIONS = 4 * 4;

    public static final long[] KING_PATH = new long[4];

    static {
        KING_PATH[0] = Bitboard.BETWEEN_BB[Square.E1][Square.H1] | Bitboard.getBB(Square.E1);
        KING_PATH[1] = Bitboard.getBB(Square.C1) | Bitboard.getBB(Square.D1) | Bitboard.getBB(Square.E1);
        KING_PATH[2] = Bitboard.BETWEEN_BB[Square.E8][Square.H8] | Bitboard.getBB(Square.E8);
        KING_PATH[3] = Bitboard.getBB(Square.C8) | Bitboard.getBB(Square.D8) | Bitboard.getBB(Square.E8);
    }

    public static final char[] FEN_CHARACTERS = new char[]{'K', 'Q', 'k', 'q'};

    public static long getCastlingKingBB(Board board) {
        long result = 0L;
        if (board.colorToMove == WHITE) {
            if ((board.castlingRights & WHITE_KING_SIDE) != 0) result |= Bitboard.getBB(Square.G1);
            if ((board.castlingRights & WHITE_QUEEN_SIDE) != 0) result |= Bitboard.getBB(Square.C1);
        } else {
            // black
            if ((board.castlingRights & BLACK_KING_SIDE) != 0) result |= Bitboard.getBB(Square.G8);
            if ((board.castlingRights & BLACK_QUEEN_SIDE) != 0) result |= Bitboard.getBB(Square.C8);
        }
        return result;
    }

    public static int getCastlingOOFriendlyKingSquares(Board board) {
        if (board.colorToMove == WHITE && ((board.castlingRights & WHITE_KING_SIDE) != 0)) return Square.G1;
        else if (board.colorToMove == BLACK && ((board.castlingRights & BLACK_KING_SIDE) != 0)) return Square.C1;
        return -1;
    }

    public static int getCastlingOOOFriendlyKingSquares(Board board) {
        if (board.colorToMove == WHITE && ((board.castlingRights & WHITE_QUEEN_SIDE) != 0)) return Square.G8;
        else if (board.colorToMove == BLACK && ((board.castlingRights & BLACK_QUEEN_SIDE) != 0)) return Square.C8;
        return -1;
    }

    // once the king moves player  loses all castling rights, remains on other side
    public static int getCastlingRightsKingMoved(int colorToMove, int castlingRights) {
        if (colorToMove == WHITE) return castlingRights & 0b0011;
        else return castlingRights & 0b1100;
    }

    // player loses castling rights on specific side
    public static int getCastlingRightsRookMoved(int colorToMove, int castlingRights, int rookFromSquare) {
        if (colorToMove == WHITE) {
            if (rookFromSquare == Square.H1) return castlingRights & 0b0111; // king side
            else if (rookFromSquare == Square.A1) return castlingRights & 0b1011; // queen side
        } else {
            if (rookFromSquare == Square.H8) return castlingRights & 0b1101; // king side
            else if (rookFromSquare == Square.A8) return castlingRights & 0b1110; // queen side
        }
        // rook moved from original position before, no change
        return castlingRights;
    }

    public static int getRookToSquare(int kingToSquare) {
        if (kingToSquare == Square.G1 || kingToSquare == Square.G8)
            return kingToSquare + 1; // 1 square left from final king
        else if (kingToSquare == Square.C1 || kingToSquare == Square.C8)
            return kingToSquare - 1; // 1 square right from final king
        System.out.println("invalid castling move " + kingToSquare);
        return -1;
    }

    public static int getRookFromSquare(int colorToMove, int kingToSquare) {
        if (kingToSquare == Square.G1 || kingToSquare == Square.G8) return Square.H1 + colorToMove * 56;
        else if (kingToSquare == Square.C1 || kingToSquare == Square.C8) return Square.A1 + colorToMove * 56;
        return -1;
    }

    public static boolean isPseudoLegal(Board board, int fromSquare, int toSquare) {
        int rookFromSquare = Castling.getRookFromSquare(board.colorToMove, toSquare);
        if (board.squares[fromSquare] != Piece.KING || board.squares[rookFromSquare] != Piece.ROOK) {
            return false;
        }
        return (Bitboard.BETWEEN_BB[board.kingSquare[board.colorToMove]][rookFromSquare] & board.allPieces) == 0;
    }

    public static boolean isLegal(Board board, int from, int to) {
        if (board.checkingPiecesBB != 0) {
            return false;
        }

        long kingPath = getKingPath(to);
        while (kingPath != 0) {
            // king does not move through a checked position?
            if (Check.isInCheckWithKing(Long.numberOfTrailingZeros(kingPath), board.colorToMove, board.piecesBB[board.oppositeColor], board.allPieces)) {
                return false;
            }
            kingPath &= kingPath - 1;
        }

        return true;
    }

    public static long getKingPath(int kingToSquare) {
        switch (kingToSquare) {
            case Square.G1:
                return KING_PATH[0];
            case Square.C1:
                return KING_PATH[1];
            case Square.G8:
                return KING_PATH[2];
            case Square.C8:
                return KING_PATH[3];
        }
        System.out.println("invalid castling");
        return -1;
    }
}
