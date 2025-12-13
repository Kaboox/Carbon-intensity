package com.example.codibly.service;

import com.example.codibly.dto.CarbonIntensityResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CarbonIntensityServiceTest {

    @Autowired
    private CarbonIntensityService service;

    @Test
    void shouldFetchDataFromExternalApi() {
        // GIVEN
        // ----------

        // WHEN
        System.out.println("Getting data...");
        CarbonIntensityResponse response = service.fetchThreeDaysData();

        // THEN
        // Check for object
        assertNotNull(response, "Response data is null");

        // Check for list
        assertNotNull(response.data(), "Response data list is null");

        // Check for list emptiness
        assertFalse(response.data().isEmpty(), "Response data list is empty");

        System.out.println(response.data());

    }
}
