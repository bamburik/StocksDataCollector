package org.bamburov.ta.rule;

import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

public class CrossedUpIndicatorRuleWhenInPosition extends AbstractRule {
    private final CrossedUpIndicatorRule crossedUpIndicatorRule;

    public CrossedUpIndicatorRuleWhenInPosition(CrossedUpIndicatorRule crossedUpIndicatorRule) {
        this.crossedUpIndicatorRule = crossedUpIndicatorRule;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        return  tradingRecord.getCurrentPosition() != null
                && tradingRecord.getCurrentPosition().isOpened()
                && crossedUpIndicatorRule.isSatisfied(i);
    }

}
