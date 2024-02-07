package org.bamburov.utils;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bamburov.models.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.bamburov.Main.getProps;
import static org.bamburov.utils.Constants.*;

public class ApiUtils {
    private static String dateNow;
    private static String request;
    private static Response response;
    private static RestAssuredConfig config = RestAssured.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                    .setParam(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000)
                    .setParam(CoreConnectionPNames.SO_TIMEOUT, 60000));
    private static Logger logger = LogManager.getLogger(ApiUtils.class);

    public static List<String> getStockTickers(int page) throws Exception {
        return (List<String>) executeWithRetries(
                ApiUtils::getStockTickersWithoutRetry, page, List.class, 3, 10000
        );
    }

    public static StockDailyInfo getStockDailyInfo(String stockTicker) throws Exception {
        return executeWithRetries(
                ApiUtils::getStockDailyInfoWithoutRetry, stockTicker, StockDailyInfo.class, 3, 10000
        );
    }

    public static Response getStockDailyInfoResponse(String stockTicker) throws Exception {
        return executeWithRetries(
                ApiUtils::getStockDailyInfoResponseWithoutRetry, stockTicker, Response.class, 3, 10000
        );
    }

    public static Map<String, TechnicalAnalysis> getTechnicalAnalysis(List<String> stockTickers, String timeFrame) throws Exception {
        return (Map<String, TechnicalAnalysis>) executeWithRetries(
                (tickers) -> getTechnicalAnalysisWithoutRetry(tickers, timeFrame), stockTickers, Map.class, 3, 10000
        );
    }

    public static StockInfo2022 getStockYearInfo(String stockTicker) throws Exception {
        return executeWithRetries(
                ApiUtils::getStockYearInfoWithoutRetry, stockTicker, StockInfo2022.class, 3, 10000
        );
    }

    public static Response getPriceAndVolumeHistory(String stockTicker) throws Exception {
        return executeWithRetries(
                ApiUtils::getPriceAndVolume150DaysHistoryWithoutRetry, stockTicker, Response.class, 3, 10000
        );
    }

    private static Long getLong(JsonPath jsonpath, String path) {
        try {
            return jsonpath.getLong(path);
        } catch (Exception e) {
            return null;
        }
    }

    private static Double getDouble(JsonPath jsonpath, String path) {
        try {
            return jsonpath.getDouble(path);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getString(JsonPath jsonpath, String path) {
        try {
            return jsonpath.getString(path);
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer getInteger(JsonPath jsonpath, String path) {
        try {
            return jsonpath.getInt(path);
        } catch (Exception e) {
            return null;
        }
    }

    private static Boolean getBoolean(JsonPath jsonpath, String path) {
        try {
            return jsonpath.getBoolean(path);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getDateNow() {
        if (dateNow == null) {
            if (getProps().getDate().equals("")) {
                dateNow = LocalDateTime.now().minus(12, ChronoUnit.HOURS).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                dateNow = getProps().getDate();
            }
        }
        return dateNow;
    }

    private static TechnicalAnalysis getTechnicalAnalysis(String stockTicker, String timeFrame) throws Exception {
        request = String.format(GET_TECHNICAL_ANALYSIS_URL_FORMAT, stockTicker, timeFrame);
        response = given()
                .config(config)
                .header("User-Agent", "Mozilla/5.0")
                .get(request);
        if (response.statusCode() != 200/* || !isJsonValid(response.getBody().asString())*/) {
            throw new Exception();
        }
        JsonPath jsonpath = response.jsonPath();
        Integer summary = getInteger(jsonpath, String.format(TECHNICAL_SUMMARY_JSONPATH_FORMAT, stockTicker));
        Integer oscillators = getInteger(jsonpath, String.format(TECHNICAL_OSCILLATORS_JSONPATH_FORMAT, stockTicker));
        Integer moving = getInteger(jsonpath, String.format(TECHNICAL_MOVING_JSONPATH_FORMAT, stockTicker));
        if (summary == null && oscillators == null && moving == null) {
            throw new Exception();
        }
        return new TechnicalAnalysis(summary, oscillators, moving);
    }

    public static StockInfo2022 getStockYearInfoWithoutRetry(String stockTicker) throws Exception {
        request = String.format(STOCK_INFO_URL_FORMAT, stockTicker.toLowerCase());
        response = given()
                .config(config)
                .header("User-Agent", "Mozilla/5.0")
                .get(request);
        if (response.statusCode() == 404) {
            logger.error("Ticker info of " + stockTicker + " not found");
            return null;
        }
        if (response.statusCode() != 200/* || !isJsonValid(response.getBody().asString())*/) {
            throw new Exception();
        }

        JsonPath jsonpath = response.jsonPath();
        return new StockInfo2022(
                stockTicker,
                getString(jsonpath, "common.stock.sector"),
                getString(jsonpath, "common.stock.industry"),
                getLong(jsonpath, TOTAL_ASSETS_JSONPATH),
                getLong(jsonpath, TOTAL_LIABILITIES_JSONPATH),
                getLong(jsonpath, TOTAL_REVENUE_JSONPATH),
                getLong(jsonpath, SHARES_OUTSTANDING_JSONPATH)
        );
    }

    private static Map<String, TechnicalAnalysis> getTechnicalAnalysisWithoutRetry(List<String> stockTickers, String timeFrame) throws Exception {
        Map<String, TechnicalAnalysis> result = new HashMap<>();
        request = String.format(GET_TECHNICAL_ANALYSIS_URL_FORMAT, String.join(",", stockTickers.stream().map(String::toUpperCase).toList()), timeFrame);
        response = given()
                .config(config)
                .header("User-Agent", "Mozilla/5.0").
                get(request);
        if (response.statusCode() != 200/* || !isJsonValid(response.getBody().asString())*/) {
            throw new Exception();
        }
        JsonPath jsonpath = response.jsonPath();
        for (String ticker : stockTickers) {
            Integer summary = getInteger(jsonpath, String.format(TECHNICAL_SUMMARY_JSONPATH_FORMAT, ticker));
            Integer oscillators = getInteger(jsonpath, String.format(TECHNICAL_OSCILLATORS_JSONPATH_FORMAT, ticker));
            Integer moving = getInteger(jsonpath, String.format(TECHNICAL_MOVING_JSONPATH_FORMAT, ticker));
//                        if (summary == null && oscillators == null && moving == null) {
//                            try {
//                                result.put(ticker, executeWithRetries(
//                                        (t) -> {
//                                            return getTechnicalAnalysis(t.toUpperCase(), timeFrame);
//                                        }, ticker, TechnicalAnalysis.class, 2, 2000
//                                ));
//                            }
//                            catch (Exception e) {
//                                continue;
//                            }
//                        } else {
            result.put(ticker, new TechnicalAnalysis(summary, oscillators, moving));
//                        }
        }
        return result;
    }

    public static StockDailyInfo getStockDailyInfoWithoutRetry(String stockTicker) throws Exception {
        request = String.format(STOCK_INFO_URL_FORMAT, stockTicker.toLowerCase());
        response = getStockDailyInfoResponseWithoutRetry(stockTicker);

        JsonPath jsonpath = response.jsonPath();
        TechnicalAnalysis dayAnalysis = null;
        if (jsonpath.getBoolean(TECHNICAL_IS_HAVE_DATA_FORMAT)) {
            dayAnalysis = new TechnicalAnalysis(
                    getInteger(jsonpath, TECHNICAL_DAY_SUMMARY_JSONPATH),
                    getInteger(jsonpath, TECHNICAL_DAY_OSCILLATORS_JSONPATH),
                    getInteger(jsonpath, TECHNICAL_DAY_MOVING_JSONPATH)
            );
        }
        return new StockDailyInfo(
                stockTicker,
                getInteger(jsonpath, SCORE_JSONPATH),
                getDateNow(),
                getDouble(jsonpath, CURRENT_PRICE_JSONPATH),
                getDouble(jsonpath, PRICE_TARGET_JSONPATH),
                getDouble(jsonpath, BEST_PRICE_TARGET_JSONPATH),
                getString(jsonpath, ANALYST_CONSENSUS_JSONPATH),
                getString(jsonpath, BEST_ANALYST_CONSENSUS_JSONPATH),
                getDouble(jsonpath, P2E_JSONPATH),
                null,
                getDouble(jsonpath, P2B_JSONPATH),
                null,
                getDouble(jsonpath, P2S_JSONPATH),
                null,
                getDouble(jsonpath, P2FCF_JSONPATH),
                getDouble(jsonpath, P2CF_JSONPATH),
                getDouble(jsonpath, EPS_JSONPATH),
                getString(jsonpath, BLOGGER_SENTIMENT_JSONPATH),
                getString(jsonpath, HEDGE_FUND_TREND_JSONPATH),
                getString(jsonpath, BEST_CROWD_WISDOM_JSONPATH),
                getString(jsonpath, CROWD_WISDOM_JSONPATH),
                getString(jsonpath, NEWS_SENTIMENT_JSONPATH),
                getString(jsonpath, TECHNICAL_SMA_JSONPATH),
                getBoolean(jsonpath, TECHNICAL_IS_HAVE_DATA_FORMAT),
                dayAnalysis, null, null,
                getInteger(jsonpath, VOLUME));
    }

    private static List<String> getStockTickersWithoutRetry(int page) throws Exception {
        request = String.format(GET_STOCKS_URL_FORMAT, page);
        response = given()
                .config(config)
                .header("User-Agent", "Mozilla/5.0")
                .get(request);
        if (ApiUtils.response.statusCode() != 200/* || !isJsonValid(response.getBody().asString())*/) {
            throw new Exception();
        }
        JsonPath jsonpath = response.jsonPath();
        return jsonpath.getList(TICKERS_JSONPATH, String.class);
    }

    private static Response getStockDailyInfoResponseWithoutRetry(String stockTicker) throws Exception {
        request = String.format(STOCK_INFO_URL_FORMAT, stockTicker.toLowerCase());
        Response result = given()
                .config(config)
                .header("User-Agent", "Mozilla/5.0")
                .get(request);
        if (result.statusCode() == 404) {
            logger.error("Ticker info of " + stockTicker + " not found");
            return null;
        }
        if (result.statusCode() != 200/* || !isJsonValid(response.getBody().asString())*/) {
            throw new Exception();
        }
        return result;
    }

    private static Response getPriceAndVolumeHistoryWithoutRetry(String stockTicker, int daysBack) throws Exception {
        request = String.format(PRICE_AND_VOLUME_HISTORY_URL_FORMAT, stockTicker.toLowerCase(), daysBack);
        response = given()
                .config(config)
                .header("User-Agent", "Mozilla/5.0")
                .get(request);
        if (response.statusCode() == 404) {
            logger.error("Price and volume history of " + stockTicker + " ticker not found");
            return null;
        }
        if (response.statusCode() != 200/* || !isJsonValid(response.getBody().asString())*/) {
            throw new Exception();
        }
        return response;
    }

    private static Response getPriceAndVolume150DaysHistoryWithoutRetry(String stockTicker) throws Exception {
        return getPriceAndVolumeHistoryWithoutRetry(stockTicker, 150);
    }

    private static <T, O> O executeWithRetries(IFunctionalInterface<T, O> iFunctionalInterface, T input, Class<O> type, int maxTries, int pollingInterval) throws Exception {
        int count = 0;
        while (true) {
            try {
                return type.cast(iFunctionalInterface.doRetryableAction(input));
            } catch (Exception e) {
                Thread.sleep(pollingInterval);
                logger.error("Request - " + request +
                        "\nResponse status - " + response.statusCode() +
                        "\nResponse body - " + response.getBody().asPrettyString());
                if (++count == maxTries) {
                    return null;
                }
            }
        }
    }

    public static List<ScoreChangeEventInfo> setPriceTrend(List<ScoreChangeEventInfo> scoreChangeEventInfoList) throws Exception {
        List<String> tickers = scoreChangeEventInfoList.stream().map(ScoreChangeEventInfo::getTicker).distinct().toList();
        for(String ticker : tickers) {
            JsonPath jsonPath = null;
            try {
                jsonPath = getStockDailyInfoResponseWithoutRetry(ticker).jsonPath();
            } catch (Exception e) {
                continue;
            }
            System.out.println(ticker);
            for (ScoreChangeEventInfo scoreChangeEventInfo : scoreChangeEventInfoList.stream().filter(x -> x.getTicker().equals(ticker)).toList()) {
                LocalDate date;
                if (scoreChangeEventInfo.getDate().getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                    date = scoreChangeEventInfo.getDate().minusDays(1);
                } else if (scoreChangeEventInfo.getDate().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    date = scoreChangeEventInfo.getDate().minusDays(2);
                } else {
                    date = scoreChangeEventInfo.getDate();
                }
                String fullDate = getString(jsonPath, String.format(DAILY_INFO_FULL_DATE_BY_SHORT_DATE_FORMAT, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                for(int i = 0; i < 3 && fullDate == null; i++) {
                    date = date.minusDays(1);
                    fullDate = getString(jsonPath, String.format(DAILY_INFO_FULL_DATE_BY_SHORT_DATE_FORMAT, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                }
                if (fullDate == null) {
                    scoreChangeEventInfo.setScoreChangeTrend(Consensus.LACK_OF_INFO);
                    continue;
                }
                int index = getInteger(jsonPath, String.format(DAILY_INFO_INDEX_BY_FULL_DATE_FORMAT, fullDate));
                Double currentPrice = getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index));
                scoreChangeEventInfo.setCurrentPrice(currentPrice);
                int countOf0Volume = 0;
                List<Double> priceHistory = new ArrayList<>();
                StringBuilder stringPriceHistory = new StringBuilder();
                double minPrice = Double.MAX_VALUE;
                for (int i = index - 5; i < index; i++) {
                    Double nonNullablePrice = getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, i));
                    if (getLong(jsonPath, String.format(DAILY_INFO_VOLUME_BY_INDEX_FORMAT, index)) == 0 &&
                            (priceHistory.size() == 0 || priceHistory.get(priceHistory.size() - 1).equals(nonNullablePrice))) {
                        countOf0Volume++;
                    }
                    priceHistory.add(nonNullablePrice);
                    stringPriceHistory.append('-').append(nonNullablePrice == 0 ? "null" : nonNullablePrice);
                    if (nonNullablePrice != 0 && minPrice > nonNullablePrice) {
                        minPrice = nonNullablePrice;
                    }
                }
                scoreChangeEventInfo.setPriceAfter1day(getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index + 1)));
                scoreChangeEventInfo.setPriceAfter2day(getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index + 2)));
                scoreChangeEventInfo.setPriceAfter3day(getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index + 3)));
                scoreChangeEventInfo.setPriceAfter4day(getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index + 4)));
                scoreChangeEventInfo.setPriceAfter5day(getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index + 5)));
                scoreChangeEventInfo.setPriceAfter6day(getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index + 6)));
                scoreChangeEventInfo.setPriceAfter7day(getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index + 7)));
                scoreChangeEventInfo.setPriceAfter8day(getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index + 8)));
                scoreChangeEventInfo.setPriceAfter9day(getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index + 9)));
                scoreChangeEventInfo.setPriceAfter10day(getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index + 10)));
                if (currentPrice / minPrice > 1.05) {
                    scoreChangeEventInfo.setPriceChangeTrend(Consensus.STRONG_NO);
                } else if (currentPrice / minPrice > 1.03) {
                    scoreChangeEventInfo.setPriceChangeTrend(Consensus.NO);
                } else if (currentPrice / minPrice > 1.0) {
                    scoreChangeEventInfo.setPriceChangeTrend(Consensus.YES);
                } else {
                    scoreChangeEventInfo.setPriceChangeTrend(Consensus.STRONG_YES);
                }
                scoreChangeEventInfo.setPriceHistory(stringPriceHistory.deleteCharAt(0).toString());
                scoreChangeEventInfo.setLiquid(countOf0Volume <= 1);
            }
        }
        return scoreChangeEventInfoList;
    }

    public static List<TechnicalAnalysisChangeEventInfo> setPrices(List<TechnicalAnalysisChangeEventInfo> technicalAnalysisChangeEventInfoList) throws Exception {
        technicalAnalysisChangeEventInfoList = technicalAnalysisChangeEventInfoList.stream().filter(x -> !x.getEndDate().isEqual(LocalDate.now().minusDays(1))).collect(Collectors.toList());
        List<String> tickers = technicalAnalysisChangeEventInfoList.stream().map(TechnicalAnalysisChangeEventInfo::getTicker).distinct().toList();
        for(String ticker : tickers) {
            JsonPath jsonPath = null;
            try {
                jsonPath = getStockDailyInfoResponse(ticker).jsonPath();
            } catch (Exception e) {
                continue;
            }

            System.out.println(ticker);
            int lastPriceIndex = 0;

            // Check if stock is liquid
            // and remove events with this stock if stock is not liquid
            boolean isLiquid = true;
            System.out.println("check " + ticker);
            try {
                lastPriceIndex = getInteger(jsonPath, DAILY_INFO_LAST_INDEX_OF_PRICES_DATA) - 1;
                for (int i = lastPriceIndex; i >= lastPriceIndex - 100 && i >=0; i--) {
                    if (500 > getInteger(jsonPath, String.format(DAILY_INFO_VOLUME_BY_INDEX_FORMAT, i))) {
                        isLiquid = false;
                    }
                }
            } catch (Exception e) {
                isLiquid = false;
            }
            if (!isLiquid) {
                technicalAnalysisChangeEventInfoList = technicalAnalysisChangeEventInfoList.stream().filter(x -> !x.getTicker().equals(ticker)).collect(Collectors.toList());
                continue;
            }

            System.out.println(ticker + " is liquid");
            for (TechnicalAnalysisChangeEventInfo technicalAnalysisChangeEventInfo : technicalAnalysisChangeEventInfoList.stream().filter(x -> x.getTicker().equals(ticker)).toList()) {
                LocalDate date;
                if (technicalAnalysisChangeEventInfo.getStartDate().getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
                    date = technicalAnalysisChangeEventInfo.getStartDate().plusDays(3);
                } else if (technicalAnalysisChangeEventInfo.getStartDate().getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                    date = technicalAnalysisChangeEventInfo.getStartDate().plusDays(2);
                } else {
                    date = technicalAnalysisChangeEventInfo.getStartDate().plusDays(1);
                }
                String fullDate = getString(jsonPath, String.format(DAILY_INFO_FULL_DATE_BY_SHORT_DATE_FORMAT, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                for(int i = 0; i < 3 && fullDate == null; i++) {
                    date = date.minusDays(1);
                    fullDate = getString(jsonPath, String.format(DAILY_INFO_FULL_DATE_BY_SHORT_DATE_FORMAT, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                }
                if (fullDate == null) {
                    continue;
                }
                int index = getInteger(jsonPath, String.format(DAILY_INFO_INDEX_BY_FULL_DATE_FORMAT, fullDate));
                Double currentPrice = getDouble(jsonPath, String.format(DAILY_INFO_OPEN_BY_INDEX_FORMAT, index));
                technicalAnalysisChangeEventInfo.setBuyPrice(currentPrice);
                double closePrice = 0;
                double dynamicStopLoss = currentPrice * 0.98;
                while (lastPriceIndex > index && !technicalAnalysisChangeEventInfo.getEndDate().isBefore(LocalDate.parse(getString(jsonPath, String.format(DAILY_INFO_DATE_BY_INDEX_FORMAT, index)).split("T")[0], DateTimeFormatter.ofPattern("yyyy-MM-dd")))) {
                    index++;
                    double high = getDouble(jsonPath, String.format(DAILY_INFO_HIGH_BY_INDEX_FORMAT, index));
                    double low = getDouble(jsonPath, String.format(DAILY_INFO_LOW_BY_INDEX_FORMAT, index));
                    closePrice = getDouble(jsonPath, String.format(DAILY_INFO_PRICE_BY_INDEX_FORMAT, index));
                    if (low < dynamicStopLoss) {
                        technicalAnalysisChangeEventInfo.setSellPrice(dynamicStopLoss);
                        technicalAnalysisChangeEventInfo.setByStopLoss(true);
                        break;
                    }
                    dynamicStopLoss = high * 0.98;
                    technicalAnalysisChangeEventInfo.getPriceValues().add(closePrice);
                }
                if (!technicalAnalysisChangeEventInfo.isByStopLoss()){
                    technicalAnalysisChangeEventInfo.setSellPrice(closePrice);
                }
            }
        }
        return technicalAnalysisChangeEventInfoList;
    }

    public static boolean isLiquid(String ticker) throws Exception {
        System.out.println("check " + ticker);
        try {
            JsonPath jsonPath = getPriceAndVolumeHistoryWithoutRetry(ticker, 30).jsonPath();
            for (int i = 0; i < 20; i++) {
                if (500 > getInteger(jsonPath, String.format(PRICE_AND_VOLUME_HISTORY_VOLUME_BY_INDEX_FORMAT, i))) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static BarSeries getBarSeriesFor(String ticker, int period, String fromDate) {
        Response response = null;
        try {
            response = ApiUtils.getStockDailyInfoResponse(ticker);
        } catch (Exception e) {
            return null;
        }
        JsonPath jsonPath = response.jsonPath();
        String fullDate = getString(jsonPath, String.format(DAILY_INFO_FULL_DATE_BY_SHORT_DATE_FORMAT, fromDate));
        if (fullDate == null) {
            return null;
        }
        int index = getInteger(jsonPath, String.format(DAILY_INFO_INDEX_BY_FULL_DATE_FORMAT, fullDate));
        BarSeries result = new BaseBarSeries();
        for (int i = index - period; i <=index; i++) {
            result.addBar(
                    ZonedDateTime.parse(jsonPath.getString(String.format(DAILY_INFO_DATE_BY_INDEX_FORMAT, i))),
                    jsonPath.getDouble(String.format(DAILY_INFO_OPEN_BY_INDEX_FORMAT, i)),
                    jsonPath.getDouble(String.format(DAILY_INFO_HIGH_BY_INDEX_FORMAT, i)),
                    jsonPath.getDouble(String.format(DAILY_INFO_LOW_BY_INDEX_FORMAT, i)),
                    jsonPath.getDouble(String.format(DAILY_INFO_CLOSE_BY_INDEX_FORMAT, i)),
                    jsonPath.getLong(String.format(DAILY_INFO_VOLUME_BY_INDEX_FORMAT, i))
            );
        }
        return result;
    }

    private  static boolean isJsonValid(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            try {
                new JSONArray(json);
            } catch (JSONException ne) {
                return false;
            }
        }
        return true;
    }
}

