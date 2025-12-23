package com.example.smartbulbs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LampTelemetry {
    private String lampId;
    private String type;
    private String status;
    private int brightness;
    private int colorTemp;
    private double powerConsumption;
    private double voltage;
    private Instant timestamp;
}