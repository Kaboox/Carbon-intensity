package com.example.codibly.service;

import com.example.codibly.dto.CarbonIntensityResponse;
import com.example.codibly.dto.GenerationMix;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
public class CarbonIntensityService {

    private final RestClient restClient;

    private static final Set<String> CLEAN_ENERGY_SOURCES = Set.of(
            "biomass",
            "nuclear",
            "hydro",
            "wind",
            "solar"
    );

    public CarbonIntensityService(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://api.carbonintensity.org.uk")
                .build();
    }

    public CarbonIntensityResponse fetchThreeDaysData() {
        LocalDate today = LocalDate.now();

        LocalDateTime startDateTime = today.atStartOfDay();
        LocalDateTime endDateTime = today.plusDays(3).atStartOfDay();

        String fromIso = startDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
        String toIso = endDateTime.format(DateTimeFormatter.ISO_DATE_TIME);

        return restClient.get()
                .uri("/generation/{from}/{to}", fromIso, toIso)
                .retrieve()
                .body(CarbonIntensityResponse.class);
    }

    public double calculateCleanEnergyPercentage(List<GenerationMix> mix) {
        if (mix == null || mix.isEmpty()) {
            return 0.0;
        }

        return mix.stream()
                .filter(source -> CLEAN_ENERGY_SOURCES.contains(source.fuel()))
                .mapToDouble(GenerationMix::perc)
                .sum();
    }
}
