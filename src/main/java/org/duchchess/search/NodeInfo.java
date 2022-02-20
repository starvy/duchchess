package org.duchchess.search;

import org.duchchess.board.Board;
import org.duchchess.move.MoveGenerator;
import org.duchchess.move.MovePicker;

/**
 * stores data specific for 1 node
 */
public class NodeInfo {
    MovePicker movePicker = new MovePicker();

    int killerMove1 = 0, killerMove2 = 0;

    MoveGenerator generator;

    public NodeInfo(MoveGenerator generator) {
        this.generator = generator;
    }

    public void setup(Board board,
                      int ttMove,
                      int threshold) {

        movePicker.setup(board, ttMove, killerMove1, killerMove2, threshold, generator);
    }

    public void setup(Board board,
                      int threshold) {
        movePicker.setup(board, 0, 0, 0, threshold, generator);
    }

    public int getPhase() {
        return movePicker.phase;
    }

    public int nextMove(boolean skipQuiets) {
        return movePicker.next(skipQuiets);
    }

    public void addKillerMove(int move) {
        if (killerMove1 != move) {
            killerMove2 = killerMove1;
            killerMove1 = move;
        }
    }
}
