package org.duchchess;

public class Color {
    public static final int[] FACTOR = new int[]{1, -1};

    public static final int WHITE = 0;
    public static final int BLACK = 1;

    public static final int SIZE = 2;

    public static int getColorFromChar(char ch) {
        return Character.isUpperCase(ch) ? 0 : 1;
    }
}
