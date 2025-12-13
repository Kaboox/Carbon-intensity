package com.example.codibly.dto;

import java.time.LocalDateTime;

public record ChargingWindow(
        LocalDateTime startTime,
        LocalDateTime endTime,
        double cleanEnergyPercent
) {
}
