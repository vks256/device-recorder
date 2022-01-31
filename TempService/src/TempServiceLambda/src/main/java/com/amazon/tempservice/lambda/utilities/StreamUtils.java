package com.amazon.tempservice.lambda.utilities;

import com.amazon.tempservice.lambda.models.Constants;
import com.amazon.tempservice.lambda.models.DeviceReadingRequest;
import com.amazon.tempservice.lambda.models.DeviceReadingResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Log4j2
public class StreamUtils {

    private ObjectMapper objectMapper;

    public StreamUtils() {
        objectMapper = new ObjectMapper();
    }

    // This is special method to differentiate between POST and GET APIs.
    // Since the lambda is shared by both POST and GET APIs, the GET API pass an extra parameter "getApi" as queryString
    // This method looks for this special param and figures out if the request is coming from GET API
    public boolean isCallForGETAPI(String inputStream) {
        try {
            JsonNode inputNode = this.objectMapper.readTree(inputStream);
            if (inputNode == null) {
                throw new IllegalArgumentException(Constants.MALFORMED_DATA_ERROR_MESSAGE);
            } else if (inputNode.has("getApi")) {
                return true;
            }
            return false;
        }
        catch (Exception e) {
            throw new RuntimeException("Exception occured in parsing the inputStream", e);
        }
    }

    // Verify if id is a valid UUID, throw exception if not.
    public UUID parseDeviceId(String inputStream) throws Exception {
        JsonNode inputNode = this.objectMapper.readTree(inputStream);

        if (inputNode == null || !inputNode.has("id")) {
            throw new IOException("Invalid device_id");
        }
        return UUID.fromString(inputNode.get("id").asText());
    }

    public UUID parseDeviceIdBackup(String inputStream) {
        try {
            JsonNode inputNode = this.objectMapper.readTree(inputStream);

            if (inputNode == null || !inputNode.has("id")) {
                throw new IllegalArgumentException("Invalid device_id");
            }
            return UUID.fromString(inputNode.get("id").asText());
        }
        catch (Exception e) {
            throw new RuntimeException("Invalid device_id", e);
        }
    }

    public DeviceReadingRequest parseReadingRequest(String inputStream) {
        DeviceReadingRequest deviceReadingRequest = null;
        try {
            deviceReadingRequest = objectMapper.readValue(inputStream, DeviceReadingRequest.class);
            // Verify Malformed data
            if (deviceReadingRequest == null) {
                throw new IllegalArgumentException("Invalid payload");
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Exception occured in parsing the Reading request payload", e);
        }
        return deviceReadingRequest;
    }

    public String convertStreamToString(InputStream inputStream) throws Exception {
        JsonNode inputNode = this.objectMapper.readTree(inputStream);
        System.out.println("Debug: inputStream: " + inputNode);
        return inputNode.toString();
    }

    public void writeOutput(OutputStream outputStream, @NonNull DeviceReadingResponse deviceReadingResponse) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(objectMapper.writeValueAsString(deviceReadingResponse));
        writer.close();
    }

    public void writeOutput(OutputStream outputStream, @NonNull String message, int statucCode) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("statusCode", statucCode);
        response.put("message", message);
        String responseString = ((JsonNode) response).toString();

        System.out.println("Debug: responseString: " + responseString);
        writer.write(responseString);
        writer.close();
    }

}
