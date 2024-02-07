package org.bamburov.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bamburov.Props;
import org.bamburov.models.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.bamburov.Main.getProps;
import static org.bamburov.utils.FileUtils.readFileFromResourcesToString;

public class MySqlUtils {
    private static BufferedWriter writerIncreasedScore;
    private static BufferedWriter writerDecreasedScore;

    static {
        try {
            writerIncreasedScore = new BufferedWriter(new FileWriter("increasedScore.txt"));
            writerDecreasedScore = new BufferedWriter(new FileWriter("decreasedScore.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void close() throws IOException {
        writerIncreasedScore.close();
        writerDecreasedScore.close();
    }

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
        for (StockInfo2022 stock : list) {
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
        String query = String.format(readFileFromResourcesToString("insertToInfo2022.txt"), builder);
        Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            logger.error("failed to insert year info for " + String.join("\",\"", list.stream().map(StockInfo2022::getStock).toList()));
        }
    }

    public static void insertToDaily(List<StockDailyInfo> list) throws SQLException {
        StringBuilder builder = new StringBuilder();
        for (StockDailyInfo stock : list) {
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
                    .append(stock.getMonthAnalysis() != null && stock.getMonthAnalysis().getMoving() != null ? stock.getMonthAnalysis().getMoving() + "," : "NULL,")
                    .append(stock.getVolume() != null ? stock.getVolume() : "NULL")
                    .append("),\n");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.deleteCharAt(builder.length() - 1);
        String query = String.format(readFileFromResourcesToString("insertToDaily.txt"), builder);
        Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            logger.error("failed to insert daily info for " + String.join("\",\"", list.stream().map(StockDailyInfo::getStock).toList()));
        }
    }

    public static List<String> getTickersFromNyseAndNasdaq() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("SELECT stock FROM INFO_2022 WHERE market='nyse' OR market='nasdaq';");
        List<String> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(resultSet.getString("stock"));
        }
        return result;
    }

    public static List<StockInfo2022> getYearInfoFor(List<String> tickers) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery(String.format(readFileFromResourcesToString("selectFromInfo2022.txt"), String.join("\",\"", tickers)));
        List<StockInfo2022> result = new ArrayList<>();
        while (resultSet.next()) {
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

    // write to file when score was increased or decreased by 3+ points on next day
    public static List<ScoreChangeEventInfo> catchScorePlus3Events(List<String> tickers, String startDate, String endDate) throws SQLException {
        List<ScoreChangeEventInfo> result = new ArrayList<>();
        for (String ticker : tickers) {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT score, date FROM DAILY WHERE stock='" + ticker + "' AND date >= '" + startDate + "' AND date <= '" + endDate + "';");
            int prevScore = 0, score = 0, index = 0;
            LocalDate prevDate = null, date = null;
            while (resultSet.next()) {
                if (index == 0) {
                    prevScore = resultSet.getInt("score");
                    prevDate = resultSet.getDate("date").toLocalDate();
                } else if (index == 1) {
                    score = resultSet.getInt("score");
                    date = resultSet.getDate("date") != null ? resultSet.getDate("date").toLocalDate() : null;
                } else {
                    prevScore = score;
                    score = resultSet.getInt("score");
                    prevDate = date;
                    date = resultSet.getDate("date") != null ? resultSet.getDate("date").toLocalDate() : null;
                }
                if (prevScore != 0 && score != 0 && date != null && prevDate != null) {
                    if ((prevScore - 3 >= score) || (prevScore + 3 <= score) && date.minusDays(1).isEqual(prevDate)) {
                        ScoreChangeEventInfo scoreChangeEventInfo = new ScoreChangeEventInfo();
                        scoreChangeEventInfo.setTicker(ticker);
                        scoreChangeEventInfo.setDate(date);
                        scoreChangeEventInfo.setPrevScore(prevScore);
                        scoreChangeEventInfo.setCurrentScore(score);
                        result.add(scoreChangeEventInfo);
                    }
                }
                index++;
            }
        }
        return result;
    }

    public static List<ScoreChangeEventInfo> setScoreChangesTrend(List<ScoreChangeEventInfo> scoreChangeEventInfoList) throws SQLException {
        for (ScoreChangeEventInfo scoreChangeEventInfo : scoreChangeEventInfoList) {
            Statement stmt = connection.createStatement();
            String startDate = scoreChangeEventInfo.getDate().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDate = scoreChangeEventInfo.getDate().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            ResultSet resultSet = stmt.executeQuery("SELECT score, date FROM DAILY WHERE stock='" + scoreChangeEventInfo.getTicker() + "' AND date >= '" + startDate + "' AND date <= '" + endDate + "';");
            StringBuilder scoreHistory = new StringBuilder();
            int maxScore = 0, countOfNoScore = 0;
            while (resultSet.next()) {
                int score = resultSet.getInt("score");
                if (score == 0) {
                    countOfNoScore++;
                }
                scoreHistory.append("-").append(score != 0 ? score : "null");
                if (maxScore < score) {
                    maxScore = score;
                }
            }
            scoreHistory.deleteCharAt(0);
            scoreChangeEventInfo.setScoreHistory(scoreHistory.toString());
            if (countOfNoScore >= 3) {
                scoreChangeEventInfo.setScoreChangeTrend(Consensus.LACK_OF_INFO);
            } else if (maxScore + 3 <= scoreChangeEventInfo.getCurrentScore()) {
                scoreChangeEventInfo.setScoreChangeTrend(Consensus.STRONG_YES);
            } else if (maxScore + 2 <= scoreChangeEventInfo.getCurrentScore()) {
                scoreChangeEventInfo.setScoreChangeTrend(Consensus.YES);
            } else if (maxScore + 1 <= scoreChangeEventInfo.getCurrentScore()) {
                scoreChangeEventInfo.setScoreChangeTrend(Consensus.NO);
            } else {
                scoreChangeEventInfo.setScoreChangeTrend(Consensus.STRONG_NO);
            }
        }
        return scoreChangeEventInfoList;
    }

    public static List<String> getSectors() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("SELECT sector, COUNT(*)\n" +
                "FROM INFO_2022 i join DAILY d on i.stock = d.stock\n" +
                "WHERE d.date = '2023-07-07'\n" +
                "AND d.current_price >= 3\n" +
                "AND i.sector is not null\n" +
                "-- AND i.industry is not null\n" +
                "GROUP BY sector -- , industry\n" +
                "HAVING COUNT(*) > 3;");
        List<String> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(resultSet.getString("sector"));
        }
        return result;
    }

    public static List<String> getIndustries() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("SELECT industry, COUNT(*)\n" +
                "FROM INFO_2022 i join DAILY d on i.stock = d.stock\n" +
                "WHERE d.date = '2023-07-07'\n" +
                "AND d.current_price >= 3\n" +
                "AND i.industry is not null\n" +
                "-- AND i.industry is not null\n" +
                "GROUP BY industry -- , industry\n" +
                "HAVING COUNT(*) > 3;");
        List<String> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(resultSet.getString("industry"));
        }
        return result;
    }

    public static List<TechnicalAnalysisChangeEventInfo> catchTechnicalDaySummaryBecameBuyEvents(String sector, String startDate, String endDate) throws Exception {
        List<String> tickers = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT d.stock\n" +
                "FROM INFO_2022 i join DAILY d on i.stock = d.stock\n" +
                "WHERE d.date = '2023-07-07'\n" +
                "AND d.current_price >= 3\n" +
                "AND i.sector = '" + sector + "';");
        while (resultSet.next()) {
            tickers.add(resultSet.getString("stock"));
        }
        List<TechnicalAnalysisChangeEventInfo> result = new ArrayList<>();
        for (String ticker : tickers) {
            Statement stmt = connection.createStatement();
            TechnicalAnalysisChangeEventInfo technicalAnalysisChangeEventInfo = null;
            ResultSet rs = stmt.executeQuery("SELECT technical_day_summary, date FROM DAILY WHERE stock='" + ticker + "' AND date >= '" + startDate + "' AND date <= '" + endDate + "';");
            int prevDayAnalysis = 0, dayAnalysis = 0, index = 0;
            LocalDate prevDate = null, date = null;
            while (rs.next()) {
                if (index == 0) {
                    prevDayAnalysis = rs.getInt("technical_day_summary");
                    prevDate = rs.getDate("date").toLocalDate();
                } else if (index == 1) {
                    dayAnalysis = rs.getInt("technical_day_summary");
                    date = rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null;
                } else {
                    prevDayAnalysis = dayAnalysis;
                    dayAnalysis = rs.getInt("technical_day_summary");
                    prevDate = date;
                    date = rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null;
                }
                if (dayAnalysis != 0 && date != null && prevDate != null && technicalAnalysisChangeEventInfo == null) {
                    if ((prevDayAnalysis > 2 || prevDayAnalysis == 0) && (dayAnalysis == 2 || dayAnalysis == 1) && date.minusDays(1).isEqual(prevDate)) {
                        technicalAnalysisChangeEventInfo = new TechnicalAnalysisChangeEventInfo();
                        technicalAnalysisChangeEventInfo.setTicker(ticker);
                        technicalAnalysisChangeEventInfo.setStartDate(date);
                    }
                }
                if (prevDayAnalysis != 0 && date != null && prevDate != null && technicalAnalysisChangeEventInfo != null) {
                    if ((dayAnalysis > 2 || dayAnalysis == 0) && (prevDayAnalysis == 2 || prevDayAnalysis == 1)) {
                        technicalAnalysisChangeEventInfo.setEndDate(date);
                        result.add(technicalAnalysisChangeEventInfo);
                        technicalAnalysisChangeEventInfo = null;
                    }
                }
                if (technicalAnalysisChangeEventInfo != null) {
                    technicalAnalysisChangeEventInfo.getAnalysisValues().add(dayAnalysis);
                }
                index++;
            }
        }
        return result;
    }
}
