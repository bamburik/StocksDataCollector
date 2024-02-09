package org.bamburov.models.backTestingOutputData;

import lombok.Data;

@Data
public class TrueSignalsData {
    private double tenPercentileRelativeProfit;
    private double fiftyPercentileRelativeProfit;
    private double ninetyPercentileRelativeProfit;
    private double averageRelativeProfit;
    private double tenPercentileRelativeRequiredStopLoss;
    private double fiftyPercentileRelativeRequiredStopLoss;
    private double ninetyPercentileRelativeRequiredStopLoss;
    private double averageRelativeRequiredStopLoss;
}
