package org.bamburov.ta.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ta4j.core.Trade;

@AllArgsConstructor
@Data
public class MyPosition {
    Trade.TradeType tradeType;
    int entryIndex;
    int exitIndex;
    double entryPrice;
    double exitPrice;

    public boolean hasProfit() {
        return tradeType == Trade.TradeType.BUY ? exitPrice > entryPrice : exitPrice < entryPrice;
    }

    public double getProfitPercentage() {
        if (tradeType == Trade.TradeType.BUY) {
            return (exitPrice / entryPrice - 1) * 100;
        } else {
            return  (entryPrice - exitPrice) / entryPrice * 100;
        }
    }

    public int getPositionLength() {
        return exitIndex - entryIndex;
    }
}
