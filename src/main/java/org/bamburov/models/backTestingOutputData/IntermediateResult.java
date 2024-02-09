package org.bamburov.models.backTestingOutputData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntermediateResult {
    private String ticker;
    private boolean isSignalFalse;
    private double relativeMaxProfit;
    private double relativeRequiredStoploss;

}
