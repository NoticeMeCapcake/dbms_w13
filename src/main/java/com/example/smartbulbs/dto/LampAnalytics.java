package com.example.smartbulbs.dto;

import java.time.Instant;

public record LampAnalytics (
        Instant periodStart,
        Instant periodEnd,
        long totalLamps,
        long lampsOnline,
        long lampsOffline,
        double totalPowerConsumption,
        double avgBrightness
        ) {
}
