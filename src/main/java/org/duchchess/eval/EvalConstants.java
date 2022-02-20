package org.duchchess.eval;

import org.duchchess.Color;
import org.duchchess.board.Piece;

import static org.duchchess.Color.BLACK;
import static org.duchchess.Color.WHITE;

public class EvalConstants {
    public static final short MATE_SCORE = Short.MIN_VALUE + 1;
    public static final short DRAW_SCORE = 0;

    public static final int[] MATERIAL_VALUES = new int[]{0, 100, 300, 325, 550, 900, 0};

    public static final int[][] PAWN_POSITION_SCORES = new int[2][64];
    public static final int[][] KNIGHT_POSITION_SCORES = new int[2][64];
    public static final int[][] BISHOP_POSITION_SCORES = new int[2][64];
    public static final int[][][] POSITION = new int[Piece.BISHOP + 1][2][64];

    public static final int[][] ENDGAME_KING_POSITION_SCORES = new int[2][64];


    public static final int[] KNIGHT_MOBILITY = new int[]{-15, -5, -1, 2, 5, 7, 9, 11, 13};
    public static final int[] BISHOP_MOBILITY = new int[]{-15, -11, -6, -1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12};
    public static final int[] ROOK_MOBILITY = new int[]{-10, -4, -2, 0, 2, 3, 4, 5, 6, 8, 8, 9, 10, 11, 12};
    public static final int[] QUEEN_MOBILITY = new int[]{-10, -6, -5, -4, -2, -2, -1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 6, 6, 6,
            7, 7, 8, 8, 9, 9, 10, 10, 10};
    public static final int[][] MOBILITY = new int[][]{
            new int[]{0},
            new int[]{0}, // empty
            KNIGHT_MOBILITY,
            BISHOP_MOBILITY,
            ROOK_MOBILITY,
            QUEEN_MOBILITY
    };

    public static final long[] FILE_MASKS = new long[]{
            0x101010101010101L, 0x202020202020202L, 0x404040404040404L, 0x808080808080808L, 0x1010101010101010L,
            0x2020202020202020L, 0x4040404040404040L, 0x8080808080808080L
    };

    public static final long[] ADJACENT_FILE_MASKS = new long[]{
            0x202020202020202L, 0x505050505050505L, 0xa0a0a0a0a0a0a0aL, 0x1414141414141414L,
            0x2828282828282828L, 0x5050505050505050L, 0xa0a0a0a0a0a0a0a0L, 0x4040404040404040L
    };

    static {
        PAWN_POSITION_SCORES[WHITE] = new int[]{
                0, 0, 0, 0, 0, 0, 0, 0,
                80, 90, 90, 90, 90, 90, 90, 80,
                30, 50, 60, 60, 60, 60, 50, 30,
                -5, 5, 10, 30, 30, 10, 5, -5,
                -10, 0, 0, 25, 25, 0, 0, -10,
                -5, -5, 0, 10, 10, 0, -5, -5,
                -5, 0, 10, -30, -30, 10, 0, -5,
                0, 0, 0, 0, 0, 0, 0, 0
        };
        KNIGHT_POSITION_SCORES[WHITE] = new int[]{
                -50, -30, -30, -30, -30, -30, -30, -50,
                -40, -20, 0, 0, 0, 0, -20, -40,
                -30, 0, 10, 15, 15, 10, 0, -30,
                -30, 5, 20, 25, 25, 20, 5, -30,
                -30, 0, 20, 25, 25, 20, 0, -30,
                -30, 5, 20, 20, 20, 20, 5, -30,
                -40, -20, 0, 10, 10, 0, -20, -40,
                -50, -10, -30, -30, -30, -30, -10, -50,
        };
        BISHOP_POSITION_SCORES[WHITE] = new int[]{
                -20, -10, -10, -10, -10, -10, -10, -20,
                -10, 0, 0, 0, 0, 0, 0, -10,
                -10, 0, 5, 10, 10, 5, 0, -10,
                -10, 5, 5, 10, 10, 5, 5, -10,
                -10, 0, 10, 15, 15, 10, 0, -10,
                -10, 10, 10, 15, 15, 10, 10, -10,
                -10, 5, 0, 0, 0, 0, 5, -10,
                -20, -10, -20, -10, -10, -20, -10, -20
        };
        ENDGAME_KING_POSITION_SCORES[WHITE] = new int[]{
                -70, -30, -30, -30, -30, -30, -30, -70,
                -40, -20, 0, 0, 0, 0, -20, -40,
                -30, 0, 10, 15, 15, 10, 0, -30,
                -30, 5, 20, 25, 25, 20, 5, -30,
                -30, 0, 20, 25, 25, 20, 0, -30,
                -30, 5, 20, 20, 20, 20, 5, -30,
                -40, -20, 0, 10, 10, 0, -20, -40,
                -70, -30, -10, -30, -30, -30, -10, -70,
        };
    }

    static {
        // position scores setup for black
        for (int square = 0; square < 64; square++) {
            PAWN_POSITION_SCORES[BLACK][square] = PAWN_POSITION_SCORES[WHITE][63 - square];
            KNIGHT_POSITION_SCORES[BLACK][square] = KNIGHT_POSITION_SCORES[WHITE][63 - square];
            BISHOP_POSITION_SCORES[BLACK][square] = BISHOP_POSITION_SCORES[WHITE][63 - square];

            for (int color = 0; color < Color.SIZE; color++) {
                POSITION[Piece.PAWN][color][square] = PAWN_POSITION_SCORES[color][square];
                POSITION[Piece.KNIGHT][color][square] = KNIGHT_POSITION_SCORES[color][square];
                POSITION[Piece.BISHOP][color][square] = BISHOP_POSITION_SCORES[color][square];
            }

            ENDGAME_KING_POSITION_SCORES[BLACK][square] = ENDGAME_KING_POSITION_SCORES[WHITE][63 - square];
        }
    }
}
