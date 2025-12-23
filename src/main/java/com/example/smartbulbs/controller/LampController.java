package com.example.smartbulbs.controller;

import com.example.smartbulbs.dto.CommandRequest;
import com.example.smartbulbs.dto.LampCommand;
import com.example.smartbulbs.dto.LampStatus;
import com.example.smartbulbs.service.LampStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LampController {

    private final LampStateService lampStateService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/lamps")
    public ResponseEntity<List<LampStatus>> getAllLamps() {
        List<LampStatus> statuses = lampStateService.getAllLampStatuses().stream().toList(); // Convert to List explicitly
        return ResponseEntity.ok(statuses);
    }

    @PostMapping("/lamps/{id}/command")
    public ResponseEntity<String> sendCommand(@PathVariable("id") String lampId, @RequestBody CommandRequest request) {
        String commandId = "cmd_" + UUID.randomUUID();
        String action = request.getAction();
        int value = request.getValue();
        String priority = request.getPriority() != null ? request.getPriority() : "normal";
        Instant timestamp = Instant.now();

        if (lampStateService.getLampStatus(lampId) == null) {
            return ResponseEntity.badRequest().body("Lamp with ID " + lampId + " not found.");
        }

        kafkaTemplate.send("lamp-commands", lampId, new LampCommand(commandId, lampId, action, value, priority, timestamp));

        System.out.println("Sent command via REST: " + commandId + " to lamp: " + lampId + " action: " + action);
        return ResponseEntity.ok("Command sent successfully. Command ID: " + commandId);
    }
}