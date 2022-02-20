package org.duchchess.engine;

import org.duchchess.ChessConstants;
import org.duchchess.Perft;
import org.duchchess.board.Board;
import org.duchchess.eval.Evaluator;
import org.duchchess.search.Search;

public class Engine {
    public static final boolean DEBUG = false;

    Board board = new Board();
    Search search = new Search();

    public void newGame() {
        board.fromFen(ChessConstants.STARTER_FEN);
    }

    public void setPosition(String[] tokens) {
        int index = 1;
        boolean moves = false;
        while (index < tokens.length) {
            switch (tokens[index]) {
                case "startpos":
                    board.fromFen(ChessConstants.STARTER_FEN);
                    index++;
                    break;
                case "fen":
                    board.fromFen(tokens[2] + " " + tokens[3] + " " + tokens[4] + " " + tokens[5]);
                    index += 4;
                    break;
                case "moves":
                    moves = true;
                    index++;
                    break;
                default:
                    if (moves) {
                        board.doMove(Uci.getMoveFromUCIString(board, tokens[index]));
                    }
                    index++;
            }
        }
    }

    public void reset() {
        search = new Search();
        board.fromFen(ChessConstants.STARTER_FEN);
    }

    public void printEval() {
        System.out.println("Pawns old: " + Evaluator.calculatePawnScores(board));
        System.out.println("Materials: " + Evaluator.calculateMaterials(board));
        System.out.println("Mobility: " + Evaluator.calculateMobility(board));
        System.out.println("Total: " + Evaluator.getScore(board));
    }

    public void go(int depth) {
        search.findBestMove(board, depth);
    }

    public void goLimitTime(int maxTime) {
        search.limitTime(maxTime);
        search.findBestMove(board, 128);
    }

    public void perft(int depth) {
        Perft perft = new Perft();
        perft.printPerft(board, depth);
    }

    public void drawBoard() {
        board.print();
    }
}
