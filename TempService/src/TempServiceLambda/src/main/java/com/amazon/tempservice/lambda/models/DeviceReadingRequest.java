package com.amazon.tempservice.lambda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceReadingRequest {
    @JsonProperty("id")
    UUID id;

    @JsonProperty("readings")
    ArrayList<Reading> readings;
}
