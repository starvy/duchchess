package org.duchchess.engine;

import org.duchchess.board.*;
import org.duchchess.move.Magic;
import org.duchchess.move.Move;
import org.duchchess.move.MoveGenerator;
import org.duchchess.move.MoveList;
import org.duchchess.search.History;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Universal Chess Interface for communication with User/GUI
 */
public class Uci {
    private static final Engine engine = new Engine();

    public static void startUciLoop() {
        Magic.init();
        Scanner scanner = new Scanner(System.in);
        newGame();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            process(line);
        }

    }

    private static synchronized void process(String line) {
        String[] args = line.split(" ");
        switch (args[0]) {
            case "isready":
                System.out.println("readyok");
                break;
            case "uci":
                System.out.println("uci id duch 1.0.0");
                System.out.println("uci author Sebastian Pravda");
                System.out.println("uciok");
                break;
            case "pos":
            case "position":
                engine.setPosition(args);
                break;
            case "go":
                if (args[1].equals("depth") || args[1].equals("d")) {
                    int depth = Integer.parseInt(args[2]);
                    engine.go(depth);
                } else if (args[1].equals("movetime")) {
                    int time = Integer.parseInt(args[2]);
                    engine.goLimitTime(time);
                }
                break;
            case "ucinewgame":
                engine.reset();
                break;
            case "eval":
                engine.printEval();
                break;
            case "perft":
                engine.perft(Integer.parseInt(args[1]));
                break;
            case "d":
                engine.drawBoard();
                break;
            case "m": // prints legal moves
                printPgnMoves();
                break;
            case "quit":
                System.exit(0);
            default:
                System.out.println("Unknown command: " + line);

        }
    }

    private static void printPgnMoves() {
        MoveList moveList = new MoveList();
        MoveGenerator generator = new MoveGenerator();
        generator.setHistory(new History());
        generator.generate(engine.board, moveList);
        while (moveList.hasNext()) {
            int move = moveList.nextMove();
            String moveString = Move.makeString(move);
            System.out.printf("san %s pgn %s\n", moveString, uciMoveToSAN(engine.board, generator, moveString));
        }
    }

    private static void newGame() {
        engine.newGame();
    }

    public static int getRankFromChar(char rank) {
        return Integer.parseInt(String.valueOf(rank)) - 1;
    }

    public static int getFileFromChar(char file) {
        for (int i = 0; i < File.UCI_CHARS.length; i++) {
            if (file == File.UCI_CHARS[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int getMoveFromUCIString(Board board, String moveString) {
        int fromSquare = getFileFromChar(moveString.charAt(0)) +
                getRankFromChar(moveString.charAt(1)) * 8;
        int toSquare = getFileFromChar(moveString.charAt(2)) +
                getRankFromChar(moveString.charAt(3)) * 8;

        int move = Move.create(fromSquare, toSquare, board.squares[toSquare]);
        if (board.squares[fromSquare] == Piece.PAWN) {
            if (toSquare <= Square.A1 || toSquare >= Square.H8) {
                move = Move.createPromotion(fromSquare, toSquare, board.squares[toSquare], Piece.QUEEN);
            }
        }
        if (board.squares[fromSquare] == Piece.KING) {
            if (fromSquare == Square.E1 || fromSquare == Square.E8) {
                switch (toSquare) {
                    case Square.G1:
                    case Square.G8:
                    case Square.C1:
                    case Square.C8:
                        move = Move.createCastling(fromSquare, toSquare);
                }
            }
        }
        return move;
    }

    public static String uciMoveToSAN(Board board, MoveGenerator generator, String moveString) {
        int move = getMoveFromUCIString(board, moveString);

        List<Integer> possibleMovesToSquare = new ArrayList<>();

        int fromSquare = Move.getFromSquare(move);
        int toSquare = Move.getToSquare(move);

        int piece = board.squares[fromSquare];

        // castling has special rules
        if (piece == Piece.KING) {
            if (fromSquare == Square.E1 || fromSquare == Square.E8) {
                switch (toSquare) {
                    case Square.G1:
                    case Square.G8:
                        return "O-O";
                    case Square.C1:
                    case Square.C8:
                        return "O-O-O";
                }
            }
        }

        MoveList moveList = new MoveList();
        generator.generate(board, moveList);

        while (moveList.hasNext()) {
            int currentMove = moveList.nextMove();
            if (currentMove == move) continue; // skip original move
            if (board.squares[Move.getFromSquare(move)] == piece
                    && Move.getToSquare(currentMove) == toSquare) {
                possibleMovesToSquare.add(move);
            }
        }

        StringBuilder b = new StringBuilder();

        if (piece != Piece.PAWN) {
            // add piece character
            b.append(Piece.FEN_CHARACTERS[piece]);
        }

        // if two or more same pieces can go to the same square, distinguish by file or rank
        int fromFile = File.getFile(fromSquare);
        int fromRank = Rank.getRank(fromSquare);
        if (possibleMovesToSquare.size() > 1) {
            for (int currentMove : possibleMovesToSquare) {
                int currentFrom = Move.getFromSquare(currentMove);
                if (fromRank == Rank.getRank(currentFrom)) {
                    b.append(File.getUciChar(currentFrom));
                }
                if (fromFile == File.getFile(currentFrom)) {
                    b.append(Rank.getRank(currentFrom) + 1);
                }
            }
        }

        if (board.squares[toSquare] != 0) {
            // capture
            b.append("x");
        }

        // add destination file, rank
        b.append(File.getUciChar(File.getFile(toSquare)));
        b.append(Rank.getNumberString(Rank.getRank(toSquare) + 1));


        // add promoted piece
        if (Move.isPromotion(move)) {
            b.append("=")
                    .append(Piece.FEN_CHARACTERS[Move.getPromotedPiece(move)]);
        }

        board.doMove(move);

        // if checking move add + or mate #
        if (board.isInCheck()) {
            moveList.reset();
            generator.generate(board, moveList);
            if (moveList.hasNext()) {
                b.append("+");
            } else {
                b.append("#");
            }
        }

        board.undoMove(move);
        return b.toString();
    }
}
