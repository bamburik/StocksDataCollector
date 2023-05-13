package org.bamburov;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bamburov.models.StockDailyInfo;
import org.bamburov.models.StockInfo2022;
import org.bamburov.models.TechnicalAnalysis;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.bamburov.utils.ApiUtils.*;
import static org.bamburov.utils.FileUtils.readFileToList;
import static org.bamburov.utils.MySqlUtils.*;

public class Main {
    private static final String LOCAL_CONFIG_PROPERTIES = "config.properties";
    private static Props props;
    private static Logger logger = LogManager.getRootLogger();
    public static void main(String[] args) throws Exception {
        try {
            loadProperties();

            createMySqlConnection();
            //fulfillInfo2022();
            fulfillDaily();
        } finally {
            closeMySqlConnection();
        }
    }

    private static void fulfillDaily() throws Exception {
        List<String> tickers = readFileToList("tickers.txt");
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
                        Double p2bCalc = (dailyInfo.getCurrentPrice() * sharesOutStanding)/(totalAssets - totalLiabilities);
                        dailyInfo.setP2bCalc(Double.isInfinite(p2bCalc) || Double.isNaN(p2bCalc) || Double.isFinite(p2bCalc) ? null : p2bCalc);
                    }
                    if (dailyInfo.getCurrentPrice() != null &&
                            yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().isPresent() &&
                            yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getTotalRevenue() != null &&
                            yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getSharesOutstanding() != null) {
                        long totalRevenue = yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getTotalRevenue();
                        long sharesOutStanding = yearInfo.stream().filter(x -> x.getStock().equals(dailyInfo.getStock())).findFirst().get().getSharesOutstanding();
                        Double p2sCalc = (dailyInfo.getCurrentPrice() * sharesOutStanding)/totalRevenue;
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
        input.close();
    }
}