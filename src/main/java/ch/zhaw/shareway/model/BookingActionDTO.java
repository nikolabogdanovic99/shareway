package ch.zhaw.shareway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for booking actions (approve/reject)
 * Used by driver to approve or reject booking requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingActionDTO {
    private String bookingId;
    private String driverId;
}
