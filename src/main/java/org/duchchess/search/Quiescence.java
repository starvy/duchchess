package org.duchchess.search;

import org.duchchess.board.Board;
import org.duchchess.board.Piece;
import org.duchchess.eval.Evaluator;
import org.duchchess.eval.StaticExchangeEvaluator;
import org.duchchess.move.Move;

import static org.duchchess.eval.EvalConstants.MATERIAL_VALUES;
import static org.duchchess.search.SearchConstants.COLOR_MULTIPLIER;

public class Quiescence {
    /**
     * @return Position score
     */
    public static int search(Board board, SearchInfo searchInfo, int ply, int alpha, int beta) {
        Statistics.quiescenceNodes++;

        int eval = Evaluator.getScore(board) * COLOR_MULTIPLIER[board.colorToMove];

        if (ply >= 128) {
            return eval;
        }

        // Beta cutoff
        if (eval >= beta) {
            return eval;
        }

        alpha = Math.max(alpha, eval);

        NodeInfo nodeInfo = searchInfo.getNodeInfo(ply);
        nodeInfo.setup(board, 1);
        while (true) {
            int move = nodeInfo.nextMove(true);
            if (move == 0) {
                break;
            }

            // Skips illegal moves
            if (!board.isLegal(move)) {
                continue;
            }

            // Skip under promotions
            if (Move.isPromotion(move) && Move.getPromotedPiece(move) != Piece.QUEEN) {
                continue;
            }

            // Futility pruning
            if (eval + getMoveValue(board, move) * 1.3 < alpha) {
                continue;
            }


            // SEE pruning
            if (!StaticExchangeEvaluator.see(board, move, 0)) {
                continue;
            }

            board.doMove(move);

            int value = -search(board, searchInfo, ply + 1, -beta, -alpha);

            board.undoMove(move);

            if (value > alpha) {
                alpha = value;
            }

            if (alpha >= beta) {
                return alpha;
            }
        }
        return alpha;
    }

    /**
     * @return Predicted Move Value
     */
    private static int getMoveValue(Board board, int move) {
        if (Move.isPassant(move)) {
            return MATERIAL_VALUES[Piece.PAWN];
        }
        if (Move.isPromotion(move)) {
            return MATERIAL_VALUES[board.squares[Move.getToSquare(move)]] -
                    MATERIAL_VALUES[Piece.PAWN] +
                    MATERIAL_VALUES[Move.getPromotedPiece(move)];
        }
        return MATERIAL_VALUES[board.squares[Move.getToSquare(move)]];
    }
}
