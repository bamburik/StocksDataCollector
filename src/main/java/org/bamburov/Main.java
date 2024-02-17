package org.bamburov;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.exception.JsonPathException;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bamburov.models.*;
import org.bamburov.models.backTestingOutputData.*;
import org.bamburov.ta.core.MyPosition;
import org.bamburov.ta.core.MyTradingRecord;
import org.bamburov.ta.rule.BollingerAndStochasticBuyMyRule;
import org.bamburov.ta.rule.Bollinger3InARowUpperRule;
import org.bamburov.ta.rule.CrossedUpIndicatorRuleWhenInPosition;
import org.bamburov.utils.*;
import org.ta4j.core.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopLossRule;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static org.bamburov.utils.ApiUtils.*;
import static org.bamburov.utils.FileUtils.readFileFromProject;
import static org.bamburov.utils.FileUtils.readFileFromResourcesToList;
import static org.bamburov.utils.MySqlUtils.*;
import static org.bamburov.utils.StatisticsUtils.*;
import static org.bamburov.utils.TechnicalAnalysisUtils.*;
import static org.bamburov.utils.TechnicalAnalysisUtils.getRangesWhereRsiAbove68;
import static org.bamburov.utils.TrendLineUtils.getKOfResistanceLine;
import static org.bamburov.utils.TrendLineUtils.getKOfSupportLine;

public class Main {
    private static final String LOCAL_CONFIG_PROPERTIES = "config.properties";
    private static Props props;
    private static Logger logger = LogManager.getRootLogger();

    public static void main(String[] args) throws Exception {
        try {
            loadProperties();

            //printBollingerAndRsiOrStochastic("2023-11-24");
            List<String> tickers = readFileFromResourcesToList("strategies/BollingerBandsRsiStochastics2.csv");
            //List<String> tickers = Arrays.asList("AAOI");
            //testStrategy(tickers, 15);
//            calculateIntermediateResult();
            createMySqlConnection();
//            int totalAmountOfSignals = printScorePlusMinus3Events(tickers, "2024-02-01", "2024-02-02");
//            totalAmountOfSignals += printBollingerAndRsiOrStochasticOrHammerStrict(tickers, "2024-02-02");
//            System.out.println("Total amount of signals - " + totalAmountOfSignals);


//            printBollingerAn                                   dStochasticBuyAndSellEventsFor(
//                    readFileFromResourcesToList("strategies/BollingerBandsStochasticBuy.csv"),
//                    readFileFromResourcesToList("strategies/BollingerBandsStochasticBuyOpenPositions.txt"),
//                    "2023-09-20");
            //fulfillInfo2022();
            //writeToFileFromResources("src/main/resources/strategies/BollingerBandsRsiStochasticsForPostman2.csv" ,getTickersFromNyseAndNasdaq());
            fulfillDaily();
            //printTechnicalTechnicalSummaryAnalysisEvents();
            //writePricesAndVolumesToFile();
            //writeResultsOfBuyByBollingerAndStochastic();
            //printBollingerStrategy2();
        } finally {
            closeMySqlConnection();
        }
    }

    private static void fulfillDaily() throws Exception {
        List<String> tickers = readFileFromResourcesToList("tickers.txt");
        int tickersPerPage = 20;
        int startPageIndex = props.getStartPageIndex();
        for (int pageIndex = startPageIndex; pageIndex < (tickers.size() / tickersPerPage) + 1; pageIndex++) {
            List<StockDailyInfo> list = new ArrayList<>();
            List<String> sublist = new ArrayList<>();
            List<String> sublistWithTechnicalData = new ArrayList<>();
            for (int j = pageIndex * tickersPerPage; j < (pageIndex + 1) * tickersPerPage && j < tickers.size(); j++) {
                sublist.add(tickers.get(j));
            }
            List<StockInfo2022> yearInfo = getYearInfoFor(sublist);
            for (int j = pageIndex * tickersPerPage; j < (pageIndex + 1) * tickersPerPage && j < tickers.size(); j++) {
                StockDailyInfo dailyInfo = getStockDailyInfo(tickers.get(j));
                if (dailyInfo != null) {
                    if (dailyInfo.getCurrentPrice() != null && dailyInfo.getEps() != null) {
                        Double p2eCalc = dailyInfo.getCurrentPrice() / dailyInfo.getEps();
                        dailyInfo.setP2eCalc(Double.isInfinite(p2eCalc) || Double.isNaN(p2eCalc) || Double.isFinite(p2eCalc) ? null : p2eCalc);
                    }
                    if (dailyInfo.getCurrentPrice() != null &&
                            yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().isPresent() &&
                            yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getTotalAssets() != null &&
                            yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getTotalLiabilities() != null &&
                            yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getSharesOutstanding() != null) {
                        long totalAssets = yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getTotalAssets();
                        long totalLiabilities = yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getTotalLiabilities();
                        long sharesOutStanding = yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getSharesOutstanding();
                        Double p2bCalc = (dailyInfo.getCurrentPrice() * sharesOutStanding) / (totalAssets - totalLiabilities);
                        dailyInfo.setP2bCalc(Double.isInfinite(p2bCalc) || Double.isNaN(p2bCalc) || Double.isFinite(p2bCalc) ? null : p2bCalc);
                    }
                    if (dailyInfo.getCurrentPrice() != null &&
                            yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().isPresent() &&
                            yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getTotalRevenue() != null &&
                            yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getSharesOutstanding() != null) {
                        long totalRevenue = yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getTotalRevenue();
                        long sharesOutStanding = yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getSharesOutstanding();
                        Double p2sCalc = (dailyInfo.getCurrentPrice() * sharesOutStanding) / totalRevenue;
                        dailyInfo.setP2sCalc(Double.isInfinite(p2sCalc) || Double.isNaN(p2sCalc) || Double.isFinite(p2sCalc) ? null : p2sCalc);
                    }
                    if (dailyInfo.getDoesHaveTechnicalData()) {
                        sublistWithTechnicalData.add(tickers.get(j));
                    }
                    list.add(dailyInfo);
                }
            }
            Map<String, TechnicalAnalysis> weekAnalysis = getTechnicalAnalysis(sublistWithTechnicalData, "week");
            Map<String, TechnicalAnalysis> monthAnalysis = getTechnicalAnalysis(sublistWithTechnicalData, "month");
            for (int j = 0; j < list.size(); j++) {
                if (weekAnalysis != null && weekAnalysis.containsKey(list.get(j).getStock())) {
                    list.get(j).setWeekAnalysis(weekAnalysis.get(list.get(j).getStock()));
                }
                if (monthAnalysis != null && monthAnalysis.containsKey(list.get(j).getStock())) {
                    list.get(j).setMonthAnalysis(monthAnalysis.get(list.get(j).getStock()));
                }
            }
            insertToDaily(list);
            logger.info("page number - " + pageIndex + "\n" +
                    "inserted data of: \n" +
                    Arrays.toString(sublist.toArray()));
        }
    }

    private static void fulfillInfo2022() throws Exception {
        int i = 1;
        List<String> allTickers = new ArrayList<>();
        List<String> tickers;
        do {
            tickers = getStockTickers(i);
            if (tickers == null || tickers.size() == 0) {
                break;
            }
            List<StockInfo2022> data = new ArrayList<>();
            for (String ticker : tickers) {
                if (!allTickers.contains(ticker)) {
                    StockInfo2022 stockInfo2022 = getStockYearInfo(ticker);
                    if (stockInfo2022 != null) {
                        data.add(getStockYearInfo(ticker));
                    }
                }
            }
            allTickers.addAll(tickers);
            insertToInfo2022(data);
            logger.info("page number - " + i + "\n" +
                    "inserted data of: \n" +
                    Arrays.toString(tickers.toArray()));
            i++;
        } while (tickers != null && tickers.size() > 0);
    }

    public static void testStrategy(
            List<String> tickers,
            double relativeTakeProfit,
            double relativeStoploss,
            double initialCapital,
            int lengthOfPosition) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        double capital = initialCapital;
        for (int i = 0; i < tickers.size(); i++) {
            System.out.println(i + ". test Harammi on " + tickers.get(i) + " ticker");
            // Read data and calculate indicators
            BarSeries series = null;
            try {
                series = fillDataFor(tickers.get(i));
            } catch (IOException e) {
                continue;
            } catch (JsonPathException e) {
                continue;
            }
            // Filter stocks with low liquidity
            if (series.getBarData().stream().filter(s -> s.getVolume().doubleValue() < 50).toList().size() > 10) {
                continue;
            }
            ClosePriceIndicator close = new ClosePriceIndicator(series);
            RSIIndicator rsi = new RSIIndicator(close, 14);
            SMAIndicator volumeSma = new SMAIndicator(new VolumeIndicator(series), 40);
            SMAIndicator closeSma = new SMAIndicator(close, 40);
            for (int j = 60; j <= series.getEndIndex() - lengthOfPosition; j++) {
                if (isCandleRed(series.getBar(j - 1)) && !isCandleRed(series.getBar(j))
                        && series.getBar(j - 1).getOpenPrice().isGreaterThan(series.getBar(j).getHighPrice())
                        && series.getBar(j - 1).getClosePrice().isLessThan(series.getBar(j).getLowPrice())
                        && rsi.getValue(j).doubleValue() <= 32
                        && volumeSma.getValue(j).multipliedBy(closeSma.getValue(j)).isGreaterThan(closeSma.numOf(1000000))) {
                    Bar prev60BarsMaxPriceBar = series.getBarData().subList(j - 60, j).stream().max(Comparator.comparingDouble(x -> x.getHighPrice().doubleValue())).get();
                    double prev60BarsMaxPrice = prev60BarsMaxPriceBar.getHighPrice().doubleValue();
                    double prev60BarsMinPrice = series.getBarData().subList(j - 60, j).stream().min(Comparator.comparingDouble(x -> x.getLowPrice().doubleValue())).get().getLowPrice().doubleValue();
                    double diffPrev60BarsMaxMin = prev60BarsMaxPrice - prev60BarsMinPrice;
                    double entryPrice = series.getBar(j + 1).getOpenPrice().doubleValue();
                    int countOfShares = (int) (capital / entryPrice);
                    double takeProfit = entryPrice + relativeTakeProfit * diffPrev60BarsMaxMin;
                    double stoploss = entryPrice - relativeStoploss * diffPrev60BarsMaxMin;
                    List<Bar> sublist = series.getBarData().subList(j + 1, j + 1 + lengthOfPosition);
                    int stopLossIndex = IntStream.range(0, sublist.size())
                            .filter(x -> sublist.get(x).getLowPrice().doubleValue() <= stoploss)
                            .findFirst().orElse(lengthOfPosition + 1);
                    int takeProfitIndex = IntStream.range(0, sublist.size())
                            .filter(x -> sublist.get(x).getHighPrice().doubleValue() >= takeProfit)
                            .findFirst().orElse(lengthOfPosition + 1);
                    double exitPrice;
                    if (takeProfitIndex == lengthOfPosition + 1 && stopLossIndex == lengthOfPosition + 1) {
                        exitPrice = sublist.get(sublist.size() - 1).getClosePrice().doubleValue();
                    } else if (stopLossIndex <= takeProfitIndex) {
                        exitPrice = stoploss;
                    } else {
                        exitPrice = takeProfit;
                    }
                    capital += ((exitPrice - entryPrice) * countOfShares);
                }
            }
            System.out.println(capital);
        }
    }

        public static void calculateIntermediateResult() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<IntermediateResult> intermediateResult = objectMapper.readValue(readFileFromProject("intermediateResult.json"), new TypeReference<>(){});
        Result result = new Result();
        result.setAmountOfFalseSignals(intermediateResult.stream().filter(IntermediateResult::isSignalFalse).count());
        result.setAmountOfTrueSignals(intermediateResult.stream().filter(x -> !x.isSignalFalse()).count());
        result.setTrueSignalsData(new TrueSignalsData());
        List<Double> sortedProfits = intermediateResult.stream().filter(x -> !x.isSignalFalse()).map(IntermediateResult::getRelativeMaxProfit).sorted(Comparator.comparingDouble(x -> x)).toList();
        result.getTrueSignalsData().setAverageRelativeProfit(sortedProfits.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        result.getTrueSignalsData().setTenPercentileRelativeProfit(get10thPercentile(sortedProfits));
        result.getTrueSignalsData().setFiftyPercentileRelativeProfit(get50thPercentile(sortedProfits));
        result.getTrueSignalsData().setNinetyPercentileRelativeProfit(get90thPercentile(sortedProfits));
        List<Double> sortedRequiredStoplosses = intermediateResult.stream().filter(x -> !x.isSignalFalse()).map(IntermediateResult::getRelativeRequiredStoploss).sorted(Comparator.comparingDouble(x -> x)).toList();
        result.getTrueSignalsData().setAverageRelativeRequiredStopLoss(sortedRequiredStoplosses.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        result.getTrueSignalsData().setTenPercentileRelativeRequiredStopLoss(get10thPercentile(sortedRequiredStoplosses));
        result.getTrueSignalsData().setFiftyPercentileRelativeRequiredStopLoss(get50thPercentile(sortedRequiredStoplosses));
        result.getTrueSignalsData().setNinetyPercentileRelativeRequiredStopLoss(get90thPercentile(sortedRequiredStoplosses));

        result.setFalseSignalsData(new FalseSignalsData());
        List<Double> sortedLosses = intermediateResult.stream().filter(IntermediateResult::isSignalFalse).map(IntermediateResult::getRelativeRequiredStoploss).sorted(Comparator.comparingDouble(x -> x)).toList();
        result.getFalseSignalsData().setAverageRelativeMaxLoss(sortedLosses.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        result.getFalseSignalsData().setTenPercentileRelativeMaxLoss(get10thPercentile(sortedLosses));
        result.getFalseSignalsData().setFiftyPercentileRelativeMaxLoss(get50thPercentile(sortedLosses));
        result.getFalseSignalsData().setNinetyPercentileRelativeMaxLoss(get90thPercentile(sortedLosses));

        // Print count of stocks where event was caught more or equal than 10 times
        List<String> counted = intermediateResult.stream()
                .collect(Collectors.groupingBy(IntermediateResult::getTicker, Collectors.counting()))
                .entrySet().stream().filter(x -> x.getValue() >= 10).map(Map.Entry::getKey).toList();
        System.out.println(counted.size());

        result.setStocks(new ArrayList<>());
        for (String ticker : counted) {
            List<IntermediateResult> list = intermediateResult.stream().filter(x -> x.getTicker().equals(ticker)).toList();
            OneStockResult oneStockResult = new OneStockResult();
            oneStockResult.setTicker(ticker);
            oneStockResult.setAmountOfFalseSignals(list.stream().filter(IntermediateResult::isSignalFalse).count());
            oneStockResult.setAmountOfTrueSignals(list.stream().filter(x -> !x.isSignalFalse()).count());
            oneStockResult.setTrueSignalsData(new TrueSignalsData());
            List<Double> sublistSortedProfits = list.stream().filter(x -> !x.isSignalFalse()).map(IntermediateResult::getRelativeMaxProfit).sorted(Comparator.comparingDouble(x -> x)).toList();
            oneStockResult.getTrueSignalsData().setAverageRelativeProfit(sublistSortedProfits.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            oneStockResult.getTrueSignalsData().setTenPercentileRelativeProfit(get10thPercentile(sublistSortedProfits));
            oneStockResult.getTrueSignalsData().setFiftyPercentileRelativeProfit(get50thPercentile(sublistSortedProfits));
            oneStockResult.getTrueSignalsData().setNinetyPercentileRelativeProfit(get90thPercentile(sublistSortedProfits));
            List<Double> sublistSortedRequiredStoplosses = list.stream().filter(x -> !x.isSignalFalse()).map(IntermediateResult::getRelativeRequiredStoploss).sorted(Comparator.comparingDouble(x -> x)).toList();
            oneStockResult.getTrueSignalsData().setAverageRelativeRequiredStopLoss(sublistSortedRequiredStoplosses.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            oneStockResult.getTrueSignalsData().setTenPercentileRelativeRequiredStopLoss(get10thPercentile(sublistSortedRequiredStoplosses));
            oneStockResult.getTrueSignalsData().setFiftyPercentileRelativeRequiredStopLoss(get50thPercentile(sublistSortedRequiredStoplosses));
            oneStockResult.getTrueSignalsData().setNinetyPercentileRelativeRequiredStopLoss(get90thPercentile(sublistSortedRequiredStoplosses));

            oneStockResult.setFalseSignalsData(new FalseSignalsData());
            List<Double> sublistSortedLosses = intermediateResult.stream().filter(IntermediateResult::isSignalFalse).map(IntermediateResult::getRelativeRequiredStoploss).sorted(Comparator.comparingDouble(x -> x)).toList();
            oneStockResult.getFalseSignalsData().setAverageRelativeMaxLoss(sublistSortedLosses.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            oneStockResult.getFalseSignalsData().setTenPercentileRelativeMaxLoss(get10thPercentile(sublistSortedLosses));
            oneStockResult.getFalseSignalsData().setFiftyPercentileRelativeMaxLoss(get50thPercentile(sublistSortedLosses));
            oneStockResult.getFalseSignalsData().setNinetyPercentileRelativeMaxLoss(get90thPercentile(sublistSortedLosses));
            result.getStocks().add(oneStockResult);
        }
        objectMapper.writeValue(new File("result.json"), result);
    }

    public static void testStrategy(List<String> tickers, int lengthOfPosition) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<IntermediateResult> intermediateResult = objectMapper.readValue(readFileFromProject("intermediateResult.json"), new TypeReference<>(){});
//        for (int i = 0; i < tickers.size();i++) {
        for (int i = 0; i < tickers.size(); i++) {
            System.out.println(i + ". test Harammi on " + tickers.get(i) + " ticker");

            // Read data and calculate indicators
            BarSeries series = null;
            try {
                series = fillDataFor(tickers.get(i));
            } catch (IOException e) {
                continue;
            } catch (JsonPathException e) {
                continue;
            }
            // Filter stocks with low liquidity
            if (series.getBarData().stream().filter(s -> s.getVolume().doubleValue() < 50).toList().size() > 10) {
                continue;
            }
            ClosePriceIndicator close = new ClosePriceIndicator(series);
            RSIIndicator rsi = new RSIIndicator(close, 14);
            SMAIndicator volumeSma = new SMAIndicator(new VolumeIndicator(series), 40);
            SMAIndicator closeSma = new SMAIndicator(close, 40);
            for (int j = 60; j <= series.getEndIndex() - lengthOfPosition; j++) {
                if (isCandleRed(series.getBar(j - 1)) && !isCandleRed(series.getBar(j))
                        && series.getBar(j - 1).getOpenPrice().isGreaterThan(series.getBar(j).getHighPrice())
                        && series.getBar(j - 1).getClosePrice().isLessThan(series.getBar(j).getLowPrice())
                        && rsi.getValue(j).doubleValue() <= 32
                        && volumeSma.getValue(j).multipliedBy(closeSma.getValue(j)).isGreaterThan(closeSma.numOf(1000000))) {
                    Bar prev60BarsMaxPriceBar = series.getBarData().subList(j - 60, j).stream().max(Comparator.comparingDouble(x -> x.getHighPrice().doubleValue())).get();
                    double prev60BarsMaxPrice = prev60BarsMaxPriceBar.getHighPrice().doubleValue();
                    double prev60BarsMinPrice = series.getBarData().subList(j - 60, j).stream().min(Comparator.comparingDouble(x -> x.getLowPrice().doubleValue())).get().getLowPrice().doubleValue();
                    double diffPrev60BarsMaxMin = prev60BarsMaxPrice - prev60BarsMinPrice;
                    double entryPrice = series.getBar(j + 1).getOpenPrice().doubleValue();
                    Bar nextXBarsMaxBar = series.getBarData().subList(j + 1, j + 1 + lengthOfPosition).stream().max(Comparator.comparingDouble(x -> x.getHighPrice().doubleValue())).get();
                    double nextXBarsMaxPrice = nextXBarsMaxBar.getHighPrice().doubleValue();
                    double nextXBarsMinPrice = series.getBarData().subList(j + 1, j + 1 + lengthOfPosition).stream().min(Comparator.comparingDouble(x -> x.getLowPrice().doubleValue())).get().getLowPrice().doubleValue();
                    boolean isSignalFalse = nextXBarsMaxPrice <= entryPrice;
                    if (isSignalFalse) {
                        double relativeLoss = (entryPrice - nextXBarsMinPrice)/diffPrev60BarsMaxMin;
                        intermediateResult.add(new IntermediateResult(tickers.get(i), isSignalFalse, 0, relativeLoss));
                        // debug
//                        System.out.println();
//                        System.out.println("False signal, date - " + series.getBar(j).getDateName() + "; relative loss - " + relativeLoss);
//                        System.out.println("Loss - " + (entryPrice - nextXBarsMinPrice));
                    } else {
                        double maxProfit = nextXBarsMaxPrice - entryPrice;
                        double requiredStopLoss = series.getBarData().subList(j + 1, series.getBarData().indexOf(nextXBarsMaxBar) + 1).stream().min(Comparator.comparingDouble(x -> x.getLowPrice().doubleValue())).get().getLowPrice().doubleValue();
                        double relativeMaxProfit = maxProfit / diffPrev60BarsMaxMin;
                        double relativeRequiredStopLoss = (entryPrice - requiredStopLoss) / diffPrev60BarsMaxMin;
                        intermediateResult.add(new IntermediateResult(tickers.get(i), isSignalFalse, relativeMaxProfit, relativeRequiredStopLoss));
                        // debug
//                        System.out.println();
//                        System.out.println("True signal, date - " + series.getBar(j).getDateName() + "; relative required stoploss - " + relativeRequiredStopLoss + "; relative max profit - " + relativeMaxProfit);
//                        System.out.println("required stoploss - " + requiredStopLoss + "; max profit - " + maxProfit);
                    }
                }
            }
        }
        objectMapper.writeValue(new File("intermediateResult.json"), intermediateResult);
    }

    public static int printScorePlusMinus3Events(List<String> tickers, String startDate, String endDate) throws Exception {
//        List<String> tickers = readFileFromResourcesToList("tickers.txt");
        List<ScoreChangeEventInfo> scoreChangeEventInfoList = MySqlUtils.catchScorePlus3Events(tickers, startDate, endDate);
        scoreChangeEventInfoList = MySqlUtils.setScoreChangesTrend(scoreChangeEventInfoList);
        System.out.println("Score +- 3 events");
        for (ScoreChangeEventInfo info : scoreChangeEventInfoList) {
            System.out.println((info.getCurrentScore() > info.getPrevScore() ? "Buy - " : "Sell - ") + info.getTicker() +
                    "; Current Score - " + info.getCurrentScore() + "; Prev Score - " + info.getPrevScore() +
                    "; Score History - " + info.getScoreHistory());
        }
        return scoreChangeEventInfoList.size();
//        scoreChangeEventInfoList = ApiUtils.setPriceTrend(scoreChangeEventInfoList);
//        ExcelUtils.write(scoreChangeEventInfoList);
//        MySqlUtils.close();
    }

    public static void printTechnicalTechnicalSummaryAnalysisEvents() throws Exception {
        for (String sector : MySqlUtils.getSectors()) {
            List<TechnicalAnalysisChangeEventInfo> list = MySqlUtils.catchTechnicalDaySummaryBecameBuyEvents(sector, "2023-04-21", "2023-08-21");
            ApiUtils.setPrices(list);
            ExcelUtils.write2(list, sector + ".xlsx");
        }
    }

    public static void writePricesAndVolumesToFile() throws IOException {
        List<String> tickers = readFileFromResourcesToList("tickers.txt");
        for (String ticker : tickers) {
            Response response = null;
            try {
                response = ApiUtils.getStockDailyInfoResponse(ticker);
            } catch (Exception e) {
                continue;
            }
            String responseBody = response.getBody().asPrettyString();
            File currDir = new File(".");
            String path = currDir.getAbsolutePath();
            String fileLocation = path.substring(0, path.length() - 1) + "prices\\" + ticker + ".json";
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation));
            writer.write(JsonUtils.getJsonNodeValue(responseBody, "common.prices.data"));

            writer.close();
        }
    }

    public static void writeResultsOfBuyByBollingerAndStochastic() throws IOException {
        List<MyTradingRecord> myTradingRecords = new ArrayList<>();
        List<String> tickers = readFileFromResourcesToList("tickers.txt");
        int period = 20;
        int lengthOfRecentPeriod = 3;
        for (int i = 5000; i < tickers.size(); i++) {
            System.out.println(tickers.get(i));
            // Read data and calculate indicators
            BarSeries series = null;
            try {
                series = fillDataFor(tickers.get(i));
            } catch (IOException e) {
                continue;
            } catch (JsonPathException e) {
                continue;
            }
            // Filter stocks with low liquidity
            if (series.getBarData().stream().filter(s -> s.getVolume().doubleValue() < 50).toList().size() > 10) {
                continue;
            }
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            HighPriceIndicator highPrice = new HighPriceIndicator(series);
            LowPriceIndicator lowPrice = new LowPriceIndicator(series);
            SMAIndicator sma = new SMAIndicator(closePrice, period);
            StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(closePrice, period);
            BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma);
            BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, standardDeviation);
            BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, standardDeviation);
            StochasticOscillatorKIndicator sok = new StochasticOscillatorKIndicator(series, period);
            StochasticOscillatorDIndicator sod = new StochasticOscillatorDIndicator(sok);

            // Run 4 strategies with different stop losses (6, 8, 10, 12)
            for (int stopLossPercent = 6; stopLossPercent <= 12; stopLossPercent += 2) {
                BollingerAndStochasticBuyMyRule buyMyRule = new BollingerAndStochasticBuyMyRule(
                        sok,
                        sod,
                        bbl,
                        lowPrice,
                        period,
                        lengthOfRecentPeriod
                );
                Rule sellingRule2 = new CrossedUpIndicatorRuleWhenInPosition(new CrossedUpIndicatorRule(highPrice, bbu))
                        .or(new CrossedDownIndicatorRule(highPrice, bbm))
                        .or(new StopLossRule(closePrice, stopLossPercent));
                Strategy strategy2 = new BaseStrategy(buyMyRule, sellingRule2);
                BarSeriesManager manager2 = new BarSeriesManager(series);
                TradingRecord tradingRecord2 = manager2.run(strategy2);
                List<MyPosition> list2 = new ArrayList<>();
                for(Position position : tradingRecord2.getPositions()) {
                    int exitIndex = position.getExit().getIndex();
                    double exitPrice = 0;
                    if (new CrossedUpIndicatorRule(highPrice, bbu).isSatisfied(exitIndex)) {
                        exitPrice = bbu.getValue(exitIndex).doubleValue();
                    } else if (new CrossedDownIndicatorRule(highPrice, bbm).isSatisfied(exitIndex)) {
                        if (exitIndex != series.getBarCount() - 1) {
                            exitIndex++;
                            exitPrice = series.getBar(exitIndex).getOpenPrice().doubleValue();
                        } else {
                            exitPrice = series.getBar(exitIndex).getClosePrice().doubleValue();
                        }
                    } else {
                        exitPrice = position.getEntry().getNetPrice().doubleValue() * ((100d - stopLossPercent) / 100d);
                    }
                    MyPosition myPosition = new MyPosition(Trade.TradeType.BUY, position.getEntry().getIndex(), exitIndex, position.getEntry().getPricePerAsset().doubleValue(), exitPrice);
                    list2.add(myPosition);
                }
                myTradingRecords.add(new MyTradingRecord(tickers.get(i), stopLossPercent, list2));
            }
        }
        ExcelUtils.writeResultsOfBuyByBollingerAndStochastic(myTradingRecords);
    }

    public static void printBollingerAndStochasticBuyAndSellEventsFor(
            List<String> tickersToBuyWithStopLoss,
            List<String> tickersToSellWithStopLoss,
            String date) throws IOException {
        int countOfTickerWithNoInfo = 0;
        for (String str: tickersToBuyWithStopLoss) {
            String ticker = str.split(",")[0];
            String stopLoss = str.split(",")[1];
            BarSeries series = TechnicalAnalysisUtils.getBarSeriesFor(ticker, 23, date);
            if (series == null) {
                countOfTickerWithNoInfo++;
                System.out.println(countOfTickerWithNoInfo + ". No info for " + ticker);
                continue;
            }
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            LowPriceIndicator lowPrice = new LowPriceIndicator(series);
            SMAIndicator sma = new SMAIndicator(closePrice, 20);
            StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(closePrice, 20);
            BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma);
            BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, standardDeviation);
            StochasticOscillatorKIndicator sok = new StochasticOscillatorKIndicator(series, 20);
            StochasticOscillatorDIndicator sod = new StochasticOscillatorDIndicator(sok);
            BollingerAndStochasticBuyMyRule buyMyRule = new BollingerAndStochasticBuyMyRule(
                    sok,
                    sod,
                    bbl,
                    lowPrice,
                    20,
                    3
            );
            if (buyMyRule.isSatisfied(series.getEndIndex())) {
                System.out.println("!!!");
                System.out.println("Buy " + ticker + " and set " + stopLoss + "% stop loss");
                System.out.println("!!!");
                System.out.println("");
            }
            if (tickersToSellWithStopLoss.contains(ticker)) {
                HighPriceIndicator highPrice = new HighPriceIndicator(series);
                BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, standardDeviation);
                Rule sellingRule = new CrossedUpIndicatorRule(highPrice, bbu)
                        .or(new CrossedDownIndicatorRule(highPrice, bbm));
                if (sellingRule.isSatisfied(series.getEndIndex())) {
                    System.out.println("!!!");
                    System.out.println("Sell(close position) " + ticker);
                    System.out.println("!!!");
                    System.out.println("");
                }
            }
        }
    }

    public static void printBollingerAndRsiOrStochastic(String date) throws IOException {
        List<String> tickers = readFileFromResourcesToList("strategies/BollingerBandsRsiStochastics2.csv");
        //List<String> tickers = Arrays.asList("CEIX");
        for (String ticker : tickers) {
            try {
                BarSeries series = TechnicalAnalysisUtils.getBarSeriesFor(ticker, 60, date);
                ClosePriceIndicator close = new ClosePriceIndicator(series);
                RSIIndicator rsi = new RSIIndicator(close, 14);
                SMAIndicator sma = new SMAIndicator(close, 20);
                StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(close, 20);
                BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma);
                BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, standardDeviation);
                BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, standardDeviation);
                StochasticOscillatorKIndicator sok = new StochasticOscillatorKIndicator(series, 20);
                boolean firstRsiLow = false, firstRsiHigh = false, firstStochasticLow = false, firstStochasticHigh = false;
                for (int i = series.getEndIndex() - 15; i <= series.getEndIndex() - 5; i++) {
                    if (rsi.getValue(i).doubleValue() < 32 &&
                            close.getValue(i).isLessThan(bbl.getValue(i))) {
                        firstRsiLow = true;
                    } else if (rsi.getValue(i).doubleValue() > 68 &&
                            close.getValue(i).isGreaterThan(bbu.getValue(i))) {
                        firstRsiHigh = true;
                    }

                    if (sok.getValue(i).doubleValue() < 20 &&
                            close.getValue(i).isLessThan(bbl.getValue(i))) {
                        firstStochasticLow = true;
                    } else if (sok.getValue(i).doubleValue() > 80 &&
                            close.getValue(i).isGreaterThan(bbu.getValue(i))) {
                        firstStochasticHigh = true;
                    }
                }

                if (firstRsiLow) {
                    if ((getKOfSupportLine(close, 15) < 0 && getKOfSupportLine(rsi, 15) > 0)
                            || (getKOfSupportLine(close, 10) < 0 && getKOfSupportLine(rsi, 10) > 0)
                            || (getKOfSupportLine(close, 5) < 0 && getKOfSupportLine(rsi, 5) > 0)) {
                        System.out.println("Buy " + ticker + " (RSI)");
                    }
                }
                if (firstStochasticLow) {
                    if ((getKOfSupportLine(close, 15) < 0 && getKOfSupportLine(sok, 15) > 0)
                            || (getKOfSupportLine(close, 10) < 0 && getKOfSupportLine(sok, 10) > 0)
                            || (getKOfSupportLine(close, 5) < 0 && getKOfSupportLine(sok, 5) > 0)) {
                        System.out.println("Buy " + ticker + " (StochasticOscillator)");
                    }
                }
                if (firstRsiHigh) {
                    if ((getKOfResistanceLine(close, 15) > 0 && getKOfResistanceLine(rsi, 15) < 0)
                            || (getKOfResistanceLine(close, 10) > 0 && getKOfResistanceLine(rsi, 10) < 0)
                            || (getKOfResistanceLine(close, 5) > 0 && getKOfResistanceLine(rsi, 5) < 0)) {
                        System.out.println("Sell " + ticker + " (RSI)");
                    }
                }
                if (firstStochasticHigh) {
                    if ((getKOfResistanceLine(close, 15) > 0 && getKOfResistanceLine(sok, 15) < 0)
                            || (getKOfResistanceLine(close, 10) > 0 && getKOfResistanceLine(sok, 10) < 0)
                            || (getKOfResistanceLine(close, 5) > 0 && getKOfResistanceLine(sok, 5) < 0)) {
                        System.out.println("Sell " + ticker + " (StochasticOscillator)");
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    public static int printBollingerAndRsiOrStochasticOrHammerStrict(List<String> tickers, String date) throws IOException {
        int period = 60;
        int lastIndex = period - 1;
        int minLastIndexForRange = period - 5;
        int amountTickersOfFilteredByVolume = 0, amountOfTickersFileWasNotFoundFor = 0, amountOfTickersWithLackOfData = 0, totalAmountOfSignals = 0;
        for (String ticker : tickers) {
            try {
                BarSeries series = TechnicalAnalysisUtils.getBarSeriesFor(ticker, period, date);
                ClosePriceIndicator close = new ClosePriceIndicator(series);
                VolumeIndicator volume = new VolumeIndicator(series);
                if (volume.getValue(lastIndex).multipliedBy(new SMAIndicator(close, 40).getValue(lastIndex))
                        .isLessThan(volume.numOf(2000000))) {
                    continue;
                }
                amountTickersOfFilteredByVolume++;

                RSIIndicator rsi = new RSIIndicator(close, 14);
                SMAIndicator sma = new SMAIndicator(close, 20);
                StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(close, 20);
                BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma);
                BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, standardDeviation);
                BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, standardDeviation);
                StochasticOscillatorKIndicator sok = new StochasticOscillatorKIndicator(series, 20);

                List<int[]> rangesWhereRsiBelow32 = getRangesWhereRsiBelow32(rsi, lastIndex, 30);
                if (rangesWhereRsiBelow32.size() > 1 && rangesWhereRsiBelow32.get(0)[1] >= minLastIndexForRange) {
                    boolean lastMinBelowBbl = false;
                    for (int index = rangesWhereRsiBelow32.get(0)[0]; index <= rangesWhereRsiBelow32.get(0)[1]; index++) {
                        if (close.getValue(index).isLessThan(bbl.getValue(index))) {
                            lastMinBelowBbl = true;
                            break;
                        }
                    }
                    boolean prevLastMinBelowBbl = false;
                    for (int index = rangesWhereRsiBelow32.get(1)[0]; index <= rangesWhereRsiBelow32.get(1)[1]; index++) {
                        if (close.getValue(index).isLessThan(bbl.getValue(index))) {
                            prevLastMinBelowBbl = true;
                            break;
                        }
                    }
                    double minLastRsi = inditatorToList(rsi, rangesWhereRsiBelow32.get(0)[0], rangesWhereRsiBelow32.get(0)[1]).stream().min(Double::compareTo).get();
                    double minPrevLastRsi = inditatorToList(rsi, rangesWhereRsiBelow32.get(1)[0], rangesWhereRsiBelow32.get(1)[1]).stream().min(Double::compareTo).get();
                    double minLastPrice = inditatorToList(close, rangesWhereRsiBelow32.get(0)[0], rangesWhereRsiBelow32.get(0)[1]).stream().min(Double::compareTo).get();
                    double minPrevLastPrice = inditatorToList(close, rangesWhereRsiBelow32.get(1)[0], rangesWhereRsiBelow32.get(1)[1]).stream().min(Double::compareTo).get();
                    if (lastMinBelowBbl && prevLastMinBelowBbl
                            && minLastPrice < minPrevLastPrice * 0.98
                            && minLastRsi > minPrevLastRsi + 1
                            && rangesWhereRsiBelow32.get(0)[1] < lastIndex) {
                        System.out.println("Buy " + ticker + " (RSI Divergence); Last Index = " + rangesWhereRsiBelow32.get(0)[1]);
                        totalAmountOfSignals++;
                    }
                }

                List<int[]> rangesWhereRsiAbove68 = getRangesWhereRsiAbove68(rsi, lastIndex, 30);
                if (rangesWhereRsiAbove68.size() > 1 && rangesWhereRsiAbove68.get(0)[1] >= minLastIndexForRange) {
                    boolean lastMaxAboveBbu = false;
                    for (int index = rangesWhereRsiAbove68.get(0)[0]; index <= rangesWhereRsiAbove68.get(0)[1]; index++) {
                        if (close.getValue(index).isGreaterThan(bbu.getValue(index))) {
                            lastMaxAboveBbu = true;
                            break;
                        }
                    }
                    boolean prevLastMaxAboveBbu = false;
                    for (int index = rangesWhereRsiAbove68.get(1)[0]; index <= rangesWhereRsiAbove68.get(1)[1]; index++) {
                        if (close.getValue(index).isGreaterThan(bbu.getValue(index))) {
                            prevLastMaxAboveBbu = true;
                            break;
                        }
                    }
                    double maxLastRsi = inditatorToList(rsi, rangesWhereRsiAbove68.get(0)[0], rangesWhereRsiAbove68.get(0)[1]).stream().max(Double::compareTo).get();
                    double maxPrevLastRsi = inditatorToList(rsi, rangesWhereRsiAbove68.get(1)[0], rangesWhereRsiAbove68.get(1)[1]).stream().max(Double::compareTo).get();
                    double maxLastPrice = inditatorToList(close, rangesWhereRsiAbove68.get(0)[0], rangesWhereRsiAbove68.get(0)[1]).stream().max(Double::compareTo).get();
                    double maxPrevLastPrice = inditatorToList(close, rangesWhereRsiAbove68.get(1)[0], rangesWhereRsiAbove68.get(1)[1]).stream().max(Double::compareTo).get();
                    if (lastMaxAboveBbu && prevLastMaxAboveBbu
                            && maxLastPrice > maxPrevLastPrice * 1.02
                            && maxLastRsi < maxPrevLastRsi - 1
                            && rangesWhereRsiAbove68.get(0)[1] < lastIndex) {
                        System.out.println("Sell " + ticker + " (RSI Divergence); Last Index = " + rangesWhereRsiAbove68.get(0)[1]);
                        totalAmountOfSignals++;
                    }
                }

                List<int[]> rangesWhereStochasticBelow20 = getRangesWhereStochasticBelow20(sok, lastIndex, 30);
                if (rangesWhereStochasticBelow20.size() > 1 && rangesWhereStochasticBelow20.get(0)[1] >= minLastIndexForRange) {
                    boolean lastMinBelowBbl = false;
                    for (int index = rangesWhereStochasticBelow20.get(0)[0]; index <= rangesWhereStochasticBelow20.get(0)[1]; index++) {
                        if (close.getValue(index).isLessThan(bbl.getValue(index))) {
                            lastMinBelowBbl = true;
                            break;
                        }
                    }
                    boolean prevLastMinBelowBbl = false;
                    for (int index = rangesWhereStochasticBelow20.get(1)[0]; index <= rangesWhereStochasticBelow20.get(1)[1]; index++) {
                        if (close.getValue(index).isLessThan(bbl.getValue(index))) {
                            prevLastMinBelowBbl = true;
                            break;
                        }
                    }
                    double minLastStochastic = inditatorToList(sok, rangesWhereStochasticBelow20.get(0)[0], rangesWhereStochasticBelow20.get(0)[1]).stream().min(Double::compareTo).get();
                    double minPrevStochastic = inditatorToList(sok, rangesWhereStochasticBelow20.get(1)[0], rangesWhereStochasticBelow20.get(1)[1]).stream().min(Double::compareTo).get();
                    double minLastPrice = inditatorToList(close, rangesWhereStochasticBelow20.get(0)[0], rangesWhereStochasticBelow20.get(0)[1]).stream().min(Double::compareTo).get();
                    double minPrevLastPrice = inditatorToList(close, rangesWhereStochasticBelow20.get(1)[0], rangesWhereStochasticBelow20.get(1)[1]).stream().min(Double::compareTo).get();
                    if (lastMinBelowBbl && prevLastMinBelowBbl
                            && minLastPrice < minPrevLastPrice * 0.98
                            && minLastStochastic > minPrevStochastic + 0.7
                            && rangesWhereStochasticBelow20.get(0)[1] < lastIndex) {
                        System.out.println("Buy " + ticker + " (Stochastic Divergence); Last Index = " + rangesWhereStochasticBelow20.get(0)[1]);
                        totalAmountOfSignals++;
                    }
                }

                List<int[]> rangesWhereStochasticAbove80 = getRangesWhereStochasticAbove80(sok, lastIndex, 30);
                if (rangesWhereStochasticAbove80.size() > 1 && rangesWhereStochasticAbove80.get(0)[1] >= minLastIndexForRange) {
                    boolean lastMaxAboveBbu = false;
                    for (int index = rangesWhereStochasticAbove80.get(0)[0]; index <= rangesWhereStochasticAbove80.get(0)[1]; index++) {
                        if (close.getValue(index).isGreaterThan(bbu.getValue(index))) {
                            lastMaxAboveBbu = true;
                            break;
                        }
                    }
                    boolean prevLastMaxAboveBbu = false;
                    for (int index = rangesWhereStochasticAbove80.get(1)[0]; index <= rangesWhereStochasticAbove80.get(1)[1]; index++) {
                        if (close.getValue(index).isGreaterThan(bbu.getValue(index))) {
                            prevLastMaxAboveBbu = true;
                            break;
                        }
                    }
                    double maxLastStochastic = inditatorToList(sok, rangesWhereStochasticAbove80.get(0)[0], rangesWhereStochasticAbove80.get(0)[1]).stream().max(Double::compareTo).get();
                    double maxPrevLastStochastic = inditatorToList(sok, rangesWhereStochasticAbove80.get(1)[0], rangesWhereStochasticAbove80.get(1)[1]).stream().max(Double::compareTo).get();
                    double maxLastPrice = inditatorToList(close, rangesWhereStochasticAbove80.get(0)[0], rangesWhereStochasticAbove80.get(0)[1]).stream().max(Double::compareTo).get();
                    double maxPrevLastPrice = inditatorToList(close, rangesWhereStochasticAbove80.get(1)[0], rangesWhereStochasticAbove80.get(1)[1]).stream().max(Double::compareTo).get();
                    if (lastMaxAboveBbu && prevLastMaxAboveBbu
                            && maxLastPrice > maxPrevLastPrice * 1.02
                            && maxLastStochastic < maxPrevLastStochastic - 0.7
                            && rangesWhereStochasticAbove80.get(0)[1] < lastIndex) {
                        System.out.println("Sell " + ticker + " (Stochastic Divergence); Last Index = " + rangesWhereStochasticAbove80.get(0)[1]);
                        totalAmountOfSignals++;
                    }
                }

                int countOfRedCandles = 0;
                if (isCandleRed(series.getBar(lastIndex - 1))) {
                    countOfRedCandles++;
                }
                if (isCandleRed(series.getBar(lastIndex - 2))) {
                    countOfRedCandles++;
                }
                if (isCandleRed(series.getBar(lastIndex - 3))) {
                    countOfRedCandles++;
                }
                if (isBullishShavenTopWithLongShadow(series.getBar(lastIndex))
                        && countOfRedCandles >= 2
                        && series.getBar(lastIndex - 1).getClosePrice().isLessThan(series.getBar(lastIndex - 2).getClosePrice())
                        && series.getBar(lastIndex - 2).getClosePrice().isLessThan(series.getBar(lastIndex - 3).getClosePrice())) {
                    System.out.println("Buy " + ticker + " (Hammer pattern)");
                    totalAmountOfSignals++;
                }

                int countOfGreenCandles = 0;
                if (!isCandleRed(series.getBar(lastIndex - 1))) {
                    countOfGreenCandles++;
                }
                if (!isCandleRed(series.getBar(lastIndex - 2))) {
                    countOfGreenCandles++;
                }
                if (!isCandleRed(series.getBar(lastIndex - 3))) {
                    countOfGreenCandles++;
                }
                if (isBearishShavenTopWithLongShadow(series.getBar(lastIndex))
                        && countOfGreenCandles >= 2
                        && series.getBar(lastIndex - 1).getClosePrice().isGreaterThan(series.getBar(lastIndex - 2).getClosePrice())
                        && series.getBar(lastIndex - 2).getClosePrice().isGreaterThan(series.getBar(lastIndex - 3).getClosePrice())) {
                    System.out.println("Sell " + ticker + " (Hanging man pattern)");
                    totalAmountOfSignals++;
                }

                countOfRedCandles = 0;
                if (isCandleRed(series.getBar(lastIndex - 2))) {
                    countOfRedCandles++;
                }
                if (isCandleRed(series.getBar(lastIndex - 3))) {
                    countOfRedCandles++;
                }
                if (isCandleRed(series.getBar(lastIndex - 4))) {
                    countOfRedCandles++;
                }
                if (isCandleRed(series.getBar(lastIndex - 1)) && !isCandleRed(series.getBar(lastIndex))
                        && countOfRedCandles >= 2
                        && series.getBar(lastIndex - 1).getOpenPrice().isGreaterThan(series.getBar(lastIndex).getHighPrice())
                        && series.getBar(lastIndex - 1).getClosePrice().isLessThan(series.getBar(lastIndex).getLowPrice())) {
                    System.out.println("Buy " + ticker + " (Harami pattern)");
                    totalAmountOfSignals++;
                }

                countOfGreenCandles = 0;
                if (!isCandleRed(series.getBar(lastIndex - 2))) {
                    countOfGreenCandles++;
                }
                if (!isCandleRed(series.getBar(lastIndex - 3))) {
                    countOfGreenCandles++;
                }
                if (!isCandleRed(series.getBar(lastIndex - 4))) {
                    countOfGreenCandles++;
                }
                if (!isCandleRed(series.getBar(lastIndex - 1)) && isCandleRed(series.getBar(lastIndex))
                        && countOfGreenCandles >= 2
                        && series.getBar(lastIndex - 1).getHighPrice().isLessThan(series.getBar(lastIndex).getOpenPrice())
                        && series.getBar(lastIndex - 1).getLowPrice().isGreaterThan(series.getBar(lastIndex).getClosePrice())) {
                    System.out.println("Sell " + ticker + " (Bearish Engulfing pattern)");
                    totalAmountOfSignals++;
                }

                countOfRedCandles = 0;
                if (isCandleRed(series.getBar(lastIndex - 2))) {
                    countOfRedCandles++;
                }
                if (isCandleRed(series.getBar(lastIndex - 3))) {
                    countOfRedCandles++;
                }
                if (isCandleRed(series.getBar(lastIndex - 4))) {
                    countOfRedCandles++;
                }
                if (isCandleRed(series.getBar(lastIndex - 1)) && !isCandleRed(series.getBar(lastIndex))
                        && countOfRedCandles >= 2
                        && series.getBar(lastIndex - 1).getHighPrice().isLessThan(series.getBar(lastIndex).getClosePrice())
                        && series.getBar(lastIndex - 1).getLowPrice().isGreaterThan(series.getBar(lastIndex).getOpenPrice())) {
                    System.out.println("Buy " + ticker + " (Bullish Engulfing pattern)");
                    totalAmountOfSignals++;
                }
            } catch (NullPointerException e) {
                amountOfTickersWithLackOfData++;
            } catch (FileNotFoundException e) {
                amountOfTickersFileWasNotFoundFor++;
            } catch (Exception e) {
                System.out.println("exception on " + ticker);
                System.out.println(e.getClass().getName());
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Total amount of tickers - " + tickers.size());
        System.out.println("Amount of tickers filtered by amount - " + amountTickersOfFilteredByVolume);
        System.out.println("Amount of tickers where were not enough candles data - " + amountOfTickersWithLackOfData);
        System.out.println("Amount of tickers file was not fount for - " + amountOfTickersFileWasNotFoundFor);
        return totalAmountOfSignals;
    }

    public static void printBollingerStrategy2() throws IOException {
        List<MyTradingRecord> myTradingRecords = new ArrayList<>();
        List<String> tickers = readFileFromResourcesToList("tickers.txt");
        int period = 20;
        for (int i = 0; i < 2000; i++) {
            System.out.println(tickers.get(i));
            // Read data and calculate indicators
            BarSeries series = null;
            try {
                series = fillDataFor(tickers.get(i));
            } catch (IOException e) {
                continue;
            } catch (JsonPathException e) {
                continue;
            }
            // Filter stocks with low liquidity
            if (series.getBarData().stream().filter(s -> s.getVolume().doubleValue() < 50).toList().size() > 10) {
                continue;
            }
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            SMAIndicator sma = new SMAIndicator(closePrice, period);
            StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(closePrice, period);
            BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma);
            BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, standardDeviation);
            BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, standardDeviation);
            // Run 4 strategies with different stop losses (6, 8, 10, 12)
            for (int stopLossPercent = 6; stopLossPercent <= 12; stopLossPercent += 2) {
                Bollinger3InARowUpperRule buyRule = new Bollinger3InARowUpperRule(closePrice, bbu);
                Rule sellRule = new CrossedDownIndicatorRule(closePrice, sma)
                        .or(new StopLossRule(closePrice, stopLossPercent));
                Strategy strategy2 = new BaseStrategy(buyRule, sellRule);
                BarSeriesManager manager2 = new BarSeriesManager(series);
                TradingRecord tradingRecord2 = manager2.run(strategy2);
                List<MyPosition> list2 = new ArrayList<>();
                for(Position position : tradingRecord2.getPositions()) {
                    MyPosition myPosition = new MyPosition(Trade.TradeType.BUY, position.getEntry().getIndex(), position.getExit().getIndex(), position.getEntry().getPricePerAsset().doubleValue(), position.getExit().getPricePerAsset().doubleValue());
                    list2.add(myPosition);
                }
                myTradingRecords.add(new MyTradingRecord(tickers.get(i), stopLossPercent, list2));
            }
        }
        ExcelUtils.writeResultsOfBuyByBollingerAndStochastic(myTradingRecords);
    }

    public static Props getProps() {
        return props;
    }

    private static void loadProperties() throws IOException {
        InputStream input = Main.class.getClassLoader().getResourceAsStream(LOCAL_CONFIG_PROPERTIES);
        Properties properties = new Properties();
        properties.load(input);
        props = new Props();
        props.setMysqlConnectionString(properties.getProperty("mysql.connection.string"));
        props.setMysqlUsername(properties.getProperty("mysql.username"));
        props.setMysqlPassword(properties.getProperty("mysql.password"));
        props.setStartPageIndex(Integer.parseInt(properties.getProperty("startPageIndex")));
        props.setDate(properties.getProperty("date"));
        props.setPathToFreshPricesFolder(properties.getProperty("pathToFreshPricesFolder"));
        input.close();
    }
}