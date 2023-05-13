package org.bamburov.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bamburov.Props;
import org.bamburov.models.StockDailyInfo;
import org.bamburov.models.StockInfo2022;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.bamburov.Main.getProps;
import static org.bamburov.utils.FileUtils.readFileToString;

public class MySqlUtils {
    private static Logger logger = LogManager.getRootLogger();
    private static Connection connection;
    public static Connection getConnection() {
        return connection;
    }
    public static void createMySqlConnection() throws SQLException, ClassNotFoundException {
        Props props = getProps();
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(props.getMysqlConnectionString(), props.getMysqlUsername(), props.getMysqlPassword());
    }

    public static void closeMySqlConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static void insertToInfo2022(List<StockInfo2022> list) throws SQLException {
        StringBuilder builder = new StringBuilder();
        for (StockInfo2022 stock: list) {
            builder.append("(\"")
                    .append(stock.getStock())
                    .append("\",")
                    .append(stock.getSector() != null ? '"' + stock.getSector() + "\"," : "NULL,")
                    .append(stock.getIndustry() != null ? '"' + stock.getIndustry() + "\"," : "NULL,")
                    .append(stock.getTotalAssets()).append(',')
                    .append(stock.getTotalLiabilities()).append(',')
                    .append(stock.getTotalRevenue())
                    .append("),");
        }
        builder.deleteCharAt(builder.length() - 1);
        String query = String.format(readFileToString("insertToInfo2022.txt"), builder);
        Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            logger.error("failed to insert year info for " + String.join("\",\"", list.stream().map(StockInfo2022::getStock).toList()));
        }
    }

    public static void insertToDaily(List<StockDailyInfo> list) throws SQLException {
        StringBuilder builder = new StringBuilder();
        for (StockDailyInfo stock: list) {
            builder.append("(\"")
                    .append(stock.getStock())
                    .append("\",")
                    .append(stock.getScore() != null ? stock.getScore() + "," : "NULL,")
                    .append('"').append(stock.getDate()).append("\",")
                    .append(stock.getCurrentPrice() != null ? stock.getCurrentPrice() + "," : "NULL,")
                    .append(stock.getPriceTarget() != null ? stock.getPriceTarget() + "," : "NULL,")
                    .append(stock.getBestPriceTarget() != null ? stock.getBestPriceTarget() + "," : "NULL,")
                    .append(stock.getAnalystConsensus() != null ? '"' + stock.getAnalystConsensus() + "\"," : "NULL,")
                    .append(stock.getBestAnalystConsensus() != null ? '"' + stock.getBestAnalystConsensus() + "\"," : "NULL,")
                    .append(stock.getP2e() != null ? stock.getP2e() + "," : "NULL,")
                    .append(stock.getP2eCalc() != null ? stock.getP2eCalc() + "," : "NULL,")
                    .append(stock.getP2b() != null ? stock.getP2b() + "," : "NULL,")
                    .append(stock.getP2bCalc() != null ? stock.getP2bCalc() + "," : "NULL,")
                    .append(stock.getP2s() != null ? stock.getP2s() + "," : "NULL,")
                    .append(stock.getP2sCalc() != null ? stock.getP2sCalc() + "," : "NULL,")
                    .append(stock.getP2fcf() != null ? stock.getP2fcf() + "," : "NULL,")
                    .append(stock.getP2cf() != null ? stock.getP2cf() + "," : "NULL,")
                    .append(stock.getEps() != null ? stock.getEps() + "," : "NULL,")
                    .append(stock.getBloggerSentiment() != null ? '"' + stock.getBloggerSentiment() + "\"," : "NULL,")
                    .append(stock.getHedgeFundTrend() != null ? '"' + stock.getHedgeFundTrend() + "\"," : "NULL,")
                    .append(stock.getBestCrowdWisdom() != null ? '"' + stock.getBestCrowdWisdom() + "\"," : "NULL,")
                    .append(stock.getCrowdWisdom() != null ? '"' + stock.getCrowdWisdom() + "\"," : "NULL,")
                    .append(stock.getNewsSentiment() != null ? '"' + stock.getNewsSentiment() + "\"," : "NULL,")
                    .append(stock.getTechnicalSma() != null ? '"' + stock.getTechnicalSma() + "\"," : "NULL,")
                    .append(stock.getDayAnalysis() != null && stock.getDayAnalysis().getSummary() != null ? stock.getDayAnalysis().getSummary() + "," : "NULL,")
                    .append(stock.getDayAnalysis() != null && stock.getDayAnalysis().getOscillators() != null ? stock.getDayAnalysis().getOscillators() + "," : "NULL,")
                    .append(stock.getDayAnalysis() != null && stock.getDayAnalysis().getMoving() != null ? stock.getDayAnalysis().getMoving() + "," : "NULL,")
                    .append(stock.getWeekAnalysis() != null && stock.getWeekAnalysis().getSummary() != null ? stock.getWeekAnalysis().getSummary() + "," : "NULL,")
                    .append(stock.getWeekAnalysis() != null && stock.getWeekAnalysis().getOscillators() != null ? stock.getWeekAnalysis().getOscillators() + "," : "NULL,")
                    .append(stock.getWeekAnalysis() != null && stock.getWeekAnalysis().getMoving() != null ? stock.getWeekAnalysis().getMoving() + "," : "NULL,")
                    .append(stock.getMonthAnalysis() != null && stock.getMonthAnalysis().getSummary() != null ? stock.getMonthAnalysis().getSummary() + "," : "NULL,")
                    .append(stock.getMonthAnalysis() != null && stock.getMonthAnalysis().getOscillators() != null ? stock.getMonthAnalysis().getOscillators() + "," : "NULL,")
                    .append(stock.getMonthAnalysis() != null && stock.getMonthAnalysis().getMoving() != null ? stock.getMonthAnalysis().getMoving() : "NULL")
                    .append("),\n");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.deleteCharAt(builder.length() - 1);
        String query = String.format(readFileToString("insertToDaily.txt"), builder);
        Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            logger.error("failed to insert daily info for " + String.join("\",\"", list.stream().map(StockDailyInfo::getStock).toList()));
        }
    }

    public static List<StockInfo2022> getYearInfoFor(List<String> tickers) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery(String.format(readFileToString("selectFromInfo2022.txt"), String.join("\",\"", tickers)));
        List<StockInfo2022> result = new ArrayList<>();
        while(resultSet.next()){
            result.add(new StockInfo2022(
                    resultSet.getString("stock"),
                    resultSet.getString("sector"),
                    resultSet.getString("industry"),
                    resultSet.getLong("total_assets"),
                    resultSet.getLong("total_liabilities"),
                    resultSet.getLong("total_revenue"),
                    resultSet.getLong("shares_outstanding")
            ));
        }
        return result;
    }
}
