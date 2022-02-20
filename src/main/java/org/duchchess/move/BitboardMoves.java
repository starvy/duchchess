package org.duchchess.move;

import org.duchchess.board.Bitboard;
import org.duchchess.board.File;

import static org.duchchess.Color.BLACK;
import static org.duchchess.Color.WHITE;
import static org.duchchess.board.Piece.*;

public class BitboardMoves {
    // public static final long[][] PAWN
    public static final int UP = 8;
    public static final int DOWN = -8;
    public static final int LEFT = -1;
    public static final int RIGHT = -1;

    public static final int[] PAWN_UP = new int[]{8, -8};

    public static long pawnMove(int color, long bb) {
        if (color == WHITE && Long.numberOfTrailingZeros(bb) <= 56) {
            return bb << UP;
        }
        if (Long.numberOfTrailingZeros(bb) >= 8) {
            return bb >>> 8;
        }
        return 0L;
    }

    public static long doublePawnMove(int color, int square, long pawnOne) {
        if (pawnOne != 0) {
            return StaticMoves.PAWN_DOUBLE[color][square];
        }
        return 0L;
    }

    public static long moves(int square, int piece, long allPieces) {
        switch (piece) {
            case PAWN:
                return pawnMove(piece, Bitboard.getBB(square));
            case KNIGHT:
                return knightMoves(square);
            case BISHOP:
                return bishopMoves(square, allPieces);
            case ROOK:
                return rookMoves(square, allPieces);
            case QUEEN:
                return rookMoves(square, allPieces) | bishopMoves(square, allPieces);
            case KING:
                return kingMoves(square);
        }
        return 0L;
    }

    public static long knightMoves(int square) {
        return StaticMoves.KNIGHT_MOVES[square];
    }

    public static long kingMoves(int square) {
        return StaticMoves.KING_MOVES[square];
    }

    public static long directPieceMoves(int square, int piece) {
        if (piece == KNIGHT) return knightMoves(square);
        else return kingMoves(square);
    }

    public static long slidingPieceMoves(int square, int piece, long allPieces, long friendlyPieces) {
        if (piece == BISHOP) return bishopMoves(square, allPieces, friendlyPieces);
        else return rookMoves(square, allPieces, friendlyPieces);
    }

    public static long bishopMoves(int square, long allPieces, long friendlyPieces) {
        return Magic.getBishopMoves(square, allPieces, friendlyPieces);
    }

    public static long bishopMoves(int square, long allPieces) {
        return Magic.getBishopMoves(square, allPieces);
    }

    public static long rookMoves(int square, long allPieces, long friendlyPieces) {
        return Magic.getRookMoves(square, allPieces, friendlyPieces);
    }

    public static long rookMoves(int square, long allPieces) {
        return Magic.getRookMoves(square, allPieces);
    }

    public static long pawnAttacks(int color, int square) {
        return StaticMoves.PAWN_ATTACKS[color][square];
    }

    static class StaticMoves {
        public static final long[] KING_MOVES = new long[64];

        static {
            for (int i = 0; i < KING_MOVES.length; i++) {
                int col = File.getFile(i);

                KING_MOVES[i] = Bitboard.getBB(i) << 8;
                KING_MOVES[i] |= Bitboard.getBB(i) >>> 8;
                if (col > 0) {
                    KING_MOVES[i] |= Bitboard.getBB(i) >>> 1;
                    KING_MOVES[i] |= Bitboard.getBB(i) >>> 1 + 8;
                    KING_MOVES[i] |= Bitboard.getBB(i) << 7;
                }
                if (col < 7) {
                    KING_MOVES[i] |= Bitboard.getBB(i) << 1;
                    KING_MOVES[i] |= Bitboard.getBB(i) << 1 + 8;
                    KING_MOVES[i] |= Bitboard.getBB(i) >>> 7;
                }
            }
        }

        public static final long[] KNIGHT_MOVES = new long[64];

        static {
            for (int i = 0; i < 64; i++) {
                int col = 7 - File.getFile(i);
                if (col > 0) {
                    // 2 nahoru doleva
                    KNIGHT_MOVES[i] = Bitboard.getBB(i) << 9 + 8;
                    KNIGHT_MOVES[i] |= Bitboard.getBB(i) >>> 7 + 8;
                    if (col > 1) {
                        // 2 doleva nahoru
                        KNIGHT_MOVES[i] |= Bitboard.getBB(i) << 10;
                        KNIGHT_MOVES[i] |= Bitboard.getBB(i) >>> 6;
                    }
                }
                if (col < 7) {
                    // 2 nahoru dolu
                    KNIGHT_MOVES[i] |= Bitboard.getBB(i) >>> 9 + 8;
                    KNIGHT_MOVES[i] |= Bitboard.getBB(i) << 7 + 8;
                    if (col < 6) {
                        // 2 doprava dolu
                        KNIGHT_MOVES[i] |= Bitboard.getBB(i) >>> 10;
                        KNIGHT_MOVES[i] |= Bitboard.getBB(i) << 6;
                    }
                }
            }
        }

        public static final long[][] PAWN_DOUBLE = new long[2][64];

        static {
            for (int i = 8; i < 16; i++) {
                PAWN_DOUBLE[WHITE][i] = Bitboard.getBB(i) << 16;
            }
            // black
            for (int i = 48; i < 56; i++) {
                PAWN_DOUBLE[BLACK][i] = Bitboard.getBB(i) >> 16;
            }
        }

        public static final long[][] PAWN_ATTACKS = new long[2][64];

        static {
            for (int i = 0; i < PAWN_ATTACKS[WHITE].length; i++) {
                int file = File.getFile(i);
                if (file > 0) {
                    PAWN_ATTACKS[WHITE][i] = Bitboard.getBB(i) << 7;
                }
                if (file < 7) {
                    PAWN_ATTACKS[WHITE][i] |= Bitboard.getBB(i) << 9;
                }
            }

            // black
            for (int i = 0; i < PAWN_ATTACKS[BLACK].length; i++) {
                int file = File.getFile(i);
                if (file > 0) {
                    PAWN_ATTACKS[BLACK][i] = Bitboard.getBB(i) >> 9;
                }
                if (file < 7) {
                    PAWN_ATTACKS[BLACK][i] |= Bitboard.getBB(i) >> 7;
                }
            }
        }

    }

}
