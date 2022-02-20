package org.duchchess.search;

import org.duchchess.Color;
import org.duchchess.move.Move;

public class History {
    int[][][] history = new int[Color.SIZE][64][64];

    public int getHistoryScore(int color, int move) {
        int from = Move.getFromSquare(move);
        int to = Move.getToSquare(move);
        return history[color][from][to];
    }

    // HH
    public void writeToHistory(int color, int move, int score) {
        int from = Move.getFromSquare(move);
        int to = Move.getToSquare(move);

        int entry = history[color][from][to];

        int delta = Math.max(-400, Math.min(400, score));
        entry += 32 * delta - entry * Math.abs(delta) / 512;
        history[color][from][to] = entry;
    }
}
