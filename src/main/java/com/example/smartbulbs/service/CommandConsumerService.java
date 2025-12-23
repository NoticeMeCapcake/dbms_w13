package com.example.smartbulbs.service;

import com.example.smartbulbs.dto.LampCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommandConsumerService {

    @KafkaListener(topics = "lamp-commands", groupId = "command-processor", containerFactory = "commandConsumerFactory")
    public void processCommand(LampCommand command, Acknowledgment acknowledgment) {
        log.info("Received command: {}", command);

        if (Math.random() < 0.2) {
            log.error("Simulating error processing command: {}", command.getCommandId());
            throw new RuntimeException("Simulated processing error for command " + command.getCommandId());
        }

        log.info("Successfully processed command: {} for lamp: {}", command.getCommandId(), command.getLampId());

        acknowledgment.acknowledge();
    }
}