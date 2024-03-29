package org.bamburov.models.backTestingOutputData;

import lombok.Data;

import java.util.List;

@Data
public class Result {
    private long amountOfTrueSignals;
    private long amountOfFalseSignals;
    private TrueSignalsData trueSignalsData;
    private FalseSignalsData falseSignalsData;
    List<OneStockResult> stocks;
}
