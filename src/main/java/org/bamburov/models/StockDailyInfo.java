package org.bamburov.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockDailyInfo {
    private String stock;
    private Integer score;
    private String date;
    private Double currentPrice;
    private Double priceTarget;
    private Double bestPriceTarget;
    private String analystConsensus;
    private String bestAnalystConsensus;
    private Double p2e;
    private Double p2eCalc;
    private Double p2b;
    private Double p2bCalc;
    private Double p2s;
    private Double p2sCalc;
    private Double p2fcf;
    private Double p2cf;
    private Double eps;
    private String bloggerSentiment;
    private String hedgeFundTrend;
    private String bestCrowdWisdom;
    private String crowdWisdom;
    private String newsSentiment;
    private String technicalSma;
    private Boolean doesHaveTechnicalData;
    private TechnicalAnalysis dayAnalysis;
    private TechnicalAnalysis weekAnalysis;
    private TechnicalAnalysis monthAnalysis;
    private Integer volume;
}