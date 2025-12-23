package com.example.smartbulbs.service;

import com.example.smartbulbs.dto.LampTelemetry;
import com.example.smartbulbs.model.Lamp;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelemetryConsumerService {

    private final LampStateService lampStateService;

    @KafkaListener(topics = "lamp-telemetry", groupId = "analytics-processor", containerFactory = "telemetryConsumerFactory")
    public void processTelemetry(LampTelemetry telemetry, Acknowledgment acknowledgment) {
        System.out.println("Received telemetry: " + telemetry);

        Lamp lamp = new Lamp(telemetry.getLampId(), telemetry.getType()); // В реальности, объект может быть найден в LampStateService
        lamp.updateFromTelemetry(telemetry);
        lampStateService.updateLampState(telemetry.getLampId(), lamp);

        acknowledgment.acknowledge();
    }
}