package org.bamburov.models.backTestingOutputData;

import lombok.Data;

@Data
public class OneStockResult {
    private String ticker;
    private int amountOfTrueSignals;
    private int amountOfFalseSignals;
    private TrueSignalsData trueSignalsData;
    private FalseSignalsData falseSignalsData;
}
