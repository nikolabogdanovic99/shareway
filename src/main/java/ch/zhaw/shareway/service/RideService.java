package ch.zhaw.shareway.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.zhaw.shareway.model.Vehicle;
import ch.zhaw.shareway.repository.VehicleRepository;

/**
 * Service layer for Ride business logic
 * Handles validation and state transitions for rides
 */
@Service
public class RideService {

    @Autowired
    private VehicleRepository vehicleRepository;

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
}