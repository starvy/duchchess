package org.duchchess;

import org.duchchess.board.Board;
import org.duchchess.board.Piece;
import org.duchchess.engine.Engine;
import org.duchchess.move.Move;
import org.duchchess.move.MoveGenerator;
import org.duchchess.move.MoveList;
import org.duchchess.search.History;

/**
 * Debugging Move Generator
 */
public class Perft {
    MoveGenerator generator = new MoveGenerator();
    MoveList[] moveLists = new MoveList[128];
    public int attackCount = 0;
    public int checksCount = 0;
    public int mateCount = 0;

    public Perft() {
        for (int i = 0; i < 128; i++) {
            moveLists[i] = new MoveList();
        }
        generator.setHistory(new History());
    }

    public void printPerft(Board board, int depth) {
        long startTime = System.currentTimeMillis();
        long nodes = perft(board, depth, depth);
        long timeDelta = System.currentTimeMillis() - startTime;

        System.out.println("Nodes searched: " + nodes + " (" + (nodes * 1000 / timeDelta) + " nps)");
        System.out.println("Checks: " + checksCount);
    }

    public int perft(Board board, int depth, int startDepth) {
        int nodes = 0;

        if (board.isInCheck()) checksCount++;

        if (depth == 0) {
            return 1;
        }

        MoveList moveList = moveLists[board.moveCounter];
        moveList.reset();
        generator.generate(board, moveList);


        int movesPlayed = 0;

        while (moveList.hasNext()) {
            int move = moveList.nextMove();
            if (!board.isLegal(move)) {
                continue;
            }

            int piece = board.squares[Move.getFromSquare(move)];
            if (Engine.DEBUG && !board.isPseudoLegal(move)) {
                board.print();
                System.out.println("ilegal: " + Move.makeString(move));
            }

            board.doMove(move);
            movesPlayed++;

            long nodesForDepth = perft(board, depth - 1, startDepth);
            nodes += nodesForDepth;
            if (depth == startDepth) {
                System.out.println(Move.makeString(move) + ": " + nodesForDepth + " with " + Piece.FEN_CHARACTERS[piece]);
            }
            if (Move.getAttackedPiece(move) != 0) attackCount++;

            board.undoMove(move);
        }

        if (nodes == 0) {
            mateCount++;
        }

        return nodes;

    }
}
