package com.example.smartbulbs.service;

import com.example.smartbulbs.dto.LampStatus;
import com.example.smartbulbs.model.Lamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class LampStateService {

    private final Map<String, Lamp> lampStates = new ConcurrentHashMap<>();

    private static final Duration OFFLINE_THRESHOLD = Duration.ofMinutes(2);

    @PostConstruct
    public void initializeLamps() {
        lampStates.put("lamp_1", new Lamp("lamp_1", "color_bulb"));
        lampStates.put("lamp_2", new Lamp("lamp_2", "white_bulb"));
        lampStates.put("lamp_3", new Lamp("lamp_3", "rgb_strip"));
        lampStates.put("lamp_4", new Lamp("lamp_4", "filament_bulb"));
        lampStates.put("lamp_5", new Lamp("lamp_5", "smart_candle"));
    }

    public void updateLampState(String lampId, Lamp updatedLamp) {
        lampStates.put(lampId, updatedLamp);
    }

    public Collection<LampStatus> getAllLampStatuses() {
        return lampStates.values().stream()
                .map(this::convertToLampStatus)
                .toList();
    }

    public LampStatus getLampStatus(String lampId) {
        Lamp lamp = lampStates.get(lampId);
        if (lamp != null) {
            return convertToLampStatus(lamp);
        }
        return null;
    }

    private LampStatus convertToLampStatus(Lamp lamp) {
        if (lamp.getLastSeen() == null) {
            return new LampStatus(lamp.getLampId(), lamp.getType(), lamp.getStatus(), lamp.getBrightness(), lamp.getColorTemp(), lamp.getPowerConsumption(), lamp.getVoltage(), null, false);
        }
        boolean isOnline = Instant.now().isBefore(lamp.getLastSeen().plus(OFFLINE_THRESHOLD));
        return new LampStatus(lamp.getLampId(), lamp.getType(), lamp.getStatus(), lamp.getBrightness(), lamp.getColorTemp(), lamp.getPowerConsumption(), lamp.getVoltage(), lamp.getLastSeen(), isOnline);
    }

    public void checkForAnomalies() {
        Instant now = Instant.now();
        Instant threshold = now.minus(OFFLINE_THRESHOLD);
        lampStates.forEach((id, lamp) -> {
            if (lamp.getLastSeen() != null && lamp.getLastSeen().isBefore(threshold)) {
                log.warn("ALERT: Lamp {} is potentially offline (last seen: {})", id, lamp.getLastSeen());
            }
        });
    }
}