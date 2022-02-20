package org.duchchess.search;

import org.duchchess.board.Board;
import org.duchchess.eval.EvalConstants;
import org.duchchess.eval.Evaluator;
import org.duchchess.eval.StaticExchangeEvaluator;
import org.duchchess.hash.TTEntry;
import org.duchchess.hash.TranspositionTable;
import org.duchchess.move.Move;
import org.duchchess.move.MoveGenerator;
import org.duchchess.move.MovePicker;

import static org.duchchess.search.SearchConstants.*;

public class Search {
    public static final int INFINITY = 222_222_222;
    MoveGenerator generator = new MoveGenerator();

    TranspositionTable tt = new TranspositionTable(256);

    SearchControl searchControl = new SearchControl();

    public Search() {
    }

    public void limitTime(int maxTime) {
        searchControl.limitTime(maxTime);
    }

    /**
     * Find best move and print
     */
    public void findBestMove(Board board, int maxDepth) {
        Statistics.reset();
        searchControl.stop = false;

        int depth = 1;

        long startTime = System.currentTimeMillis();

        while (depth <= maxDepth) {
            SearchInfo searchInfo = new SearchInfo(generator);
            searchInfo.startTime = startTime;

            generator.setHistory(searchInfo.history);

            search(board, searchInfo, depth, 0, -INFINITY, INFINITY, false);

            long endTime = System.currentTimeMillis();
            long timeDelta = endTime - startTime;
            if (timeDelta <= 0) {
                timeDelta = 1;
            }

            int score = getScore(board);
            String pv = getPv(board, depth);


            System.out.println("info depth " + depth +
                    " score cp " + score +
                    " nodes " + (Statistics.negamaxNodes + Statistics.quiescenceNodes) +
                    " nps " + (Statistics.negamaxNodes + Statistics.quiescenceNodes) * 1000L / timeDelta +
                    " time " + timeDelta +
                    " pv " + pv);


            // No Time left
            if (searchControl.stopNow(startTime, 0)) {
                System.out.println("bestmove " + Move.makeString(getBestmove(board)));
                return;
            }

            // Reached maximum depth
            if (depth == maxDepth) {
                System.out.println("bestmove " + Move.makeString(getBestmove(board)));
            }

            depth++;
        }

    }

    /**
     * @return String containing best moves for each ply from transposition table
     */
    public String getPv(Board board, int depth) {
        int bestMove;
        long entry = tt.findEntry(board);
        if (entry == 0 || depth == 0)
            return "";
        else {
            bestMove = TTEntry.getMove(entry);
            String pV = "";
            if (bestMove != 0) {
                board.doMove(bestMove);
                pV = Move.makeString(bestMove) + " " + getPv(board, depth - 1);
                board.undoMove(bestMove);
            }
            return pV;
        }
    }

    public int getScore(Board board) {
        return TTEntry.getScore(tt.findEntry(board));
    }

    public int getBestmove(Board board) {
        return TTEntry.getMove(tt.findEntry(board));
    }

    public int search(Board board, SearchInfo searchInfo, int depth, int ply, int alpha, int beta, boolean skipNullMove) {
        if (searchControl.stopNow(searchInfo.startTime, ply)) {
            return 0;
        }
        Statistics.negamaxNodes++;

        // Mate distance pruning
        alpha = Math.max(alpha, EvalConstants.MATE_SCORE + ply);
        beta = Math.min(beta, -EvalConstants.MATE_SCORE - ply - 1);
        if (alpha >= beta) {
            return alpha;
        }

        // Transposition Table
        long foundEntry = tt.findEntry(board);
        int ttMove = TTEntry.getMove(foundEntry);
        int score = TTEntry.getScore(foundEntry, ply);
        if (foundEntry != 0) {
            int scoreType = TTEntry.getScoreType(foundEntry);
            int ttDepth = TTEntry.getDepth(foundEntry);


            if (ttDepth >= depth) {
                if (scoreType == TTEntry.EXACT_SCORE) {
                    if (score == EvalConstants.MATE_SCORE) {
                        return score - ply;
                    } else if (score == -EvalConstants.MATE_SCORE) {
                        return score + ply;
                    }
                    return score;
                }
                if (scoreType == TTEntry.SCORE_LOWER) {
                    if (score >= beta) {
                        return score;
                    }
                } else if (scoreType == TTEntry.SCORE_UPPER) {
                    if (score <= alpha) {
                        return score;
                    }
                }
            }
        }

        if (depth <= 0) {
            return Quiescence.search(board, searchInfo, ply, alpha, beta);
        }
        boolean inCheck = board.isInCheck();
        boolean isPv = alpha != beta - 1;
        boolean prunable = !inCheck && !isPv;

        int eval = Evaluator.getScore(board) * COLOR_MULTIPLIER[board.colorToMove];

        // Pruning
        if (prunable) {

            if (TTEntry.canRefineEval(foundEntry, eval, score)) {
                eval = score;
            }

            // Futility pruning
            if (depth < SearchConstants.FUTILITY_CHILD_MARGIN.length && eval < 10000) {
                if (eval - SearchConstants.FUTILITY_CHILD_MARGIN[depth] >= beta) {
                    return eval;
                }
            }

            // Razoring
            if (depth < SearchConstants.RAZOR_MARGIN.length) {
                int razorAlpha = alpha - RAZOR_MARGIN[depth];
                if (eval < razorAlpha) {
                    int razorSearchValue = search(board, searchInfo, 0, ply, razorAlpha, razorAlpha + 1, false);
                    if (razorSearchValue < razorAlpha) {
                        return razorSearchValue;
                    }
                }
            }

            // Null Move Pruning
            if (!skipNullMove &&
                    depth > 1 &&
                    eval >= beta &&
                    board.hasNonPawnMaterial()) {

                board.doNullMove();
                int reduction = 3 + depth / 3;
                int value = -search(board, searchInfo, depth - reduction, ply + 1, -beta, -beta + 1, true);
                board.undoNullMove();

                if (value >= beta) {
                    return value;
                }
            }


        }

        NodeInfo nodeInfo = searchInfo.getNodeInfo(ply);
        nodeInfo.setup(board, ttMove, 1);

        int scoreType = TTEntry.SCORE_UPPER;
        int bestMoveThisNode = 0;
        int movesPerformed = 0;

        boolean skipQuiets = false;

        while (true) {
            int move = nodeInfo.nextMove(skipQuiets);
            if (move == 0) {
                break;
            }

            if (!board.isLegal(move)) {
                continue;
            }

            boolean isCapture = board.squares[Move.getToSquare(move)] != 0;

            if (prunable && !Move.isPromotion(move) && movesPerformed > 0) {

                // Late move pruning
                if (depth <= LMP_DEPTH && movesPerformed >= depth * LMP_MULTIPLIER + LMP_MIN_MOVES) {
                    skipQuiets = true;
                }

                // Futility pruning
                if (!isCapture && depth < FUTILITY_PARENT_MARGIN.length) {
                    int futilityValue = eval + SearchConstants.FUTILITY_PARENT_MARGIN[depth];
                    if (futilityValue <= alpha) {
                        skipQuiets = true;
                    }
                }
                if (!isCapture && depth < FUTILITY_HISTORY_MARGIN.length &&
                        searchInfo.history.getHistoryScore(board.colorToMove, move) < FUTILITY_HISTORY_MARGIN[depth]) {
                    skipQuiets = true;
                }

                // Static Exchange Evaluation
                if (depth < 7 && nodeInfo.getPhase() < MovePicker.PHASE_GOOD_OR_EQUAL_CAPTURES &&
                        !StaticExchangeEvaluator.see(board, move, -20 * (depth * depth))) {
                    continue;
                }
            }

            board.doMove(move);
            movesPerformed++;

            int value;
            if (board.isDraw()) {
                // Position is draw
                value = EvalConstants.DRAW_SCORE;
            } else {
                value = alpha + 1;

                // Reductions
                int reduction = 1;
                if (depth > 2 && movesPerformed > 1 && Move.isQuiet(move) && !Move.isPromotion(move)) {
                    reduction = LMR_TABLE[Math.min(depth, 63)][Math.min(movesPerformed, 63)];

                    if (move == nodeInfo.killerMove1 || move == nodeInfo.killerMove2) {
                        reduction -= 1;
                    }

                    if (!isPv) {
                        reduction += 1;
                    }

                    reduction = Math.min(depth - 1, Math.max(reduction, 1));
                }

                // Late Move Reductions
                if (reduction != 1) {
                    value = -search(board, searchInfo, depth - reduction, ply + 1, -alpha - 1, -alpha, false);
                }

                // Principal Variation Search
                if (value > alpha && movesPerformed > 1) {
                    value = -search(board, searchInfo, depth - 1, ply + 1, -alpha - 1, -alpha, false);
                }

                // Normal Search
                if (value > alpha) {
                    value = -search(board, searchInfo, depth - 1, ply + 1, -beta, -alpha, false);
                }
            }

            board.undoMove(move);

            // Beta cutoffs
            if (value >= beta) {
                Statistics.betaCutoffs++;
                if (Move.isQuiet(move)) {
                    nodeInfo.addKillerMove(move);
                    searchInfo.writeToHistory(board.colorToMove, move, depth * depth);
                }
                tt.saveEntry(searchControl, board, move, beta, depth, TTEntry.SCORE_LOWER);
                return beta;
            }

            if (Move.isQuiet(move)) {
                searchInfo.writeToHistory(board.colorToMove, move, -(depth * depth));
            }

            if (value > alpha) {
                // New bestmove
                alpha = value;
                scoreType = TTEntry.EXACT_SCORE;

                bestMoveThisNode = move;

                if (ply == 0) {
                    searchInfo.bestMove = bestMoveThisNode;
                }
            }
        }
        if (movesPerformed == 0) {
            if (board.isInCheck()) {
                // Checkmate
                tt.saveEntry(searchControl, board, bestMoveThisNode, EvalConstants.MATE_SCORE, depth, TTEntry.EXACT_SCORE);
                return EvalConstants.MATE_SCORE + ply;
            } else {
                // Stalemate
                tt.saveEntry(searchControl, board, bestMoveThisNode, 0, depth, TTEntry.EXACT_SCORE);
                return 0;
            }
        }

        // Save TT entry
        tt.saveEntry(searchControl, board, bestMoveThisNode, alpha, depth, scoreType);

        return alpha;
    }
}
