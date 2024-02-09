package org.bamburov.models.backTestingOutputData;

import lombok.Data;

@Data
public class FalseSignalsData {
    private double tenPercentileRelativeRequiredStopLoss;
    private double fiftyPercentileRelativeRequiredStopLoss;
    private double ninetyPercentileRelativeRequiredStopLoss;
    private double averageRelativeRequiredStopLoss;
}
