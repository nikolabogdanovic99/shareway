package ch.zhaw.shareway.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for aggregation pipeline results
 * Groups rides by status with count and IDs
 */
@NoArgsConstructor
@Getter
public class RideStatusAggregationDTO {
    private String id;           // RideStatus (OPEN, FULL, COMPLETED, etc.)
    private Long count;          // Number of rides with this status
    private List<String> rideIds; // Array of ride IDs
}
