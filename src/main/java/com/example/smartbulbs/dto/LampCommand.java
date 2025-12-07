package com.example.smartbulbs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LampCommand {
    private String commandId;
    private String lampId;
    private String action;
    private int value;
    private String priority;
    private Instant timestamp;
}