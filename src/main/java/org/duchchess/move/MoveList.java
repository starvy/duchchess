package org.duchchess.move;

import org.duchchess.board.Board;
import org.duchchess.board.Piece;

/**
 * Stores every move for position
 */
public class MoveList {
    private final int[] moves = new int[256];
    private final int[] scores = new int[256];
    private int nextToMove = 0;
    private int nextToGenerate = 0;

    public void reset() {
        nextToMove = 0;
        nextToGenerate = 0;
    }

    public int nextMove() {
        return moves[nextToMove++];
    }

    public boolean hasNext() {
        return nextToGenerate != nextToMove;
    }

    public void addMove(int move) {
        addMove(move, 0);
    }

    public void addMove(int move, int score) {
        scores[nextToGenerate] = score;
        moves[nextToGenerate++] = move;
    }


    public void print(Board board) {
        for (int i = 0; i < nextToGenerate; i++) {
            if (board.isLegal(moves[i])) {
                System.out.println("move " + i + " >> " + Move.makeString(moves[i]) + " with " + board.squares[Move.getFromSquare(moves[i])] + " attacking " + Piece.FEN_CHARACTERS[Move.getAttackedPiece(moves[i])]);
            } else {
                System.out.println("move " + i + " >> " + Move.makeString(moves[i]) + " with " + board.squares[Move.getFromSquare(moves[i])] + " attacking " + Piece.FEN_CHARACTERS[Move.getAttackedPiece(moves[i])] + " ILLEGAL");
            }
        }
    }

    public void sort() {
        final int left = nextToMove;
        for (int i = left, j = i; i < nextToGenerate - 1; j = ++i) {
            final int score = scores[i + 1];
            final int move = moves[i + 1];
            while (score > scores[j]) {
                scores[j + 1] = scores[j];
                moves[j + 1] = moves[j];
                if (j-- == left) {
                    break;
                }
            }
            scores[j + 1] = score;
            moves[j + 1] = move;
        }
    }
}
