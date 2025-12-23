package com.example.smartbulbs.model;

import com.example.smartbulbs.dto.LampTelemetry;
import lombok.Data;

import java.time.Instant;

@Data
public class Lamp {
    private String lampId;
    private String type;
    private String status;
    private int brightness;
    private int colorTemp;
    private double powerConsumption;
    private double voltage;
    private Instant lastSeen;

    public Lamp(String lampId, String type) {
        this.lampId = lampId;
        this.type = type;
    }

    public void updateFromTelemetry(LampTelemetry telemetry) {
        if (!this.lampId.equals(telemetry.getLampId())) {
            throw new IllegalArgumentException("Lamp ID mismatch");
        }
        this.status = telemetry.getStatus();
        this.brightness = telemetry.getBrightness();
        this.colorTemp = telemetry.getColorTemp();
        this.powerConsumption = telemetry.getPowerConsumption();
        this.voltage = telemetry.getVoltage();
        this.lastSeen = telemetry.getTimestamp();
    }
}