package org.duchchess.search;

public class SearchConstants {
    public static final int MAIN_SEARCH = 0;
    public static final int Q_SEARCH = 1;

    public static final int[] COLOR_MULTIPLIER = {1, -1};

    public static final int[] RAZOR_MARGIN = {0, 400};

    public static final int[] FUTILITY_PARENT_MARGIN = {0, 100, 200, 310, 430, 550, 660};
    public static final int[] FUTILITY_CHILD_MARGIN = {0, 100, 160, 230, 310, 400, 500};

    public static final int[] FUTILITY_HISTORY_MARGIN = {0, -12500, -13000, -15500, -17000, -18000, -19000};

    public static final int[][] LMR_TABLE = new int[64][64];

    static {
        // Ethereal LMR formula with depth and number of performed moves
        for (int depth = 1; depth < 64; depth++) {
            for (int moveNumber = 1; moveNumber < 64; moveNumber++) {
                LMR_TABLE[depth][moveNumber] = (int) (0.6f + Math.log(depth) * Math.log(moveNumber * 1.2f) / 2.5f);
            }
        }
    }

    public static final int LMP_DEPTH = 6;
    public static final int LMP_MULTIPLIER = 3;
    public static final int LMP_MIN_MOVES = 3;

    public static final int ASPIRATION_WINDOW_DELTA = 20;
}
