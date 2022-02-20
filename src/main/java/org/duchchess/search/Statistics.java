package org.duchchess.search;

public class Statistics {
    public static int negamaxNodes = 0;
    public static int quiescenceNodes = 0;

    public static int betaCutoffs = 0;

    public static int prunedNodes = 0;

    public static void reset() {
        negamaxNodes = 0;
        quiescenceNodes = 0;
        betaCutoffs = 0;
        prunedNodes = 0;
    }
}
