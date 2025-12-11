package com.example.codibly.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenerationItem(String from, String to, @JsonProperty("generationmix")List<GenerationMix> generationMix) {
}
