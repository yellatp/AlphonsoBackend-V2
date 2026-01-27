package com.alphonso.Interviewer_Service.RequestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RadarChartDto {
    private String[] labels;
    private Double[] values;
}