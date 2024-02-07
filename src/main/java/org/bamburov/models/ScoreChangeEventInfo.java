package org.bamburov.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScoreChangeEventInfo {
    private LocalDate date;
    private String ticker;
    private int prevScore;
    private int currentScore;

    /**
     * Yes if score was not changed quickly and dramatically
     */
    private Consensus scoreChangeTrend = Consensus.LACK_OF_INFO;

    /**
     * history for last 7 days listed with '-'
     */
    private String scoreHistory = "";

    /**
     * Yes if price hasn't raised by 5+% last 5 working days, strong yes if it hasn't raised by 2%
     */
    private Consensus priceChangeTrend = Consensus.LACK_OF_INFO;

    /**
     * history for last 5 days listed with '-'
     */
    private String priceHistory = "";

    private Double currentPrice;

    private boolean isLiquid;

    private Double priceAfter1day;

    private Double priceAfter2day;

    private Double priceAfter3day;

    private Double priceAfter4day;

    private Double priceAfter5day;

    private Double priceAfter6day;

    private Double priceAfter7day;

    private Double priceAfter8day;

    private Double priceAfter9day;

    private Double priceAfter10day;

}
