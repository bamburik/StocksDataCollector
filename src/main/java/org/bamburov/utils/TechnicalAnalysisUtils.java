package org.bamburov.utils;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.ta4j.core.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.bamburov.utils.Constants.*;
import static org.bamburov.utils.Constants.DAILY_INFO_VOLUME_BY_INDEX_FORMAT;
import static org.bamburov.utils.FileUtils.readFileFromProject;
import static org.bamburov.utils.FileUtils.readFileFromResourcesToString;

public class TechnicalAnalysisUtils {
    public static BarSeries fillData(String path, String stockTicker) throws IOException {
        BarSeries series = new BaseBarSeriesBuilder().withName(stockTicker).build();
        JsonPath jsonPath = new JsonPath(readFileFromProject("prices/" + stockTicker + ".json"));
        int count = jsonPath.getInt("size()");
        for(int i = 0; i < count; i++) {
            series.addBar(
                    ZonedDateTime.parse(jsonPath.getString("[" + i + "].date")),
                    jsonPath.getDouble("[" + i + "].open"),
                    jsonPath.getDouble("[" + i + "].high"),
                    jsonPath.getDouble("[" + i + "].low"),
                    jsonPath.getDouble("[" + i + "].close"),
                    jsonPath.getLong("[" + i + "].volume")
            );
        }
        return series;
    }

    public static BarSeries fillDataFor(String stockTicker) throws IOException {
        return fillData("prices/", stockTicker);
    }

    public static BarSeries getBarSeriesFor(String ticker, int period, String fromDate) throws IOException {
        JsonPath jsonPath = new JsonPath(readFileFromProject("C:\\Users\\v.bamburov\\Documents\\personal\\FileWriteService\\data\\" + ticker + ".json"));
        String fullDate = jsonPath.getString(String.format(PRICE_AND_VOLUME_HISTORY_FULL_DATE_BY_SHORT_DATE_FORMAT, fromDate));
        if (fullDate == null) {
            return null;
        }
        int index = jsonPath.getInt(String.format(PRICE_AND_VOLUME_HISTORY_INDEX_BY_FULL_DATE_FORMAT, fullDate));
        BarSeries result = new BaseBarSeries();
        for (int i = index - period + 1; i <=index; i++) {
            result.addBar(
                    ZonedDateTime.of(LocalDateTime.parse(jsonPath.getString("[" + i + "].date")), ZoneId.systemDefault()),
                    jsonPath.getDouble("[" + i + "].open"),
                    jsonPath.getDouble("[" + i + "].high"),
                    jsonPath.getDouble("[" + i + "].low"),
                    jsonPath.getDouble("[" + i + "].close"),
                    jsonPath.getLong("[" + i + "].volume")
            );
        }
        return result;
    }

    public static List<int[]> getRangesWhereRsiBelow32(RSIIndicator rsi, int lastIndex, int period) {
        List<int[]> result = new ArrayList<>();
        boolean rsiBelow32 = false;
        for (int index = lastIndex; index >= lastIndex - period; index--) {
            if (rsiBelow32){
                if (rsi.getValue(index).isGreaterThan(rsi.numOf(32))) {
                    result.get(result.size() - 1)[0] = index + 1;
                }
            } else {
                if (rsi.getValue(index).isLessThan(rsi.numOf(32))) {
                    result.add(new int[2]);
                    result.get(result.size() - 1)[1] = index;
                }
            }
            rsiBelow32 = rsi.getValue(index).isLessThan(rsi.numOf(32));
        }
        if (rsiBelow32) {
            result.get(result.size() - 1)[0] = lastIndex - period;
        }
        return result;
    }

    public static List<int[]> getRangesWhereRsiAbove68(RSIIndicator rsi, int lastIndex, int period) {
        List<int[]> result = new ArrayList<>();
        boolean rsiAbove68 = false;
        for (int index = lastIndex; index >= lastIndex - period; index--) {
            if (rsiAbove68){
                if (rsi.getValue(index).isLessThan(rsi.numOf(68))) {
                    result.get(result.size() - 1)[0] = index + 1;
                }
            } else {
                if (rsi.getValue(index).isGreaterThan(rsi.numOf(68))) {
                    result.add(new int[2]);
                    result.get(result.size() - 1)[1] = index;
                }
            }
            rsiAbove68 = rsi.getValue(index).isGreaterThan(rsi.numOf(68));
        }
        if (rsiAbove68) {
            result.get(result.size() - 1)[0] = lastIndex - period;
        }
        return result;
    }

    public static List<int[]> getRangesWhereStochasticBelow20(StochasticOscillatorKIndicator sok, int lastIndex, int period) {
        List<int[]> result = new ArrayList<>();
        boolean stochasticBelow20 = false;
        for (int index = lastIndex; index >= lastIndex - period; index--) {
            if (stochasticBelow20){
                if (sok.getValue(index).isGreaterThan(sok.numOf(20))) {
                    result.get(result.size() - 1)[0] = index + 1;
                }
            } else {
                if (sok.getValue(index).isLessThan(sok.numOf(20))) {
                    result.add(new int[2]);
                    result.get(result.size() - 1)[1] = index;
                }
            }
            stochasticBelow20 = sok.getValue(index).isLessThan(sok.numOf(20));
        }
        if (stochasticBelow20) {
            result.get(result.size() - 1)[0] = lastIndex - period;
        }
        return result;
    }

    public static List<int[]> getRangesWhereStochasticAbove80(StochasticOscillatorKIndicator sok, int lastIndex, int period) {
        List<int[]> result = new ArrayList<>();
        boolean stochasticAbove80 = false;
        for (int index = lastIndex; index >= lastIndex - period; index--) {
            if (stochasticAbove80){
                if (sok.getValue(index).isLessThan(sok.numOf(80))) {
                    result.get(result.size() - 1)[0] = index + 1;
                }
            } else {
                if (sok.getValue(index).isGreaterThan(sok.numOf(80))) {
                    result.add(new int[2]);
                    result.get(result.size() - 1)[1] = index;
                }
            }
            stochasticAbove80 = sok.getValue(index).isGreaterThan(sok.numOf(80));
        }
        if (stochasticAbove80) {
            result.get(result.size() - 1)[0] = lastIndex - period;
        }
        return result;
    }

    public static List<Double> inditatorToList(Indicator<Num> indicator, int startIndex, int endIndex) {
        List<Double> result = new ArrayList<>();
        for (int index = startIndex; index <= endIndex; index++) {
            result.add(indicator.getValue(index).doubleValue());
        }
        return result;
    }

    public static double getExitPriceByDynamicStoploss(Trade.TradeType tradeType, BarSeries series, int entryIndex, int exitIndex, int lossPercentage) {
        Num lossPercentageNum = series.numOf(lossPercentage);
        Num hundred = series.numOf(100);
        if (tradeType == Trade.TradeType.BUY) {
            Num maxHigh = series.numOf(0);
            for(int i = entryIndex; i <= exitIndex; i++) {
                if (series.getBar(i).getHighPrice().isGreaterThan(maxHigh)){
                    maxHigh = series.getBar(i).getHighPrice();
                }
            }
            Num lossRatioThreshold = hundred.minus(lossPercentageNum).dividedBy(hundred);
            return maxHigh.multipliedBy(lossRatioThreshold).doubleValue();
        } else {
            Num minLow = series.numOf(9999999);
            for(int i = entryIndex; i <= exitIndex; i++) {
                if (series.getBar(i).getLowPrice().isLessThan(minLow)){
                    minLow = series.getBar(i).getLowPrice();
                }
            }
            Num lossRatioThreshold = hundred.plus(lossPercentageNum).dividedBy(hundred);
            return minLow.multipliedBy(lossRatioThreshold).doubleValue();
        }
    }

    public static boolean isCandleRed(Bar bar) {
        return bar.getClosePrice().isLessThan(bar.getOpenPrice());
    }

    public static boolean isDragonflyDoji(Bar bar) {
        double closeOpenDiff = bar.getClosePrice().minus(bar.getOpenPrice()).doubleValue();
        closeOpenDiff = closeOpenDiff >= 0 ? closeOpenDiff : closeOpenDiff * -1;
        double highLowDiff = bar.getHighPrice().minus(bar.getLowPrice()).doubleValue();
        double highCloseDiff = bar.getHighPrice().minus(bar.getClosePrice()).doubleValue();
        return  closeOpenDiff * 50 <= highLowDiff && highCloseDiff * 50 <= highLowDiff;
    }

    public static boolean isGravestoneDoji(Bar bar) {
        double closeOpenDiff = bar.getClosePrice().minus(bar.getOpenPrice()).doubleValue();
        closeOpenDiff = closeOpenDiff >= 0 ? closeOpenDiff : closeOpenDiff * -1;
        double highLowDiff = bar.getHighPrice().minus(bar.getLowPrice()).doubleValue();
        double closeLowDiff = bar.getClosePrice().minus(bar.getLowPrice()).doubleValue();
        return  closeOpenDiff * 50 <= highLowDiff && closeLowDiff * 50 <= highLowDiff;
    }

    public static boolean isBearishShavenTop(Bar bar) {
        double highOpenDiff = bar.getHighPrice().minus(bar.getOpenPrice()).doubleValue();
        double highLowDiff = bar.getHighPrice().minus(bar.getLowPrice()).doubleValue();
        return isCandleRed(bar) && highOpenDiff * 50 <= highLowDiff;
    }

    public static boolean isBearishShavenTopWithLongShadow(Bar bar) {
        double closeLowDiff = bar.getClosePrice().minus(bar.getLowPrice()).doubleValue();
        double openCloseDiff = bar.getOpenPrice().minus(bar.getClosePrice()).doubleValue();
        return isBearishShavenTop(bar) && closeLowDiff > openCloseDiff;
    }

    public static boolean isBullishShavenTop(Bar bar) {
        double highCloseDiff = bar.getHighPrice().minus(bar.getClosePrice()).doubleValue();
        double highLowDiff = bar.getHighPrice().minus(bar.getLowPrice()).doubleValue();
        return !isCandleRed(bar) && highCloseDiff * 50 <= highLowDiff;
    }

    public static boolean isBullishShavenTopWithLongShadow(Bar bar) {
        double openLowDiff = bar.getOpenPrice().minus(bar.getLowPrice()).doubleValue();
        double closeOpenDiff = bar.getClosePrice().minus(bar.getOpenPrice()).doubleValue();
        return isBullishShavenTop(bar) && openLowDiff > closeOpenDiff;
    }

    public static boolean isBearishShavenBottom(Bar bar) {
        double closeLowDiff = bar.getClosePrice().minus(bar.getLowPrice()).doubleValue();
        double highLowDiff = bar.getHighPrice().minus(bar.getLowPrice()).doubleValue();
        return isCandleRed(bar) && closeLowDiff * 50 <= highLowDiff;
    }

    public static boolean isBearishShavenBottomWithLongShadow(Bar bar) {
        double highOpenDiff = bar.getHighPrice().minus(bar.getOpenPrice()).doubleValue();
        double openCloseDiff = bar.getOpenPrice().minus(bar.getClosePrice()).doubleValue();
        return isBearishShavenBottom(bar) && highOpenDiff > openCloseDiff;
    }

    public static boolean isBullishShavenBottom(Bar bar) {
        double openLowDiff = bar.getOpenPrice().minus(bar.getLowPrice()).doubleValue();
        double highLowDiff = bar.getHighPrice().minus(bar.getLowPrice()).doubleValue();
        return !isCandleRed(bar) && openLowDiff * 50 <= highLowDiff;
    }

    public static boolean isBullishShavenBottomWithLongShadow(Bar bar) {
        double highCloseDiff = bar.getHighPrice().minus(bar.getClosePrice()).doubleValue();
        double closeOpenDiff = bar.getClosePrice().minus(bar.getOpenPrice()).doubleValue();
        return isBullishShavenBottom(bar) && highCloseDiff > closeOpenDiff;
    }
}
