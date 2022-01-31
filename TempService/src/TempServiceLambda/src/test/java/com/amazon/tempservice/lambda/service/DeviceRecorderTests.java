package com.amazon.tempservice.lambda.service;

import com.amazon.tempservice.lambda.models.DeviceInfo;
import com.amazon.tempservice.lambda.models.DeviceReadingRequest;
import com.amazon.tempservice.lambda.utilities.StreamUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.Assert;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class DeviceRecorderTests {

    private DeviceRecorder deviceRecorder;
    private StreamUtils streamUtils;

    private String validReadingPayload, invalidReadingPayload, invalidDevideId;
    private final UUID DEVICE_ID = UUID.fromString("36d5658a-6908-479e-887e-a949ec199272");

    @BeforeEach
    public void setup() throws IOException {
        deviceRecorder = new DeviceRecorder();
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
    }

    @Test
    public void storeDeviceInfo_SavesReadingsInMap_ForGoodData() throws Exception {
        DeviceReadingRequest deviceReadingRequest = streamUtils.parseReadingRequest(validReadingPayload);
        deviceRecorder.storeDeviceInfo(deviceReadingRequest); // Method to test

        // Verification
        Map<UUID, DeviceInfo> idDeviceInfoMap = deviceRecorder.getIdDeviceInfoMap();
        Assert.assertTrue(idDeviceInfoMap.containsKey(DEVICE_ID));
    }
}
