package com.amazon.tempservice.lambda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceReadingResponse {
    @JsonProperty
    int statusCode;

    @JsonProperty
    UUID id;

    @JsonProperty
    Integer latestCount;

    @JsonProperty
    Integer cumulativeCount;

    @JsonProperty
    String message;
}
