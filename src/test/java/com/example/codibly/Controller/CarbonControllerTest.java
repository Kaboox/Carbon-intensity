package com.example.codibly.Controller;

import com.example.codibly.dto.ChargingWindow;
import com.example.codibly.dto.DailyCarbonProfile;
import com.example.codibly.service.CarbonIntensityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@WebMvcTest(CarbonController.class)
class CarbonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarbonIntensityService service;

    @Test
    void shouldReturnDailyMix() throws Exception {
        // GIVEN
        DailyCarbonProfile mockProfile = new DailyCarbonProfile(
                LocalDate.now(),
                50.5,
                Map.of("wind", 50.5)
        );

        // Return this list if someone asks service for getDailySummaries
        when(service.getDailySummaries()).thenReturn(List.of(mockProfile));

        // WHEN & THEN - call the endpoint and check results
        mockMvc.perform(get("/api/carbon/mix"))
                .andExpect(status().isOk()) // Is it code 200? (OK)
                .andExpect(jsonPath("$", hasSize(1))) // Does the list contain 1 element
                .andExpect(jsonPath("$[0].cleanEnergyPercent", is(50.5))); // Do we get matching values
    }

    @Test
    void shouldReturnOptimalChargingWindow() throws Exception {
        // GIVEN
        ChargingWindow mockWindow = new ChargingWindow(
                LocalDateTime.of(2025, 12, 12, 10, 0),
                LocalDateTime.of(2025, 12, 12, 12, 0),
                80.0
        );


        when(service.findOptimalChargingWindow(2)).thenReturn(mockWindow);

        // WHEN & THEN
        mockMvc.perform(get("/api/carbon/optimal-charging").param("hours", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cleanEnergyPercent", is(80.0)));
    }
}