package org.duchchess.search;

public class SearchControl {
    public boolean hasFixedTime = false;

    private int maxTime;
    public boolean stop = false;

    public boolean stopNow(long startTime, int ply) {
        if (!hasFixedTime) return false;
        if ((System.currentTimeMillis() - startTime) >= maxTime && ply > 0) {
            stop = true;
        }
        return stop;
    }

    public void reset() {
        stop = false;
        maxTime = 0;
        hasFixedTime = false;
    }

    public void limitTime(int maxTime) {
        this.hasFixedTime = true;
        this.maxTime = maxTime;
    }
}
