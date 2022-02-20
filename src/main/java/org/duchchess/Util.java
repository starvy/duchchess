package org.duchchess;

import org.duchchess.board.Board;

public class Util {
    public static final long[] BB_MASK = new long[64];

    static {
        for (int i = 0; i < 64; i++) {
            BB_MASK[i] = 1L << i;
        }
    }

    public static byte[] getSetBitsSlow(long value) {
        if (Long.bitCount(value) == 0) {
            return new byte[0];
        }
        byte[] setBits = new byte[Long.bitCount(value)];

        byte counter = 0;
        for (byte i = 0; i < 64; i++) {
            if ((value >> i & 1) == 1) {
                setBits[counter++] = i;
            }
        }
        return setBits;
    }

    public static void isTrue(boolean b, String msg) {
        if (!b) {
            System.out.println("value is false: " + msg);
        }
    }

    public static void isTrue(Board board, boolean b, String msg) {
        if (!b) {
            System.out.println("value is false: " + msg);
            board.print();
        }
    }
}
