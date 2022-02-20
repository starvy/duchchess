package org.duchchess.hash;

/**
 * Pseudo random number generator from Pirarucu Engine https://github.com/ratosh/pirarucu
 */
public class XorRandom {
    private static final long multiplier = 0x2545F4914F6CDD1DL;
    private long seed = 1070372L;

    public long nextLong() {
        seed ^= seed >>> 12;
        seed ^= seed << 25;
        seed ^= seed >>> 27;
        return seed * multiplier;
    }
}
