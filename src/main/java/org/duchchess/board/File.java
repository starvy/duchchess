package org.duchchess.board;

public class File {
    public static final char[] UCI_CHARS = new char[]{'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'};

    public static final int A = 7;
    public static final int B = 6;
    public static final int C = 5;
    public static final int D = 4;
    public static final int E = 3;
    public static final int F = 2;
    public static final int G = 1;
    public static final int H = 0;

    public static int getFile(int square) {
        return square % 8;
    }

    public static char getUciChar(int file) {
        return UCI_CHARS[file];
    }
}

