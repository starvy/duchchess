package org.duchchess.move;

import org.duchchess.board.Board;
import org.duchchess.eval.StaticExchangeEvaluator;
import org.duchchess.search.SearchConstants;

public class MovePicker {

    public final static int PHASE_TT = 8;
    public final static int PHASE_GENERATE_CAPTURES = 7;
    public final static int PHASE_GOOD_OR_EQUAL_CAPTURES = 6;
    public final static int PHASE_KILLER1 = 5;
    public final static int PHASE_KILLER2 = 4;
    public final static int PHASE_GENERATE_QUIET = 3;
    public final static int PHASE_QUIET = 2;
    public final static int PHASE_BAD_MATERIAL_EXCHANGE = 1;
    public final static int PHASE_END = 0;

    public int phase;

    Board board;

    MoveList captureMoveList = new MoveList();
    MoveList badCaptureMoveList = new MoveList();
    MoveList quietMoveList = new MoveList();
    MoveGenerator generator;

    int threshold;

    int searchType;

    int ttMove;
    int killerMove1, killerMove2;

    public void setup(Board board,
                      int ttMove,
                      int killerMove1,
                      int killerMove2,
                      int threshold,
                      MoveGenerator generator) {
        this.board = board;

        this.ttMove = ttMove;
        this.killerMove1 = killerMove1;
        this.killerMove2 = killerMove2;

        this.phase = PHASE_TT;
        this.searchType = SearchConstants.MAIN_SEARCH;
        this.threshold = threshold;

        this.generator = generator;

        quietMoveList.reset();
        captureMoveList.reset();
        badCaptureMoveList.reset();
    }

    public int next(boolean skipQuiets) {
        while (true) {
            switch (phase) {
                case PHASE_TT:
                    phase--;
                    if (ttMove != 0) {
                        if (board.isPseudoLegal(ttMove)) {
                            return ttMove;
                        }
                    }
                    break;

                case PHASE_GENERATE_CAPTURES:
                    generator.generateAttacks(board, captureMoveList);
                    captureMoveList.sort();
                    phase--;
                    break;

                case PHASE_GOOD_OR_EQUAL_CAPTURES:
                    while (captureMoveList.hasNext()) {
                        int move = captureMoveList.nextMove();

                        if (move == ttMove) continue; // Already returned in PHASE_TT we don't want duplicates

                        if (!StaticExchangeEvaluator.see(board, move, threshold)) {
                            if (searchType == SearchConstants.MAIN_SEARCH) {
                                badCaptureMoveList.addMove(move);
                            }
                            continue;
                        }

                        if (move == killerMove1) killerMove1 = 0; // Preventing duplicates
                        if (move == killerMove2) killerMove2 = 0;

                        return move;
                    }

                    if (skipQuiets) phase = PHASE_BAD_MATERIAL_EXCHANGE;
                    else phase = PHASE_KILLER1;
                    break;

                case PHASE_KILLER1:
                    phase--;
                    if (killerMove1 != 0 &&
                            killerMove1 != ttMove &&
                            board.isPseudoLegal(killerMove1) &&
                            !skipQuiets) {
                        return killerMove1;
                    }
                    break;

                case PHASE_KILLER2:
                    phase--;
                    if (killerMove2 != 0 &&
                            killerMove2 != ttMove &&
                            board.isPseudoLegal(killerMove2) &&
                            !skipQuiets) {
                        return killerMove2;
                    }
                    break;

                case PHASE_GENERATE_QUIET:
                    phase--;
                    generator.generateQuiet(board, quietMoveList);
                    quietMoveList.sort();
                    break;

                case PHASE_QUIET:
                    while (quietMoveList.hasNext()) {
                        int move = quietMoveList.nextMove();

                        if (move == ttMove ||
                                move == killerMove1 ||
                                move == killerMove2) {
                            continue;
                        }
                        return move;
                    }
                    phase--;
                    break;

                case PHASE_BAD_MATERIAL_EXCHANGE:
                    while (badCaptureMoveList.hasNext()) {
                        int move = badCaptureMoveList.nextMove();

                        if (move == killerMove1 || // TT move already filtered in good captures
                                move == killerMove2) {
                            continue;
                        }
                        return move;
                    }
                    phase--;
                    break;

                case PHASE_END:
                    return 0;
            }
        }
    }

    /*public int nextScore() {
        return
    }*/
}
