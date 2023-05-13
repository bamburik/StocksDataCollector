package org.bamburov.utils;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bamburov.models.StockDailyInfo;
import org.bamburov.models.StockInfo2022;
import org.bamburov.models.TechnicalAnalysis;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.bamburov.utils.Constants.*;

public class ApiUtils {
    private static String dateNow;
    private static String request;
    private static Response response;
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
            dateNow = LocalDateTime.now().minus(12, ChronoUnit.HOURS).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        return dateNow;
    }

    private static TechnicalAnalysis getTechnicalAnalysis(String stockTicker, String timeFrame) throws Exception {
        request = String.format(GET_TECHNICAL_ANALYSIS_URL_FORMAT, stockTicker, timeFrame);
        response = given()
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
        response = given()
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
                dayAnalysis, null, null);
    }

    private static List<String> getStockTickersWithoutRetry(int page) throws Exception {
        request = String.format(GET_STOCKS_URL_FORMAT, page);
        response = given()
                .header("User-Agent", "Mozilla/5.0")
                .get(request);
        if (ApiUtils.response.statusCode() != 200/* || !isJsonValid(response.getBody().asString())*/) {
            throw new Exception();
        }
        JsonPath jsonpath = response.jsonPath();
        return jsonpath.getList(TICKERS_JSONPATH, String.class);
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

