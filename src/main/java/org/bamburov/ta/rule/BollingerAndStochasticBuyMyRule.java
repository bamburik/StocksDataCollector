package org.bamburov.ta.rule;

import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.rules.AbstractRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

public class BollingerAndStochasticBuyMyRule extends AbstractRule {

    private final StochasticOscillatorKIndicator sok;

    private final StochasticOscillatorDIndicator sod;

    private final BollingerBandsLowerIndicator bbl;

    private final LowPriceIndicator lowPrice;

    private final CrossedUpIndicatorRule sokCrossedUpSod;

    private final CrossedUpIndicatorRule lowCrossedUpBbl;

    private int startIndex;

    private int lengthOfRecentPeriod;

    public BollingerAndStochasticBuyMyRule(
            StochasticOscillatorKIndicator sok,
            StochasticOscillatorDIndicator sod,
            BollingerBandsLowerIndicator bbl,
            LowPriceIndicator lowPrice,
            int startIndex,
            int lengthOfRecentPeriod
    ) {
        this.sod = sod;
        this.sok = sok;
        this.bbl = bbl;
        this.lowPrice = lowPrice;
        sokCrossedUpSod = new CrossedUpIndicatorRule(sok, sod);
        lowCrossedUpBbl = new CrossedUpIndicatorRule(lowPrice, bbl);
        this.startIndex = startIndex;
        this.lengthOfRecentPeriod = lengthOfRecentPeriod;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        if (i < startIndex + lengthOfRecentPeriod) {
            return false;
        }
        if (tradingRecord != null
                && tradingRecord.getCurrentPosition() != null
                && tradingRecord.getCurrentPosition().isOpened()) {
            return false;
        }
        if (!lowCrossedUpBbl.isSatisfied(i)) {
            return false;
        }
        for (int index = i - lengthOfRecentPeriod; index <=i ; index ++) {
            if (isStochasticSatisfied(index)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStochasticSatisfied(int i) {
        if (i == 0 || sok.getValue(i - 1).isGreaterThan(sok.numOf(20))) {
            return false;
        }
        return sokCrossedUpSod.isSatisfied(i);
    }
}
