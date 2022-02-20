package org.duchchess.search;

import org.duchchess.move.MoveGenerator;

public class SearchInfo {
    public long startTime = 0;
    NodeInfo[] nodeInfos = new NodeInfo[128];

    MoveGenerator generator;

    public int bestMove = 0;

    public SearchInfo(MoveGenerator generator) {
        this.generator = generator;

        for (int i = 0; i < nodeInfos.length; i++) {
            nodeInfos[i] = new NodeInfo(generator);
        }
    }

    History history = new History();

    public void writeToHistory(int color, int move, int score) {
        history.writeToHistory(color, move, score);
    }

    public NodeInfo getNodeInfo(int ply) {
        return nodeInfos[ply];
    }
}
