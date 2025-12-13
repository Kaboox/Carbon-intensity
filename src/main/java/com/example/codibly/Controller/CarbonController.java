package com.example.codibly.Controller;

import com.example.codibly.dto.ChargingWindow;
import com.example.codibly.dto.DailyCarbonProfile;
import com.example.codibly.service.CarbonIntensityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/carbon")
public class CarbonController {

    private final CarbonIntensityService carbonIntensityService;

    public CarbonController(CarbonIntensityService carbonIntensityService) {
        this.carbonIntensityService = carbonIntensityService;
    }

    @GetMapping("/mix")
    public List<DailyCarbonProfile> getEnergyMix() {
        return carbonIntensityService.getDailySummaries();
    }

    @GetMapping("optimal-charging")
    public ChargingWindow getOptimalCharging(@RequestParam int hours) {
        return carbonIntensityService.findOptimalChargingWindow(hours);
    }
}
