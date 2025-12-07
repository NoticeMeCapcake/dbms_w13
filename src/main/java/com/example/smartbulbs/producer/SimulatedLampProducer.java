package com.example.smartbulbs.producer;

import com.example.smartbulbs.dto.LampTelemetry;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class SimulatedLampProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    private final Random random = new Random();

    private final String[] lampIds = {"lamp_1", "lamp_2", "lamp_3", "lamp_4", "lamp_5"};
    private final String[] types = {"color_bulb", "white_bulb", "rgb_strip", "filament_bulb", "smart_candle"};

    @Scheduled(fixedRate = 15000) // Каждые 15 секунд
    public void sendTelemetry() {
        for (String id : lampIds) {
            String type = types[random.nextInt(types.length)];
            String status = random.nextBoolean() ? "on" : "off";
            int brightness = random.nextInt(101); // 0-100
            int colorTemp = status.equals("on") ? 2700 + random.nextInt(6301) : 0; // 2700K - 9000K если вкл, иначе 0
            double powerConsumption = status.equals("on") ? 5.0 + (random.nextDouble() * 50.0) : 0.1; // Вт
            double voltage = 210.0 + (random.nextDouble() * 20.0); // Вольт
            Instant timestamp = Instant.now();

            LampTelemetry telemetry = new LampTelemetry(id, type, status, brightness, colorTemp, powerConsumption, voltage, timestamp);

            kafkaTemplate.send("lamp-telemetry", id, telemetry);
            System.out.println("Sent telemetry: " + telemetry);
        }
    }
}