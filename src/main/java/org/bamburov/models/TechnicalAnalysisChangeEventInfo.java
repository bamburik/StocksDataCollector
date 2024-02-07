package org.bamburov.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TechnicalAnalysisChangeEventInfo {
    private LocalDate startDate;
    private LocalDate endDate;
    private String ticker;
    private List<Integer> analysisValues = new ArrayList<>();
    private List<Double> priceValues = new ArrayList<>();
    private Double buyPrice;
    private Double sellPrice;
    private boolean byStopLoss = false;
}
