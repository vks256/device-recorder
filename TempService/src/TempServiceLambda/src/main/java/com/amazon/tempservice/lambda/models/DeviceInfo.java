package com.amazon.tempservice.lambda.models;

import lombok.Data;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.HashSet;

@Data
public class DeviceInfo {
    Set<String> timestamps;
    String latestTimestamp;
    Integer latestCount;
    Integer cumulativeCount;

    public DeviceInfo() {
        timestamps = new HashSet<>();
        latestTimestamp = null;
        latestCount = 0;
        cumulativeCount = 0;
    }

    // If timestamp exists in the set, it is a duplicate. Ignore it
    // If timestamp is higher than latestTimestamp, update it
    public void updateDeviceInfo(@NonNull String timestamp, @NonNull Integer count) {
        if (timestamps.contains(timestamp))
            return;
        else
            timestamps.add(timestamp);


        if (latestTimestamp == null || isLatestTimestamp(timestamp, latestTimestamp)) {
            latestTimestamp = timestamp;
            latestCount = count;
        }

        cumulativeCount += count;

    }

    // Returns true, if newTs > oldTs
    private boolean isLatestTimestamp(String newTs, String oldTs) {
        OffsetDateTime newDateTime = OffsetDateTime.parse(newTs);
        OffsetDateTime oldDateTime = OffsetDateTime.parse(oldTs);

        return (newDateTime.compareTo(oldDateTime) == 1);
    }
}

