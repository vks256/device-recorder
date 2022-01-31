package com.amazon.tempservice.lambda.utilities;

import com.amazon.tempservice.lambda.models.DeviceReadingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.Assert;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class StreamUtilsTests {

    private StreamUtils streamUtils;

    private String validReadingPayload, invalidReadingPayload, invalidDevideId;
    private final UUID DEVICE_ID = UUID.fromString("36d5658a-6908-479e-887e-a949ec199272");
    private final String TIMESTAMP1 = "2021-09-29T16:08:15+01:00";
    private final int COUNT1 = 2;
    private final String TIMESTAMP2 = "2021-09-29T16:09:15+01:00";
    private final int COUNT2 = 15;

    @BeforeEach
    public void setup() throws IOException {
        streamUtils = new StreamUtils();

        validReadingPayload =
            "{" +
                "    \"id\": \"36d5658a-6908-479e-887e-a949ec199272\"," +
                "    \"readings\": [" +
                "        {" +
                "            \"timestamp\": \"2021-09-29T16:08:15+01:00\"," +
                "            \"count\": \"2\"" +
                "     }, {" +
                "            \"timestamp\": \"2021-09-29T16:09:15+01:00\"," +
                "            \"count\": \"15\" }" +
                "     ]" +
                "}";

        invalidReadingPayload =
            "{" +
                "    \"id\": \"36d5658a-6908-479e-887e-a949ec199272\"," +
                "    \"readings\":{" +
                "            \"timestamp\": \"2021-09-29T16:08:15+01:00\"," +
                "            \"count\": \"2\"" +
                "    }" +
                "}";

        invalidDevideId = "{\"id\": \"123\"}"; // Not a UUID
    }

    @Test
    public void parseInputPayload_ReturnValidPayload_ForGoodData() {
        DeviceReadingRequest deviceReadingRequest = streamUtils.parseReadingRequest(validReadingPayload); // Method to test
        System.out.println("deviceReadingRequest: " + deviceReadingRequest);

        // Verification
        Assert.assertEquals(DEVICE_ID, deviceReadingRequest.getId());
        Assert.assertEquals(TIMESTAMP1, deviceReadingRequest.getReadings().get(0).getTimestamp());
        Assert.assertEquals(TIMESTAMP2, deviceReadingRequest.getReadings().get(1).getTimestamp());
    }

    @Test
    public void parseInputPayload_ThrowsException_ForMalformedUUID() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            streamUtils.parseDeviceId(invalidDevideId);
        });

        String expectedMessage = "Invalid UUID string";
        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }
}
