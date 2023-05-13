package org.bamburov.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockInfo2022 {
    private String stock;
    private String sector;
    private String industry;
    private Long totalAssets;
    private Long totalLiabilities;
    private Long totalRevenue;
    private Long sharesOutstanding;
}
