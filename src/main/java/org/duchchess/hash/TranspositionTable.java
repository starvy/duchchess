package org.duchchess.hash;


import org.duchchess.board.Bitboard;
import org.duchchess.board.Board;
import org.duchchess.search.SearchControl;

import java.util.Arrays;

/**
 * KEY
 * Zobrist key: 64 bit
 * <p>
 * ENTRY
 * Move 18 bit
 * Score 32 bit
 * Depth 12 bit
 * Score Type 2 bit
 * <p>
 * 1MB = 64_000 entries
 * <p>
 * Inspired by Pirarucu Engine https://github.com/ratosh/pirarucu
 */
public class TranspositionTable {
    int sizeMB;

    int tableBits;
    int tableElements;

    int indexShift;

    long[] keys;
    long[] entries;

    public TranspositionTable(int sizeMB) {
        this.sizeMB = sizeMB;
        tableBits = calculateTableBits(sizeMB);
        tableElements = calculateTableElements(tableBits);
        indexShift = calculateIndexShift(tableBits);

        this.keys = new long[tableElements];
        this.entries = new long[tableElements];
    }

    public int getUsage() {
        int result = 0;
        for (int i = 0; i < 1000; i++) {
            if (entries[i] != 0L && keys[i] != 0L) {
                result++;
            }
        }
        return result;
    }

    public void reset() {
        Arrays.fill(keys, 0);
        Arrays.fill(entries, 0);
    }

    public long findEntry(Board board) {
        int startIndex = getIndex(board.zobristKey, indexShift);
        int maxIndex = getMaxIndex(startIndex, tableElements);
        int index = startIndex;
        long wantedKey = board.zobristKey;
        while (index < maxIndex) {
            long key = this.keys[index];
            long entry = this.entries[index];
            long savedKey = key ^ entry;

            // Unpopulated entry
            if (key == 0L && entry == 0L) {
                break;
            }
            if (wantedKey == savedKey) {
                // Found
                return entry;
            }
            index++;
        }
        return 0L;
    }

    public void saveEntry(SearchControl searchControl, Board board, int move, int score, int depth, int scoreType) {
        if (searchControl.stop) {
            return;
        }
        int startIndex = getIndex(board.zobristKey, indexShift);
        int endIndex = getMaxIndex(startIndex, tableElements);
        int index = startIndex;

        int usedIndex = -1;

        long wantedKey = board.zobristKey;

        int replacedDepth = Integer.MAX_VALUE;

        while (index < endIndex) {
            long entry = this.entries[index];
            long key = this.keys[index];
            long savedKey = key ^ entry;
            // Unpopulated entry
            if (key == 0L && entry == 0L) {
                usedIndex = index;
                break;
            }
            int savedDepth = TTEntry.getDepth(entry);

            // Update entry
            if (savedKey == wantedKey) {
                usedIndex = index;
                if (savedDepth > depth && scoreType != TTEntry.EXACT_SCORE) {
                    // Already has more calculated move
                    return;
                }
                break;
            }

            // Replace the lowest depth
            if (savedDepth < replacedDepth) {
                usedIndex = index;
                replacedDepth = savedDepth;
            }
            index++;
        }

        long entry = TTEntry.create(move, score, depth, scoreType);
        entries[usedIndex] = entry;
        keys[usedIndex] = wantedKey ^ entry;
    }

    static int calculateTableBits(int sizeMB) {
        return Long.numberOfTrailingZeros(sizeMB) + 16;
    }

    static int calculateTableElements(int tableBits) {
        if (tableBits == 0) {
            return 0;
        }
        return (int) Bitboard.getBB(tableBits);
    }

    static int calculateIndexShift(int tableBits) {
        return 64 - tableBits;
    }

    static int getIndex(long zobristKey, int indexShift) {
        return (int) (zobristKey >>> indexShift);
    }

    static int getMaxIndex(int index, int tableSize) {
        return Math.min(index + 4, tableSize);
    }
}
