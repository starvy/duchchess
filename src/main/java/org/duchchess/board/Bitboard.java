package org.duchchess.board;

import org.duchchess.Util;
import org.duchchess.move.Magic;

import java.util.Arrays;

public class Bitboard {
    public static final long[] RANK = new long[8];

    public static final long ALL = Long.MAX_VALUE;
    public static final long EMPTY = 0L;

    static {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                RANK[i] |= Bitboard.getBB(i * 8 + j);
            }
        }
    }

    public static final long[][] PINNED_MOVE_MASK = new long[64][64];

    static {
        final int[] DIRECTION = {-1, -7, -8, -9, 1, 7, 8, 9};

        for (int pinnedPieceIndex = 0; pinnedPieceIndex < 64; pinnedPieceIndex++) {
            for (int kingIndex = 0; kingIndex < 64; kingIndex++) {
                int correctDirection = 0;
                for (int direction : DIRECTION) {
                    if (correctDirection != 0) {
                        break;
                    }
                    int xray = kingIndex + direction;
                    while (xray >= 0 && xray < 64) {
                        if (direction == -1 || direction == -9 || direction == 7) {
                            if ((xray & 7) == 7) {
                                break;
                            }
                        }
                        if (direction == 1 || direction == 9 || direction == -7) {
                            if ((xray & 7) == 0) {
                                break;
                            }
                        }
                        if (xray == pinnedPieceIndex) {
                            correctDirection = direction;
                            break;
                        }
                        xray += direction;
                    }
                }

                if (correctDirection != 0) {
                    int xray = kingIndex + correctDirection;
                    while (xray >= 0 && xray < 64) {
                        if (correctDirection == -1 || correctDirection == -9 || correctDirection == 7) {
                            if ((xray & 7) == 7) {
                                break;
                            }
                        }
                        if (correctDirection == 1 || correctDirection == 9 || correctDirection == -7) {
                            if ((xray & 7) == 0) {
                                break;
                            }
                        }
                        PINNED_MOVE_MASK[pinnedPieceIndex][kingIndex] |= Bitboard.getBB(xray);
                        xray += correctDirection;
                    }
                }
            }
        }
    }
    // bishop moves if the board was empty

    public static final long[] BISHOP_RAYS = new long[64];
    //    static {
//        for (int i = 0; i < 64; i++) {
//            BISHOP_RAYS[i] = Magic.getBishopMoves(i, 0, 0);
//        }
//    }
    public static final long[] ROOK_RAYS = new long[64];

//    static {
//        for (int i = 0; i < 64; i++) {
//            ROOK_RAYS[i] = Magic.getRookMoves(i, 0, 0);
//        }
//    }

    public static void init() {
        for (int i = 0; i < 64; i++) {
            BISHOP_RAYS[i] = Magic.getBishopMoves(i, 0, 0);
        }

        for (int i = 0; i < 64; i++) {
            ROOK_RAYS[i] = Magic.getRookMoves(i, 0, 0);
        }
    }

    public static final long[][] BETWEEN_BB = new long[64][64];

    static {
        int i;

        // fill from->to where to > from
        for (int from = 0; from < 64; from++) {
            for (int to = from + 1; to < 64; to++) {

                // horizontal
                if (from / 8 == to / 8) {
                    i = to - 1;
                    while (i > from) {
                        BETWEEN_BB[from][to] |= Bitboard.getBB(i);
                        i--;
                    }
                }

                // vertical
                if (from % 8 == to % 8) {
                    i = to - 8;
                    while (i > from) {
                        BETWEEN_BB[from][to] |= Bitboard.getBB(i);
                        i -= 8;
                    }
                }

                // diagonal \
                if ((to - from) % 9 == 0 && to % 8 > from % 8) {
                    i = to - 9;
                    while (i > from) {
                        BETWEEN_BB[from][to] |= Bitboard.getBB(i);
                        i -= 9;
                    }
                }

                // diagonal /
                if ((to - from) % 7 == 0 && to % 8 < from % 8) {
                    i = to - 7;
                    while (i > from) {
                        BETWEEN_BB[from][to] |= Bitboard.getBB(i);
                        i -= 7;
                    }
                }
            }
        }

        // fill from->to where to < from
        for (int from = 0; from < 64; from++) {
            for (int to = 0; to < from; to++) {
                BETWEEN_BB[from][to] = BETWEEN_BB[to][from];
            }
        }
    }

    public static long getBB(int square) {
        return Util.BB_MASK[square];
    }

    public static void draw(long bb) {
        int[][] consoleBoard = new int[8][8];
        for (int i = 0; i < 8; i++) {
            Arrays.fill(consoleBoard[i], 0);
        }
        while (bb != 0) {
            int square = Long.numberOfTrailingZeros(bb);
            consoleBoard[Rank.getRank(square)][File.getFile(square)] = 1;
            bb &= bb - 1;
        }

        System.out.println("+---+---+---+---+---+---+---+---+");
        for (int i = 7; i >= 0; i--) {
            for (int j = 7; j >= 0; j--) {
                if (consoleBoard[i][j] != 0) {
                    System.out.print("| X ");
                } else {
                    System.out.print("|   ");
                }
            }
            System.out.println("|");
            System.out.println("+---+---+---+---+---+---+---+---+");
        }
    }

    public static String makeString(long bb) {
        StringBuilder builder = new StringBuilder();

        int[][] consoleBoard = new int[8][8];
        for (int i = 0; i < 8; i++) {
            Arrays.fill(consoleBoard[i], 0);
        }
        while (bb != 0) {
            int square = Long.numberOfTrailingZeros(bb);
            consoleBoard[Rank.getRank(square)][File.getFile(square)] = 1;
            bb &= bb - 1;
        }

        builder.append("+---+---+---+---+---+---+---+---+\n");
        for (int i = 7; i >= 0; i--) {
            for (int j = 7; j >= 0; j--) {
                if (consoleBoard[i][j] != 0) {
                    builder.append("| X ");
                } else {
                    builder.append("|   ");
                }
            }
            builder.append("|\n");
            builder.append("+---+---+---+---+---+---+---+---+\n");
        }

        return builder.toString();
    }
}
