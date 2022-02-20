package org.duchchess.hash;

import org.duchchess.Util;

/**
 * ENTRY
 * Move 18 bit
 * Score 16 bit
 * Depth 12 bit
 * Score Type 2 bit
 */
public class TTEntry {
    public static final int EXACT_SCORE = 0;
    public static final int SCORE_LOWER = 1;
    public static final int SCORE_UPPER = 2;


    static long DEPTH_SHIFT = 18; // 12 bits
    static long TYPE_SHIFT = 30; // 2 bits
    static long SCORE_SHIFT = 32;

    static long MOVE_MASK = 0b111111_111111_111111;
    static long SCORE_MASK = Short.MAX_VALUE;
    static long DEPTH_MASK = 0b1111_1111_1111;
    static long SCORE_TYPE_MASK = 0b11;

    public static long create(int move, int score, int depth, int scoreType) {
//        System.out.println("creating " + score);
        Util.isTrue(score >= Short.MIN_VALUE && score <= Short.MAX_VALUE, "score");
        return move | ((long) score << SCORE_SHIFT) | ((long) depth << DEPTH_SHIFT) | ((long) scoreType << TYPE_SHIFT);
    }

    public static int getScore(long entry) {
        return (int) (entry >> SCORE_SHIFT);
    }

    public static int getScore(long entry, int ply) {
        int score = getScore(entry);
//        System.out.println(score);
        /*if (score == -EvalConstants.MATE_SCORE) {
            return score + ply;
        } else if (score == EvalConstants.MATE_SCORE){
            return score - ply;
        }*/
        Util.isTrue(score >= Short.MIN_VALUE && score <= Short.MAX_VALUE, "score");
        return score;
    }

    public static int getMove(long entry) {
        return (int) (entry & MOVE_MASK);
    }

    public static int getDepth(long entry) {
        return (int) ((entry >>> DEPTH_SHIFT) & DEPTH_MASK);
    }

    public static int getScoreType(long entry) {
        return (int) ((entry >>> TYPE_SHIFT) & SCORE_TYPE_MASK);
    }

    public static boolean canRefineEval(long entry, int eval, int score) {
        int scoreType = getScoreType(entry);
        if (entry != 0) {
            return scoreType == EXACT_SCORE ||
                    scoreType == SCORE_UPPER && score < eval ||
                    scoreType == SCORE_LOWER && score > eval;
        }
        return false;
    }
}
