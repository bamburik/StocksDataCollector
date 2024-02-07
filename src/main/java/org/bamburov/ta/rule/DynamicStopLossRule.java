package org.bamburov.ta.rule;

import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;

public class DynamicStopLossRule extends AbstractRule {

    private final Num HUNDRED;

    private final HighPriceIndicator highPrice;

    private final LowPriceIndicator lowPrice;

    private final Num lossPercentage;

    public DynamicStopLossRule(HighPriceIndicator highPrice, LowPriceIndicator lowPriceIndicator, Num lossPercentage) {
        this.highPrice = highPrice;
        this.lossPercentage = lossPercentage;
        this.lowPrice = lowPriceIndicator;
        this.HUNDRED = highPrice.numOf(100);
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        boolean satisfied = false;
        // No trading history or no position opened, no loss
        if (tradingRecord != null) {
            Position currentPosition = tradingRecord.getCurrentPosition();
            if (currentPosition.isOpened()) {
                int entryIndex = currentPosition.getEntry().getIndex();

                if (currentPosition.getEntry().isBuy()) {
                    satisfied = isBuyStopSatisfied(entryIndex, i);
                } else {
                    satisfied = isSellStopSatisfied(entryIndex, i);
                }
            }
        }
        if (satisfied) {
            System.out.println("DynamicStopLossRule is satisfied for index " + i);
        }
        return satisfied;
    }

    private boolean isBuyStopSatisfied(int entryIndex, int currentIndex) {
        Num lossRatioThreshold = HUNDRED.minus(lossPercentage).dividedBy(HUNDRED);

        Num maxHigh = highPrice.numOf(0);
        for(int i = entryIndex; i <= currentIndex; i++) {
            if (highPrice.getValue(i).isGreaterThan(maxHigh)){
                maxHigh = highPrice.getValue(i);
            }
        }

        Num threshold = maxHigh.multipliedBy(lossRatioThreshold);
        boolean condition = lowPrice.getValue(currentIndex).isLessThanOrEqual(threshold);
        if (condition) {
            System.out.println("reached threshold: " + threshold.doubleValue());
        }
        return condition;
    }

    private boolean isSellStopSatisfied(int entryIndex, int currentIndex) {
        Num lossRatioThreshold = HUNDRED.minus(lossPercentage).dividedBy(HUNDRED);

        Num minLow = lowPrice.numOf(9999999);
        for(int i = entryIndex; i <= currentIndex; i++) {
            if (lowPrice.getValue(i).isLessThan(minLow)){
                minLow = lowPrice.getValue(i);
            }
        }

        Num threshold = minLow.multipliedBy(lossRatioThreshold);
        boolean condition = highPrice.getValue(currentIndex).isGreaterThanOrEqual(threshold);
        if (condition) {
            System.out.println("reached threshold: " + threshold.doubleValue());
        }
        return condition;
    }
}
