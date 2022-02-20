package org.duchchess.move;

import org.duchchess.Util;
import org.duchchess.board.Bitboard;

/**
 * Magic Generator from Chess22k https://github.com/sandermvdb/chess22k
 */
public class Magic {
    public static final long[] rookMovementMasks = new long[64];
    private static final long[] bishopMovementMasks = new long[64];
    public static final long[] rookMagicNumbers = new long[]{0xa180022080400230L, 0x40100040022000L, 0x80088020001002L, 0x80080280841000L,
            0x4200042010460008L, 0x4800a0003040080L, 0x400110082041008L, 0x8000a041000880L, 0x10138001a080c010L, 0x804008200480L, 0x10011012000c0L,
            0x22004128102200L, 0x200081201200cL, 0x202a001048460004L, 0x81000100420004L, 0x4000800380004500L, 0x208002904001L, 0x90004040026008L,
            0x208808010002001L, 0x2002020020704940L, 0x8048010008110005L, 0x6820808004002200L, 0xa80040008023011L, 0xb1460000811044L, 0x4204400080008ea0L,
            0xb002400180200184L, 0x2020200080100380L, 0x10080080100080L, 0x2204080080800400L, 0xa40080360080L, 0x2040604002810b1L, 0x8c218600004104L,
            0x8180004000402000L, 0x488c402000401001L, 0x4018a00080801004L, 0x1230002105001008L, 0x8904800800800400L, 0x42000c42003810L, 0x8408110400b012L,
            0x18086182000401L, 0x2240088020c28000L, 0x1001201040c004L, 0xa02008010420020L, 0x10003009010060L, 0x4008008008014L, 0x80020004008080L,
            0x282020001008080L, 0x50000181204a0004L, 0x102042111804200L, 0x40002010004001c0L, 0x19220045508200L, 0x20030010060a900L, 0x8018028040080L,
            0x88240002008080L, 0x10301802830400L, 0x332a4081140200L, 0x8080010a601241L, 0x1008010400021L, 0x4082001007241L, 0x211009001200509L,
            0x8015001002441801L, 0x801000804000603L, 0xc0900220024a401L, 0x1000200608243L};
    private static final long[] bishopMagicNumbers = new long[]{0x2910054208004104L, 0x2100630a7020180L, 0x5822022042000000L, 0x2ca804a100200020L,
            0x204042200000900L, 0x2002121024000002L, 0x80404104202000e8L, 0x812a020205010840L, 0x8005181184080048L, 0x1001c20208010101L, 0x1001080204002100L,
            0x1810080489021800L, 0x62040420010a00L, 0x5028043004300020L, 0xc0080a4402605002L, 0x8a00a0104220200L, 0x940000410821212L, 0x1808024a280210L,
            0x40c0422080a0598L, 0x4228020082004050L, 0x200800400e00100L, 0x20b001230021040L, 0x90a0201900c00L, 0x4940120a0a0108L, 0x20208050a42180L,
            0x1004804b280200L, 0x2048020024040010L, 0x102c04004010200L, 0x20408204c002010L, 0x2411100020080c1L, 0x102a008084042100L, 0x941030000a09846L,
            0x244100800400200L, 0x4000901010080696L, 0x280404180020L, 0x800042008240100L, 0x220008400088020L, 0x4020182000904c9L, 0x23010400020600L,
            0x41040020110302L, 0x412101004020818L, 0x8022080a09404208L, 0x1401210240484800L, 0x22244208010080L, 0x1105040104000210L, 0x2040088800c40081L,
            0x8184810252000400L, 0x4004610041002200L, 0x40201a444400810L, 0x4611010802020008L, 0x80000b0401040402L, 0x20004821880a00L, 0x8200002022440100L,
            0x9431801010068L, 0x1040c20806108040L, 0x804901403022a40L, 0x2400202602104000L, 0x208520209440204L, 0x40c000022013020L, 0x2000104000420600L,
            0x400000260142410L, 0x800633408100500L, 0x2404080a1410L, 0x138200122002900L};
    private static final long[][] rookMagicMoves = new long[64][];
    private static final long[][] bishopMagicMoves = new long[64][];
    public static long[][] rookOccupancyVariations = new long[64][];
    private static long[][] bishopOccupancyVariations = new long[64][];
    public static final int[] rookShifts = new int[64];
    private static final int[] bishopShifts = new int[64];
    public static final long[] neighbourPieces = new long[64];
    public static final long[] rookMovesEmptyBoard = new long[64];
    public static final long[] bishopMovesEmptyBoard = new long[64];
    public static final long[] queenMovesEmptyBoard = new long[64];

    public static long getRookMoves(int fromIndex, long allPieces, long friendlyPieces) {
        return rookMagicMoves[fromIndex][(int) ((allPieces & rookMovementMasks[fromIndex]) * rookMagicNumbers[fromIndex] >>> rookShifts[fromIndex])]
                & ~friendlyPieces;
    }

    public static long getBishopMoves(int fromIndex, long allPieces, long friendlyPieces) {
        return bishopMagicMoves[fromIndex][(int) ((allPieces & bishopMovementMasks[fromIndex]) * bishopMagicNumbers[fromIndex] >>> bishopShifts[fromIndex])]
                & ~friendlyPieces;
    }

    public static long getRookMoves(final int fromIndex, final long allPieces) {
        return rookMagicMoves[fromIndex][(int) ((allPieces & rookMovementMasks[fromIndex]) * rookMagicNumbers[fromIndex] >>> rookShifts[fromIndex])];
    }

    public static long getBishopMoves(final int fromIndex, final long allPieces) {
        return bishopMagicMoves[fromIndex][(int) ((allPieces & bishopMovementMasks[fromIndex]) * bishopMagicNumbers[fromIndex] >>> bishopShifts[fromIndex])];
    }

    public static void init() {
        calculateMovementMasks();
        calculateBishopVariations();
        calculateRookVariations();
        generateRookMoveDatabase();
        generateBishopMoveDatabase();
        generateShiftArrays();
        testValues();
        clearValues();
        fillBlockingPieces();
        fillSlidingMovesEmptyBoard();
    }

    private static void fillSlidingMovesEmptyBoard() {
        for (int i = 0; i < 64; i++) {
            bishopMovesEmptyBoard[i] = getBishopMoves(i, 0);
            rookMovesEmptyBoard[i] = getRookMoves(i, 0);
            queenMovesEmptyBoard[i] = bishopMovesEmptyBoard[i] | rookMovesEmptyBoard[i];
        }

    }

    private static void fillBlockingPieces() {
        // fill neighbourPieces
        for (int i = 0; i < 64; i++) {
            // up
            if (i < 56) {
                neighbourPieces[i] |= Bitboard.getBB(i + 8);
                // up-right
                if ((i & 7) != 0) {
                    neighbourPieces[i] |= Bitboard.getBB(i + 7);
                }
                // up-left
                if ((i & 7) != 7) {
                    neighbourPieces[i] |= Bitboard.getBB(i + 9);
                }
            }
            // right
            if ((i & 7) != 0) {
                neighbourPieces[i] |= Bitboard.getBB(i - 1);
            }
            // left
            if ((i & 7) != 7) {
                neighbourPieces[i] |= Bitboard.getBB(i + 1);
            }
            if (i > 7) {
                neighbourPieces[i] |= Bitboard.getBB(i - 8);
                // down-right
                if ((i & 7) != 0) {
                    neighbourPieces[i] |= Bitboard.getBB(i - 9);
                }
                // down-left
                if ((i & 7) != 7) {
                    neighbourPieces[i] |= Bitboard.getBB(i - 7);
                }
            }
        }
    }

    private static void generateShiftArrays() {
        for (int i = 0; i < 64; i++) {
            rookShifts[i] = 64 - Long.bitCount(rookMovementMasks[i]);
            bishopShifts[i] = 64 - Long.bitCount(bishopMovementMasks[i]);
        }
    }

    private static void testValues() {
        // count rook moves
        int counter = 0;
        for (int i = 0; i < 64; i++) {
            counter += Long.bitCount(getRookMoves(i, 0));
        }
        if (counter != 896) {
            System.out.println("Incorrect number of rook moves found! " + counter);
        }

        // count bishop moves
        counter = 0;
        for (int i = 0; i < 64; i++) {
            counter += Long.bitCount(getBishopMoves(i, 0));
        }
        if (counter != 560) {
            System.out.println("Incorrect number of bishop moves found! " + counter);
        }
    }

    private static void clearValues() {
        rookOccupancyVariations = null;
        bishopOccupancyVariations = null;
    }

    private static void calculateRookVariations() {

        // calculate all variations
        int j = 0;
        for (int bitIndex = 0; bitIndex < 64; bitIndex++) {
            byte[] setBitsInMask = Util.getSetBitsSlow(rookMovementMasks[bitIndex]);
            int variationCount = (int) Bitboard.getBB(setBitsInMask.length);
            rookOccupancyVariations[bitIndex] = new long[variationCount];

            for (int variationIndex = 0; variationIndex < variationCount; variationIndex++) {
                byte[] setBitsInCounter = Util.getSetBitsSlow(variationIndex);
                for (j = 0; j < setBitsInCounter.length; j++) {
                    rookOccupancyVariations[bitIndex][variationIndex] |= Bitboard.getBB(setBitsInMask[setBitsInCounter[j]]);
                }
            }
        }
    }

    private static void calculateBishopVariations() {

        // calculate all variations
        int j = 0;
        for (int bitIndex = 0; bitIndex < 64; bitIndex++) {
            byte[] setBitsInMask = Util.getSetBitsSlow(bishopMovementMasks[bitIndex]);
            int variationCount = (int) Bitboard.getBB(setBitsInMask.length);
            bishopOccupancyVariations[bitIndex] = new long[variationCount];

            for (int variationIndex = 0; variationIndex < variationCount; variationIndex++) {
                byte[] setBitsInCounter = Util.getSetBitsSlow(variationIndex);
                for (j = 0; j < setBitsInCounter.length; j++) {
                    bishopOccupancyVariations[bitIndex][variationIndex] |= Bitboard.getBB(setBitsInMask[setBitsInCounter[j]]);
                }

            }
        }
    }

    private static void calculateMovementMasks() {
        int j = 0;
        // rook
        for (int bitIndex = 0; bitIndex < 64; bitIndex++) {
            long movementMaskValue = 0;

            // up
            for (j = bitIndex + 8; j < 64 - 8; j += 8) {
                movementMaskValue |= Bitboard.getBB(j);
            }
            // down
            for (j = bitIndex - 8; j >= 0 + 8; j -= 8) {
                movementMaskValue |= Bitboard.getBB(j);
            }
            // left
            for (j = bitIndex + 1; j % 8 != 0 && j % 8 != 7; j++) {
                movementMaskValue |= Bitboard.getBB(j);
            }
            // right
            for (j = bitIndex - 1; j % 8 != 7 && j % 8 != 0 && j > 0; j--) {
                movementMaskValue |= Bitboard.getBB(j);
            }

            rookMovementMasks[bitIndex] = movementMaskValue;
        }
        // bishop
        for (int bitIndex = 0; bitIndex < 64; bitIndex++) {
            long movementMaskValue = 0;

            // up-right
            for (j = bitIndex + 7; j < 64 - 7 && j % 8 != 7 && j % 8 != 0; j += 7) {
                movementMaskValue |= Bitboard.getBB(j);
            }
            // up-left
            for (j = bitIndex + 9; j < 64 - 9 && j % 8 != 7 && j % 8 != 0; j += 9) {
                movementMaskValue |= Bitboard.getBB(j);
            }
            // down-right
            for (j = bitIndex - 9; j >= 0 + 9 && j % 8 != 7 && j % 8 != 0; j -= 9) {
                movementMaskValue |= Bitboard.getBB(j);
            }
            // down-left
            for (j = bitIndex - 7; j >= 0 + 7 && j % 8 != 7 && j % 8 != 0; j -= 7) {
                movementMaskValue |= Bitboard.getBB(j);
            }

            bishopMovementMasks[bitIndex] = movementMaskValue;
        }
    }

    private static void generateRookMoveDatabase() {
        int j = 0;
        for (int bitIndex = 0; bitIndex < 64; bitIndex++) {
            rookMagicMoves[bitIndex] = new long[rookOccupancyVariations[bitIndex].length];
            for (int variationIndex = 0; variationIndex < rookOccupancyVariations[bitIndex].length; variationIndex++) {
                long validMoves = 0;
                int magicIndex = (int) ((rookOccupancyVariations[bitIndex][variationIndex] * rookMagicNumbers[bitIndex]) >>> (64
                        - Long.bitCount(rookMovementMasks[bitIndex])));

                for (j = bitIndex + 8; j < 64; j += 8) {
                    validMoves |= Bitboard.getBB(j);
                    if ((rookOccupancyVariations[bitIndex][variationIndex] & Bitboard.getBB(j)) != 0) {
                        break;
                    }
                }
                for (j = bitIndex - 8; j >= 0; j -= 8) {
                    validMoves |= Bitboard.getBB(j);
                    if ((rookOccupancyVariations[bitIndex][variationIndex] & Bitboard.getBB(j)) != 0) {
                        break;
                    }
                }
                for (j = bitIndex + 1; j % 8 != 0; j++) {
                    validMoves |= Bitboard.getBB(j);
                    if ((rookOccupancyVariations[bitIndex][variationIndex] & Bitboard.getBB(j)) != 0) {
                        break;
                    }
                }
                for (j = bitIndex - 1; j % 8 != 7 && j >= 0; j--) {
                    validMoves |= Bitboard.getBB(j);
                    if ((rookOccupancyVariations[bitIndex][variationIndex] & Bitboard.getBB(j)) != 0) {
                        break;
                    }
                }

                rookMagicMoves[bitIndex][magicIndex] = validMoves;
            }
        }
    }

    private static void generateBishopMoveDatabase() {
        int j = 0;
        for (int bitIndex = 0; bitIndex < 64; bitIndex++) {
            bishopMagicMoves[bitIndex] = new long[bishopOccupancyVariations[bitIndex].length];
            for (int variationIndex = 0; variationIndex < bishopOccupancyVariations[bitIndex].length; variationIndex++) {
                long validMoves = 0;
                int magicIndex = (int) ((bishopOccupancyVariations[bitIndex][variationIndex] * bishopMagicNumbers[bitIndex]) >>> (64
                        - Long.bitCount(bishopMovementMasks[bitIndex])));

                // up-right
                for (j = bitIndex + 7; j % 8 != 7 && j < 64; j += 7) {
                    validMoves |= Bitboard.getBB(j);
                    if ((bishopOccupancyVariations[bitIndex][variationIndex] & Bitboard.getBB(j)) != 0) {
                        break;
                    }
                }
                // up-left
                for (j = bitIndex + 9; j % 8 != 0 && j < 64; j += 9) {
                    validMoves |= Bitboard.getBB(j);
                    if ((bishopOccupancyVariations[bitIndex][variationIndex] & Bitboard.getBB(j)) != 0) {
                        break;
                    }
                }
                // down-right
                for (j = bitIndex - 9; j % 8 != 7 && j >= 0; j -= 9) {
                    validMoves |= Bitboard.getBB(j);
                    if ((bishopOccupancyVariations[bitIndex][variationIndex] & Bitboard.getBB(j)) != 0) {
                        break;
                    }
                }
                // down-left
                for (j = bitIndex - 7; j % 8 != 0 && j >= 0; j -= 7) {
                    validMoves |= Bitboard.getBB(j);
                    if ((bishopOccupancyVariations[bitIndex][variationIndex] & Bitboard.getBB(j)) != 0) {
                        break;
                    }
                }

                bishopMagicMoves[bitIndex][magicIndex] = validMoves;
            }
        }
    }

}
