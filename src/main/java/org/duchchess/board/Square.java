package org.duchchess.board;

import static org.duchchess.engine.Uci.getFileFromChar;
import static org.duchchess.engine.Uci.getRankFromChar;

public class Square {
    public static final int H1 = 0;
    public static final int G1 = 1;
    public static final int F1 = 2;
    public static final int E1 = 3;
    public static final int D1 = 4;
    public static final int C1 = 5;
    public static final int B1 = 6;
    public static final int A1 = 7;
    public static final int H2 = 8;
    public static final int G2 = 9;
    public static final int F2 = 10;
    public static final int E2 = 11;
    public static final int D2 = 12;
    public static final int C2 = 13;
    public static final int B2 = 14;
    public static final int A2 = 15;
    public static final int H3 = 16;
    public static final int G3 = 17;
    public static final int F3 = 18;
    public static final int E3 = 19;
    public static final int D3 = 20;
    public static final int C3 = 21;
    public static final int B3 = 22;
    public static final int A3 = 23;
    public static final int H4 = 24;
    public static final int G4 = 25;
    public static final int F4 = 26;
    public static final int E4 = 27;
    public static final int D4 = 28;
    public static final int C4 = 29;
    public static final int B4 = 30;
    public static final int A4 = 31;
    public static final int H5 = 32;
    public static final int G5 = 33;
    public static final int F5 = 34;
    public static final int E5 = 35;
    public static final int D5 = 36;
    public static final int C5 = 37;
    public static final int B5 = 38;
    public static final int A5 = 39;
    public static final int H6 = 40;
    public static final int G6 = 41;
    public static final int F6 = 42;
    public static final int E6 = 43;
    public static final int D6 = 44;
    public static final int C6 = 45;
    public static final int B6 = 46;
    public static final int A6 = 47;
    public static final int H7 = 48;
    public static final int G7 = 49;
    public static final int F7 = 50;
    public static final int E7 = 51;
    public static final int D7 = 52;
    public static final int C7 = 53;
    public static final int B7 = 54;
    public static final int A7 = 55;
    public static final int H8 = 56;
    public static final int G8 = 57;
    public static final int F8 = 58;
    public static final int E8 = 59;
    public static final int D8 = 60;
    public static final int C8 = 61;
    public static final int B8 = 62;
    public static final int A8 = 63;


    public static String makeString(int square) {
        return File.getUciChar(File.getFile(square)) + Rank.getNumberString(Rank.getRank(square) + 1);
    }

    public static int getSquareFromSAN(boolean destinationSquare, String sanMove) {
        int i = destinationSquare ? 2 : 0;
        return getFileFromChar(sanMove.charAt(i)) +
                getRankFromChar(sanMove.charAt(i + 1)) * 8;
    }
}
