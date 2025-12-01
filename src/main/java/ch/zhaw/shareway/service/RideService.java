package ch.zhaw.shareway.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.model.Vehicle;
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.repository.VehicleRepository;

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
    
    /**
     * Validate that vehicle exists and belongs to driver
     * 
     * @param vehicleId The vehicle ID to validate
     * @param driverId The driver ID to check ownership
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
     * @param rideId The ride to complete
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
}
