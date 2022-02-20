package org.duchchess.board;

public class Rank {
    public static final int SIZE = 8;

    public static int getRank(int square) {
        return square >>> 3;
    }

    public static int getRelativeRank(int color, int square) {
        return getRank(square) ^ color * 7;
    }

    public static String getNumberString(int rank) {
        return String.valueOf(rank);
    }
}
