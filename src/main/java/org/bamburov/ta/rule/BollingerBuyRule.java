package org.bamburov.ta.rule;

import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.rules.AbstractRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

public class BollingerBuyRule extends AbstractRule {

    private BollingerState prevState;

    private final BollingerBandsLowerIndicator bbl;

    private final BollingerBandsMiddleIndicator bbm;

    private final BollingerBandsUpperIndicator bbu;

    private final LowPriceIndicator low;

    private final HighPriceIndicator high;

    private final int lengthOfRecentPeriod;

    private int periodOfCurrentState = 0;

    private final BarSeries series;

    private final CrossedUpIndicatorRule highCrossedUpMiddleLine;

    private final CrossedUpIndicatorRule lowCrossedUpLowLine;

    public BollingerBuyRule(
            BollingerBandsUpperIndicator bbu,
            BollingerBandsMiddleIndicator bbm,
            BollingerBandsLowerIndicator bbl,
            int lengthOfRecentPeriod
    ) {
        this.bbl = bbl;
        this.bbm = bbm;
        this.bbu = bbu;
        this.lengthOfRecentPeriod = lengthOfRecentPeriod;
        series = bbm.getBarSeries();
        low = new LowPriceIndicator(series);
        high = new HighPriceIndicator(series);
        highCrossedUpMiddleLine = new CrossedUpIndicatorRule(high, bbm);
        lowCrossedUpLowLine = new CrossedUpIndicatorRule(low, bbl);
        //prevState = BollingerState.DEFAULT;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        if (tradingRecord.getCurrentPosition() != null
                && tradingRecord.getCurrentPosition().isOpened()) {
            return false;
        }
        if (!highCrossedUpMiddleLine.isSatisfied(i)) {
            return false;
        }
        for (int index = Math.max(0, i - lengthOfRecentPeriod); index <=i ; index ++) {
            if (lowCrossedUpLowLine.isSatisfied(index)) {
                return true;
            }
        }
        return false;
    }

//    @Override
//    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
//        if (i > series.getEndIndex() || i < series.getBeginIndex()) {
//            System.out.println("BollingerBuyRule: Out of bounds");
//            return false;
//        }
//        BollingerState currentState = getNewState(i, periodOfCurrentState, prevState);
//        if (currentState != prevState
//                && !(currentState == BollingerState.BETWEEN_UPPER_AND_MIDDLE_LINES && prevState == BollingerState.RECENTLY_BACK_TO_RANGE_FROM_HIGH)
//                && !(currentState == BollingerState.BETWEEN_MIDDLE_AND_LOWER_LINES && prevState == BollingerState.RECENTLY_BACK_TO_RANGE_FROM_LOW)) {
//            prevState = currentState;
//            periodOfCurrentState = 0;
//        } else {
//            periodOfCurrentState++;
//        }
//        switch (currentState) {
//            case CROSSED_MIDDLE -> {
//                return prevState == BollingerState.RECENTLY_BACK_TO_RANGE_FROM_LOW || prevState == BollingerState.LOW_BELOW_THE_LOWER_LINE;
//            }
//            default -> {
//                return false;
//            }
//        }
//    }

//    private BollingerState getNewState(int index, int periodOfCurrentState, BollingerState prevState) {
//        Num high = series.getBar(index).getHighPrice();
//        Num low = series.getBar(index).getLowPrice();
//        if (high.isGreaterThan(bbu.getValue(index))) {
//            return BollingerState.HIGH_ABOVE_THE_UPPER_LINE;
//        }
//        if (low.isGreaterThan(bbm.getValue(index))) {
//            if (periodOfCurrentState < lengthOfRecentPeriod && prevState == BollingerState.HIGH_ABOVE_THE_UPPER_LINE) {
//                return BollingerState.RECENTLY_BACK_TO_RANGE_FROM_HIGH;
//            } else {
//                return BollingerState.BETWEEN_UPPER_AND_MIDDLE_LINES;
//            }
//        }
//        if (high.isGreaterThan(bbm.getValue(index)) && low.isLessThan(bbm.getValue(index))) {
//            return BollingerState.CROSSED_MIDDLE;
//        }
//        if (high.isLessThan(bbm.getValue(index)) && low.isGreaterThan(bbl.getValue(index))) {
//            if (periodOfCurrentState < lengthOfRecentPeriod && prevState == BollingerState.LOW_BELOW_THE_LOWER_LINE) {
//                return BollingerState.RECENTLY_BACK_TO_RANGE_FROM_LOW;
//            } else {
//                return BollingerState.BETWEEN_MIDDLE_AND_LOWER_LINES;
//            }
//        }
//        return BollingerState.LOW_BELOW_THE_LOWER_LINE;
//    }
}
