package org.bamburov.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TechnicalAnalysis {
    private Integer summary;
    private Integer oscillators;
    private Integer moving;
}