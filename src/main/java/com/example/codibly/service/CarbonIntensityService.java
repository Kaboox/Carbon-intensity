package com.example.codibly.service;

import com.example.codibly.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");
        LocalDate today = LocalDate.now();

        String fromIso = today.atStartOfDay().format(formatter);
        String toIso = today.plusDays(3).atStartOfDay().format(formatter);

        String url = "/generation/" + fromIso + "/" + toIso;


        return restClient.get()
                .uri(url)
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

    public List<DailyCarbonProfile> getDailySummaries() {
        var rawResponse = fetchThreeDaysData();

        if (rawResponse.data() == null) return List.of();

        LocalDate today = LocalDate.now();
        List<LocalDate> expectedDays = List.of(
                today,
                today.plusDays(1),
                today.plusDays(2)
        );

        Map<LocalDate, List<GenerationItem>> groupedByDay = rawResponse.data().stream()
                .collect(Collectors.groupingBy(item ->
                        LocalDateTime.parse(item.from(), DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
                ));

        return groupedByDay.entrySet().stream()
                .filter(entry -> expectedDays.contains(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> calculateDayAverage(entry.getKey(), entry.getValue()))
                .toList();
    }

    private DailyCarbonProfile calculateDayAverage(LocalDate date, List<GenerationItem> generationItems) {
        double avgClean = generationItems.stream()
                .mapToDouble(item -> calculateCleanEnergyPercentage(item.generationMix()))
                .average()
                .orElse(0.0);

        Map<String, Double> avgFuelMix = generationItems.stream()
                .flatMap(item -> item.generationMix().stream())
                .collect(Collectors.groupingBy(
                        GenerationMix::fuel,
                        Collectors.averagingDouble(GenerationMix::perc)
                ));

        return new DailyCarbonProfile(date, avgClean, avgFuelMix);
    }

    public ChargingWindow findOptimalChargingWindow(int hours) {
        if (hours < 1 || hours > 6) {
            throw new IllegalArgumentException("Length of the charging window must be between 1 and 6");
        }

        int intervalsNeeded = hours * 2;

        var response = fetchThreeDaysData();
        if (response.data() == null || response.data().isEmpty()) {
            return null;
        }

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterTomorrow = LocalDate.now().plusDays(1);

        // Filtering data - only tomorrow and day after tomorrow is needed
        List<GenerationItem> forecastIntervals = response.data().stream()
                .filter(item -> {
                    // Parse string element to LocalDate
                    LocalDate itemDate = LocalDateTime.parse(item.from(), DateTimeFormatter.ISO_DATE_TIME).toLocalDate();
                    return itemDate.equals(tomorrow) || itemDate.equals(dayAfterTomorrow);
                })
                .sorted((a, b) -> a.from().compareTo(b.from())) // Chronological sort
                .toList();

        if (forecastIntervals.size() < intervalsNeeded) {
            return null;
        }

        // SLIDING WINDOW
        double maxCleanPercent = -1.0;
        GenerationItem bestStartInterval = null;

        for (int i = 0; i <= forecastIntervals.size() - intervalsNeeded; i++) {
            List<GenerationItem> window = forecastIntervals.subList(i, i + intervalsNeeded);

            double currentAvg = window.stream()
                    .mapToDouble(item -> calculateCleanEnergyPercentage(item.generationMix()))
                    .average()
                    .orElse(0.0);

            if (currentAvg > maxCleanPercent) {
                maxCleanPercent = currentAvg;
                bestStartInterval = window.getFirst();
            }
        }

        if (bestStartInterval != null) {
            LocalDateTime start = LocalDateTime.parse(bestStartInterval.from(), DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime end = start.plusHours(hours);

            return new ChargingWindow(start, end, maxCleanPercent);
        }

        return null;

    }


}
