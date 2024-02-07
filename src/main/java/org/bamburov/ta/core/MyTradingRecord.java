package org.bamburov.ta.core;

import java.util.List;

public class MyTradingRecord {
    String ticker;
    int stopLossPercent;
    List<MyPosition> positions;

    public MyTradingRecord(String ticker, int stopLossPercent, List<MyPosition> positions) {
        this.ticker = ticker;
        this.stopLossPercent = stopLossPercent;
        this.positions = positions;
    }

    public String getTicker() {
        return ticker;
    }

    public int getStopLossPercent() {
        return stopLossPercent;
    }

    public double getAverageProfitPercentage() {
        double sum = 0;
        for (MyPosition position : positions) {
            sum += position.getProfitPercentage();
        }
        return sum / positions.size();
    }

    public double getAveragePositionLength() {
        double sum = 0;
        for (MyPosition position : positions) {
            sum += (double) position.getPositionLength();
        }
        return sum / positions.size();
    }
}
