package org.bamburov.ta.rule;

import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.AbstractRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

public class Bollinger3InARowUpperRule extends AbstractRule {

    private final ClosePriceIndicator close;

    private final BollingerBandsUpperIndicator bbu;

    public Bollinger3InARowUpperRule(ClosePriceIndicator close, BollingerBandsUpperIndicator bbu) {
        this.close = close;
        this.bbu = bbu;
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
        return new CrossedUpIndicatorRule(close, bbu).isSatisfied(i - 2) &&
                close.getValue(i - 1).isGreaterThan(bbu.getValue(i - 1)) &&
                close.getValue(i).isGreaterThan(bbu.getValue(i));
    }
}
