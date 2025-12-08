package ch.zhaw.shareway.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.BookingStatus;
import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.model.Vehicle;
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.repository.VehicleRepository;
import ch.zhaw.shareway.repository.BookingRepository;

/**
 * Service layer for Ride business logic
 * Handles validation and state transitions for rides
 */
@Service
public class RideService {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Validate that vehicle exists and belongs to driver
     * 
     * @param vehicleId The vehicle ID to validate
     * @param driverId  The driver ID to check ownership
     * @return true if vehicle exists and belongs to driver, false otherwise
     */
    public boolean validateVehicleForDriver(String vehicleId, String driverId) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle.isEmpty()) {
            return false;
        }
        return vehicle.get().getOwnerId().equals(driverId);
    }

    /**
     * Complete a ride (set status to COMPLETED)
     * 
     * Business Rules:
     * - Ride must exist
     * - Only driver of the ride can complete it
     * - Ride must be IN_PROGRESS
     * 
     * @param rideId   The ride to complete
     * @param driverId The driver requesting completion
     * @return Optional containing the completed ride, or empty if validation fails
     */
    public Optional<Ride> completeRide(String rideId, String driverId) {
        Optional<Ride> rideOpt = rideRepository.findById(rideId);

        if (rideOpt.isEmpty()) {
            return Optional.empty();
        }

        Ride ride = rideOpt.get();

        // Validate: Only driver can complete ride
        if (!ride.getDriverId().equals(driverId)) {
            return Optional.empty();
        }

        // Validate: Ride must be IN_PROGRESS
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            return Optional.empty();
        }

        // Update status
        ride.setStatus(RideStatus.COMPLETED);
        return Optional.of(rideRepository.save(ride));
    }

    /**
     * Cancel a ride (Driver only)
     * 
     * Business Rules:
     * - Ride must exist
     * - Only the driver who created the ride can cancel
     * - Ride must be OPEN or FULL (not COMPLETED or already CANCELED)
     * - All associated bookings will be set to REJECTED
     * 
     * @param rideId   The ride to cancel
     * @param driverId The driver requesting cancellation
     * @return Optional containing the canceled ride, or empty if validation fails
     */
    public Optional<Ride> cancelRide(String rideId, String driverId) {
        Optional<Ride> rideOpt = rideRepository.findById(rideId);

        if (rideOpt.isEmpty()) {
            return Optional.empty();
        }

        Ride ride = rideOpt.get();

        // Validate: Only the driver who created the ride can cancel
        if (!ride.getDriverId().equals(driverId)) {
            return Optional.empty();
        }

        // Validate: Can only cancel OPEN or FULL rides
        if (ride.getStatus() != RideStatus.OPEN && ride.getStatus() != RideStatus.FULL) {
            return Optional.empty();
        }

        // Reject all pending/approved bookings for this ride
        List<Booking> bookings = bookingRepository.findByRideId(rideId);
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.REQUESTED ||
                    booking.getStatus() == BookingStatus.APPROVED) {
                booking.setStatus(BookingStatus.REJECTED);
                booking.setUpdatedAt(LocalDateTime.now());
                bookingRepository.save(booking);
            }
        }

        // Update ride status
        ride.setStatus(RideStatus.CANCELED);

        return Optional.of(rideRepository.save(ride));
    }
}
