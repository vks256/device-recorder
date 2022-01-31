package com.amazon.tempservice.lambda.models;

public class Constants {
    public static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MESSAGE = "OK";
    public static final String MALFORMED_DATA_ERROR_MESSAGE = "Error: Malformed payload received";
    public static final String MALFORMED_DEVICE_ID_MESSAGE = "Error: Malformed device_id received";
    public static final int BAD_DATA_CODE = 400;
    public static final String NO_READING_ERROR_MESSAGE = "Error: No reading exists for the given device_id";
    public static final int NO_DATA_EXIST_CODE = 404;
    public static final String UNEXPECTED_ERROR_MESSAGE = "Error: Unexpected error occured";
    public static final int UNEXPECTED_ERROR_CODE = 500;
}
