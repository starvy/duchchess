package org.duchchess.hash;

import org.duchchess.Castling;
import org.duchchess.Color;
import org.duchchess.board.Board;
import org.duchchess.board.Piece;

import static org.duchchess.Color.BLACK;

public class Zobrist {
    static XorRandom random = new XorRandom();
    public final static long[][][] PIECE_SQUARE_HASHES = new long[Color.SIZE][Piece.SIZE][64];
    public final static long[] CASTLING_RIGHTS_HASHES = new long[Castling.COMBINATIONS];
    public final static long[] EP_SQUARE_HASHES = new long[64];
    public static long COLOR_HASH;

    // init
    static {
        // pieces
        for (int square = 0; square < 64; square++) {
            for (int color = 0; color < Color.SIZE; color++) {
                for (int piece = 0; piece < Piece.SIZE; piece++) {
                    PIECE_SQUARE_HASHES[color][piece][square] = random.nextLong();
                }
            }

            EP_SQUARE_HASHES[square] = random.nextLong();
        }

        // castling
        for (int castlingRight = 1; castlingRight < CASTLING_RIGHTS_HASHES.length; castlingRight++) {
            CASTLING_RIGHTS_HASHES[castlingRight] = random.nextLong();
        }

        // color
        COLOR_HASH = random.nextLong();
    }

    public static long getKey(Board board) {
        long zobrist = 0L;

        if (board.colorToMove == BLACK) zobrist ^= COLOR_HASH;

        for (int color = 0; color < Color.SIZE; color++) {
            for (int piece = 0; piece < Piece.SIZE; piece++) {
                long tmpPieceBB = board.piecesBB[color][piece];
                while (tmpPieceBB != 0) {
                    int square = Long.numberOfTrailingZeros(tmpPieceBB);
                    zobrist ^= PIECE_SQUARE_HASHES[color][piece][square];
                    tmpPieceBB &= tmpPieceBB - 1;
                }
            }
        }

        zobrist ^= CASTLING_RIGHTS_HASHES[board.castlingRights];

        return zobrist;
    }

    public static long getKeyCastlingUpdate(Board board) {
        return board.zobristKey ^ CASTLING_RIGHTS_HASHES[board.castlingRights];
    }

    public static long getKeyColorChange(Board board) {
        return board.zobristKey ^ COLOR_HASH;
    }

    public static long getKeyPieceMoved(Board board, int piece, int from, int to) {
        return board.zobristKey ^ PIECE_SQUARE_HASHES[board.colorToMove][piece][from] ^
                PIECE_SQUARE_HASHES[board.colorToMove][piece][to];
    }

    public static long getKeyPieceRemoved(Board board, int piece, int square) {
        return board.zobristKey ^ PIECE_SQUARE_HASHES[board.colorToMove][piece][square];
    }


}
