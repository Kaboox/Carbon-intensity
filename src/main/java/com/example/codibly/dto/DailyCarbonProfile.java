package com.example.codibly.dto;

import java.time.LocalDate;
import java.util.Map;

public record DailyCarbonProfile(
        LocalDate date,
        double cleanEnergyPercent,
        Map<String, Double> fuelMix
) {
}
