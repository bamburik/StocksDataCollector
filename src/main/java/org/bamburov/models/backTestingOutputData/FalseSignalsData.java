package org.bamburov.models.backTestingOutputData;

import lombok.Data;

@Data
public class FalseSignalsData {
    private double tenPercentileRelativeMaxLoss;
    private double fiftyPercentileRelativeMaxLoss;
    private double ninetyPercentileRelativeMaxLoss;
    private double averageRelativeMaxLoss;
}
