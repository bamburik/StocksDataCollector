package org.bamburov.models.backTestingOutputData;

import lombok.Data;

@Data
public class OneStockResult {
    private String ticker;
    private long amountOfTrueSignals;
    private long amountOfFalseSignals;
    private TrueSignalsData trueSignalsData;
    private FalseSignalsData falseSignalsData;
}
