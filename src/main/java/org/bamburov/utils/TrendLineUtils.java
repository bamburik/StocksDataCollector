package org.bamburov.utils;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrendLineUtils {

    public static double getKOfSupportLine(Indicator<Num> indicator, int period) {
        List<Integer> lowsIndexes = getLowIndexes(indicator, period);
        int lowestLowIndex = getLowestLowIndex(indicator, lowsIndexes);
        int secondLowestLowIndex;
        if (period >= 10) {
            if (indicator.getBarSeries().getEndIndex() - period / 2 >= lowestLowIndex) {
                secondLowestLowIndex = getLowestLowIndex(indicator, lowsIndexes.stream().filter(x -> x > lowestLowIndex).collect(Collectors.toList()));
            } else {
                secondLowestLowIndex = getLowestLowIndex(indicator, lowsIndexes.stream().filter(x -> x < lowestLowIndex).collect(Collectors.toList()));
            }
        } else  {
            secondLowestLowIndex = getLowestLowIndex(indicator, lowsIndexes.stream().filter(x -> x != lowestLowIndex).collect(Collectors.toList()));
        }
        double valueDifference;
        int lengthBetweenLows;
        if (secondLowestLowIndex > lowestLowIndex) {
            valueDifference = indicator.getValue(secondLowestLowIndex).minus(indicator.getValue(lowestLowIndex)).doubleValue();
            lengthBetweenLows = secondLowestLowIndex - lowestLowIndex;
        } else {
            valueDifference = indicator.getValue(lowestLowIndex).minus(indicator.getValue(secondLowestLowIndex)).doubleValue();
            lengthBetweenLows = lowestLowIndex - secondLowestLowIndex;
        }
        return  valueDifference / lengthBetweenLows;
    }

    public static double getKOfResistanceLine(Indicator<Num> indicator, int period) {
        List<Integer> highsIndexes = getHighIndexes(indicator, period);
        int highestHighIndex = getHighestHighIndex(indicator, highsIndexes);
        int secondHighestHighIndex;
        if (period >= 10) {
            if (indicator.getBarSeries().getEndIndex() - period / 2 >= highestHighIndex) {
                secondHighestHighIndex = getHighestHighIndex(indicator, highsIndexes.stream().filter(x -> x > highestHighIndex).collect(Collectors.toList()));
            } else {
                secondHighestHighIndex = getHighestHighIndex(indicator, highsIndexes.stream().filter(x -> x < highestHighIndex).collect(Collectors.toList()));
            }
        } else {
            secondHighestHighIndex = getHighestHighIndex(indicator, highsIndexes.stream().filter(x -> x != highestHighIndex).collect(Collectors.toList()));
        }
        double valueDifference;
        int lengthBetweenHighs;
        if (secondHighestHighIndex > highestHighIndex) {
            valueDifference = indicator.getValue(secondHighestHighIndex).minus(indicator.getValue(highestHighIndex)).doubleValue();
            lengthBetweenHighs = secondHighestHighIndex - highestHighIndex;
        } else {
            valueDifference = indicator.getValue(highestHighIndex).minus(indicator.getValue(secondHighestHighIndex)).doubleValue();
            lengthBetweenHighs = highestHighIndex - secondHighestHighIndex;
        }
        return  valueDifference / lengthBetweenHighs;
    }

    private static List<Integer> getLowIndexes(Indicator<Num> indicator, int period) {
        List<Integer> result = new ArrayList<>();
        int beginIndex = indicator.getBarSeries().getEndIndex() - period + 1;
        int endIndex = indicator.getBarSeries().getEndIndex();
        result.add(beginIndex);
        for (int i = beginIndex + 1; i < endIndex; i++) {
            if (indicator.getValue(i).isLessThan(indicator.getValue(i - 1)) &&
                    indicator.getValue(i).isLessThan(indicator.getValue(i + 1))) {
                result.add(i);
            }
        }
        if (indicator.getValue(endIndex).isLessThan(indicator.getValue(endIndex - 1))
                || result.size() == 1) {
            result.add(endIndex);
        }
        return result;
    }

    private static List<Integer> getHighIndexes(Indicator<Num> indicator, int period) {
        List<Integer> result = new ArrayList<>();
        int beginIndex = indicator.getBarSeries().getEndIndex() - period + 1;
        int endIndex = indicator.getBarSeries().getEndIndex();
        result.add(beginIndex);
        for (int i = beginIndex + 1; i < endIndex; i++) {
            if (indicator.getValue(i).isGreaterThan(indicator.getValue(i - 1)) &&
                    indicator.getValue(i).isGreaterThan(indicator.getValue(i + 1))) {
                result.add(i);
            }
        }
        if (indicator.getValue(endIndex).isGreaterThan(indicator.getValue(endIndex - 1))
                || result.size() == 1) {
            result.add(endIndex);
        }
        return result;
    }

    private static int getLowestLowIndex(Indicator<Num> indicator, List<Integer> lowsIndexes) {
        int result = lowsIndexes.get(0);
        Num lowestLow = indicator.getValue(result);
        for (Integer index : lowsIndexes) {
            if (indicator.getValue(index).isLessThan(lowestLow)) {
                result = index;
                lowestLow = indicator.getValue(result);
            }
        }
        return result;
    }

    private static int getHighestHighIndex(Indicator<Num> indicator, List<Integer> highsIndexes) {
        int result = highsIndexes.get(0);
        Num highestHigh = indicator.getValue(result);
        for (Integer index : highsIndexes) {
            if (indicator.getValue(index).isGreaterThan(highestHigh)) {
                result = index;
                highestHigh = indicator.getValue(result);
            }
        }
        return result;
    }

}
