package com.example.smartbulbs.service;

import com.example.smartbulbs.dto.LampAnalytics;
import com.example.smartbulbs.dto.LampStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsAggregator {

    private final LampStateService lampStateService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedRate = 60000) // Каждую минуту
    public void aggregateAndReport() {
        Instant now = Instant.now();
        var statuses = lampStateService.getAllLampStatuses();
        long totalLamps = statuses.size();
        long onlineLamps = statuses.stream().filter(LampStatus::isOnline).count();
        long offlineLamps = totalLamps - onlineLamps;
        double totalPower = statuses.stream().mapToDouble(s -> s.getPowerConsumption() > 0 ? s.getPowerConsumption() : 0.0).sum();
        double avgBrightness = statuses.stream().mapToInt(LampStatus::getBrightness).average().orElse(0.0);

        log.info("--- Aggregated Analytics (Last Minute) ---");
        log.info("Total Lamps: {}", totalLamps);
        log.info("Online Lamps: {}", onlineLamps);
        log.info("Offline Lamps: {}", offlineLamps);
        log.info("Total Power Consumption: {} W", totalPower);
        log.info("Average Brightness: {}", avgBrightness);
        log.info("------------------------------------------");
        lampStateService.checkForAnomalies();

        kafkaTemplate.send("lamp-analytics", new LampAnalytics(
                now.minusSeconds(60), 
                now, 
                totalLamps, 
                onlineLamps, 
                offlineLamps, 
                totalPower,
                avgBrightness
        ));
        log.info("Analytics sent to Kafka");
    }
}