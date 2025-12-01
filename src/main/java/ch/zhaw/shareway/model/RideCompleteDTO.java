package ch.zhaw.shareway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for completing a ride
 * Used by driver to mark ride as completed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideCompleteDTO {
    private String rideId;
    private String driverId;
}
