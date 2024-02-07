package org.bamburov.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bamburov.models.ScoreChangeEventInfo;
import org.bamburov.models.TechnicalAnalysisChangeEventInfo;
import org.bamburov.ta.core.MyTradingRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelUtils {
    public static void write(List<ScoreChangeEventInfo> scoreChangeEventInfoList) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Data");

        Row header = sheet.createRow(0);
        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Date");
        headerCell = header.createCell(1);
        headerCell.setCellValue("Ticker");
        headerCell = header.createCell(2);
        headerCell.setCellValue("Prev score");
        headerCell = header.createCell(3);
        headerCell.setCellValue("Current score");
        headerCell = header.createCell(4);
        headerCell.setCellValue("Score Change Trend");
        headerCell = header.createCell(5);
        headerCell.setCellValue("Score History");
        headerCell = header.createCell(6);
        headerCell.setCellValue("Current Price");
        headerCell = header.createCell(7);
        headerCell.setCellValue("Price Change Trend");
        headerCell = header.createCell(8);
        headerCell.setCellValue("Price History");
        headerCell = header.createCell(9);
        headerCell.setCellValue("Is Liquid");
        headerCell = header.createCell(10);
        headerCell.setCellValue("Price After 1st day");
        headerCell = header.createCell(11);
        headerCell.setCellValue("Price After 2nd day");
        headerCell = header.createCell(12);
        headerCell.setCellValue("Price After 3rd day");
        headerCell = header.createCell(13);
        headerCell.setCellValue("Price After 4th day");
        headerCell = header.createCell(14);
        headerCell.setCellValue("Price After 5th day");
        headerCell = header.createCell(15);
        headerCell.setCellValue("Price After 6th day");
        headerCell = header.createCell(16);
        headerCell.setCellValue("Price After 7th day");
        headerCell = header.createCell(17);
        headerCell.setCellValue("Price After 8th day");
        headerCell = header.createCell(18);
        headerCell.setCellValue("Price After 9th day");
        headerCell = header.createCell(19);
        headerCell.setCellValue("Price After 10th day");

        Row emptyRow = sheet.createRow(1);
        int rowIndex = 2;

        for(ScoreChangeEventInfo scoreChangeEventInfo : scoreChangeEventInfoList) {
            Row row = sheet.createRow(rowIndex);
            Cell cell = row.createCell(0);
            cell.setCellValue(scoreChangeEventInfo.getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            cell = row.createCell(1);
            cell.setCellValue(scoreChangeEventInfo.getTicker());
            cell = row.createCell(2);
            cell.setCellValue(scoreChangeEventInfo.getPrevScore());
            cell = row.createCell(3);
            cell.setCellValue(scoreChangeEventInfo.getCurrentScore());
            cell = row.createCell(4);
            cell.setCellValue(scoreChangeEventInfo.getScoreChangeTrend().value);
            cell = row.createCell(5);
            cell.setCellValue(scoreChangeEventInfo.getScoreHistory());
            cell = row.createCell(6);
            if (scoreChangeEventInfo.getCurrentPrice() != null) {
                cell.setCellValue(scoreChangeEventInfo.getCurrentPrice());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(7);
            cell.setCellValue(scoreChangeEventInfo.getPriceChangeTrend().value);
            cell = row.createCell(8);
            cell.setCellValue(scoreChangeEventInfo.getPriceHistory());
            cell = row.createCell(9);
            cell.setCellValue(scoreChangeEventInfo.isLiquid());
            cell = row.createCell(10);
            if (scoreChangeEventInfo.getPriceAfter1day() != null) {
                cell.setCellValue(scoreChangeEventInfo.getPriceAfter1day());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(11);
            if (scoreChangeEventInfo.getPriceAfter2day() != null) {
                cell.setCellValue(scoreChangeEventInfo.getPriceAfter2day());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(12);
            if (scoreChangeEventInfo.getPriceAfter3day() != null) {
                cell.setCellValue(scoreChangeEventInfo.getPriceAfter3day());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(13);
            if (scoreChangeEventInfo.getPriceAfter4day() != null) {
                cell.setCellValue(scoreChangeEventInfo.getPriceAfter4day());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(14);
            if (scoreChangeEventInfo.getPriceAfter5day() != null) {
                cell.setCellValue(scoreChangeEventInfo.getPriceAfter5day());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(15);
            if (scoreChangeEventInfo.getPriceAfter6day() != null) {
                cell.setCellValue(scoreChangeEventInfo.getPriceAfter6day());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(16);
            if (scoreChangeEventInfo.getPriceAfter7day() != null) {
                cell.setCellValue(scoreChangeEventInfo.getPriceAfter7day());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(17);
            if (scoreChangeEventInfo.getPriceAfter8day() != null) {
                cell.setCellValue(scoreChangeEventInfo.getPriceAfter8day());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(18);
            if (scoreChangeEventInfo.getPriceAfter9day() != null) {
                cell.setCellValue(scoreChangeEventInfo.getPriceAfter9day());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(19);
            if (scoreChangeEventInfo.getPriceAfter10day() != null) {
                cell.setCellValue(scoreChangeEventInfo.getPriceAfter10day());
            } else {
                cell.setCellValue("null");
            }

            rowIndex++;
        }

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "temp2.xlsx";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();

    }

    public static void write2(List<TechnicalAnalysisChangeEventInfo> TechnicalAnalysisChangeEventInfoList, String filename) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Data");

        Row header = sheet.createRow(0);
        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Start Date");
        headerCell = header.createCell(1);
        headerCell.setCellValue("End Date");
        headerCell = header.createCell(2);
        headerCell.setCellValue("Ticker");
        headerCell = header.createCell(3);
        headerCell.setCellValue("Analysis Values");
        headerCell = header.createCell(4);
        headerCell.setCellValue("Price Values");
        headerCell = header.createCell(5);
        headerCell.setCellValue("Buy Price");
        headerCell = header.createCell(6);
        headerCell.setCellValue("Sell Price");
        headerCell = header.createCell(7);
        headerCell.setCellValue("By Stop Loss");

        Row emptyRow = sheet.createRow(1);
        int rowIndex = 2;

        for(TechnicalAnalysisChangeEventInfo technicalAnalysisChangeEventInfo : TechnicalAnalysisChangeEventInfoList) {
            Row row = sheet.createRow(rowIndex);
            Cell cell = row.createCell(0);
            cell.setCellValue(technicalAnalysisChangeEventInfo.getStartDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            cell = row.createCell(1);
            cell.setCellValue(technicalAnalysisChangeEventInfo.getEndDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            cell = row.createCell(2);
            cell.setCellValue(technicalAnalysisChangeEventInfo.getTicker());
            cell = row.createCell(3);
            cell.setCellValue(String.join(", ", technicalAnalysisChangeEventInfo.getAnalysisValues().stream().map(x -> x.toString()).collect(Collectors.toList())));
            cell = row.createCell(4);
            cell.setCellValue(String.join(", ", technicalAnalysisChangeEventInfo.getPriceValues().stream().map(x -> x.toString()).collect(Collectors.toList())));
            cell = row.createCell(5);
            if (technicalAnalysisChangeEventInfo.getBuyPrice() != null) {
                cell.setCellValue(technicalAnalysisChangeEventInfo.getBuyPrice());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(6);
            if (technicalAnalysisChangeEventInfo.getBuyPrice() != null) {
                cell.setCellValue(technicalAnalysisChangeEventInfo.getSellPrice());
            } else {
                cell.setCellValue("null");
            }
            cell = row.createCell(7);
            cell.setCellValue(technicalAnalysisChangeEventInfo.isByStopLoss());
            rowIndex++;
        }

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + filename;

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();

    }

    public static void writeResultsOfBuyByBollingerAndStochastic(List<MyTradingRecord> myTradingRecords) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Data");

        Row header = sheet.createRow(0);
        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Ticker");
        headerCell = header.createCell(1);
        headerCell.setCellValue("Stop loss");
        headerCell = header.createCell(2);
        headerCell.setCellValue("Average profit");
        headerCell = header.createCell(3);
        headerCell.setCellValue("Average position length");
        int rowIndex = 1;

        for (MyTradingRecord myTradingRecord : myTradingRecords) {
            Row row = sheet.createRow(rowIndex);
            Cell cell = row.createCell(0);
            cell.setCellValue(myTradingRecord.getTicker());
            cell = row.createCell(1);
            cell.setCellValue(myTradingRecord.getStopLossPercent());
            cell = row.createCell(2);
            cell.setCellValue(myTradingRecord.getAverageProfitPercentage());
            cell = row.createCell(3);
            cell.setCellValue(myTradingRecord.getAveragePositionLength());
            rowIndex++;
        }

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "ResultsOfBuyByBollingerAndStochastic.xlsx";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
    }
}
