package org.bamburov.ta.rule;

import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.AbstractRule;
import org.ta4j.core.rules.CrossedDownIndicatorRule;

public class Bollinger3InARowLowerRule extends AbstractRule {

    private final ClosePriceIndicator close;

    private final BollingerBandsLowerIndicator bbl;

    public Bollinger3InARowLowerRule(ClosePriceIndicator close, BollingerBandsLowerIndicator bbl) {
        this.close = close;
        this.bbl = bbl;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        if (i < 24) {
            return false;
        }
        if (tradingRecord != null &&
                tradingRecord.getCurrentPosition() != null
                && tradingRecord.getCurrentPosition().isOpened()) {
            return false;
        }
        return new CrossedDownIndicatorRule(close, bbl).isSatisfied(i - 2) &&
                close.getValue(i - 1).isLessThan(bbl.getValue(i - 1)) &&
                close.getValue(i).isLessThan(bbl.getValue(i));
    }
}
