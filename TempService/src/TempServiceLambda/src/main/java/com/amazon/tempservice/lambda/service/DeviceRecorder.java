package com.amazon.tempservice.lambda.service;

import com.amazon.tempservice.lambda.models.Constants;
import com.amazon.tempservice.lambda.models.DeviceInfo;
import com.amazon.tempservice.lambda.models.DeviceReadingRequest;
import com.amazon.tempservice.lambda.models.DeviceReadingResponse;
import com.amazon.tempservice.lambda.models.Reading;
import com.amazon.tempservice.lambda.utilities.StreamUtils;
import com.amazonaws.services.lambda.runtime.Context;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Log4j2
@Data
public class DeviceRecorder {
    private Map<UUID, DeviceInfo> idDeviceInfoMap;
    private StreamUtils streamUtils;

    public DeviceRecorder(){
        idDeviceInfoMap = new HashMap<>(); // This is the in-memory storage. Ideally, this data should have been stored in a database
        streamUtils = new StreamUtils();
    }

    // Common Lambda handler for both POST and GET APIs
    // Ideally there should be two different lambdas (one for each APIs), which can interact with each other through
    // a database. Due to the constraint of in-memory solution, I have created one lambda for both APIs, so that they
    // can share the common resource "idDeviceInfoMap"
    public void lambdaHandler(InputStream inputStream, OutputStream outputStream, Context context) throws Exception {
        String inputString = streamUtils.convertStreamToString(inputStream);
        if (streamUtils.isCallForGETAPI(inputString)) {
            getDeviceReading(inputString, outputStream, context);
        }
        else {
            serveRequest(inputString, outputStream, context);
        }
    }

    // Lambda handler for POST API
    public void serveRequest(String inputString, OutputStream outputStream, Context context) throws IOException {
        DeviceReadingRequest deviceReadingRequest = null;
        try {
            deviceReadingRequest = streamUtils.parseReadingRequest(inputString);
            storeDeviceInfo(deviceReadingRequest);
            streamUtils.writeOutput(outputStream, Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CODE);
        }
        // Can do better error handling with different types of exceptions
        catch (Exception e) {
            log.error(Constants.MALFORMED_DATA_ERROR_MESSAGE, e);
            streamUtils.writeOutput(outputStream, Constants.MALFORMED_DATA_ERROR_MESSAGE, Constants.BAD_DATA_CODE);
        }
    }

    // Saves the new reading request into idDeviceInfoMap
    protected void storeDeviceInfo(@NonNull DeviceReadingRequest deviceReadingRequest) {
        UUID id = deviceReadingRequest.getId();
        ArrayList<Reading> readings = deviceReadingRequest.getReadings();

        DeviceInfo deviceInfo = idDeviceInfoMap.getOrDefault(id, new DeviceInfo());
        for (Reading reading: readings) {
            deviceInfo.updateDeviceInfo(reading.getTimestamp(), reading.getCount());
        }

        log.info("Saving the id: {} with deviceInfo: {}", id, deviceInfo);
        idDeviceInfoMap.put(id, deviceInfo);
    }


    // Lambda handler for GET API
    public void getDeviceReading(String inputString, OutputStream outputStream, Context context) throws IOException {
        try {
            UUID device_id = streamUtils.parseDeviceId(inputString);
            DeviceReadingResponse deviceReadingResponse = readDeviceInfo(device_id);
            streamUtils.writeOutput(outputStream, deviceReadingResponse);
        }
        catch (IllegalArgumentException e) {
            log.error(Constants.MALFORMED_DEVICE_ID_MESSAGE, e);
            streamUtils.writeOutput(outputStream, Constants.MALFORMED_DEVICE_ID_MESSAGE, Constants.BAD_DATA_CODE);
        }
        catch (Exception e) {
            log.error(Constants.UNEXPECTED_ERROR_MESSAGE, e);
            streamUtils.writeOutput(outputStream, Constants.UNEXPECTED_ERROR_MESSAGE, Constants.UNEXPECTED_ERROR_CODE);
        }
    }

    // Read Device info for the given id and return response payload for the GET API
    // Writes error message in response payload, if Device info is not found
    protected DeviceReadingResponse readDeviceInfo(@NonNull UUID id) {
        DeviceReadingResponse deviceReadingResponse = new DeviceReadingResponse();
        deviceReadingResponse.setId(id);

        DeviceInfo deviceInfo = idDeviceInfoMap.getOrDefault(id, null);
        if (deviceInfo == null) {
            deviceReadingResponse.setMessage(Constants.NO_READING_ERROR_MESSAGE);
            deviceReadingResponse.setStatusCode(Constants.NO_DATA_EXIST_CODE);
            log.info("deviceInfo does not exist for id: {}; deviceReadingResponse: {}", id, deviceReadingResponse);
        }
        else {
            deviceReadingResponse.setMessage(Constants.SUCCESS_MESSAGE);
            deviceReadingResponse.setStatusCode(Constants.SUCCESS_CODE);
            deviceReadingResponse.setCumulativeCount(deviceInfo.getCumulativeCount());
            deviceReadingResponse.setLatestCount(deviceInfo.getLatestCount());
            log.info("Found the deviceInfo for id: {}; deviceReadingResponse: {}", id, deviceReadingResponse);
        }

        return deviceReadingResponse;
    }
}
