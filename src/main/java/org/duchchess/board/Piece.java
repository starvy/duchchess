package org.duchchess.board;

public class Piece {
    public static final int NONE = 0;
    public static final int ALL = 0;

    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;

    public static final int SIZE = 7;

    public static final char[] FEN_CHARACTERS = new char[]{'-', 'P', 'N', 'B', 'R', 'Q', 'K', '-', 'p', 'n', 'b', 'r', 'q', 'k'};

    public static int getPieceFromChar(char ch) {
        int i = 0;
        while (i < FEN_CHARACTERS.length) {
            if (ch == FEN_CHARACTERS[i]) break;
            i++;
        }
        if (i > SIZE) i -= SIZE;
        return i;
    }
}
