package ch.zhaw.shareway.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.zhaw.shareway.model.Vehicle;
import ch.zhaw.shareway.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
public class RideServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private RideService rideService;

    @Test
    void testValidateVehicleForDriverSuccess() {
        // Use constructor: ownerId, make, model, seats, plateHash
        Vehicle vehicle = new Vehicle("driver@test.com", "Make", "Model", 4, "PLATE123");
        
        when(vehicleRepository.findById("vehicle-123")).thenReturn(Optional.of(vehicle));

        boolean result = rideService.validateVehicleForDriver("vehicle-123", "driver@test.com");

        assertTrue(result);
    }

    @Test
    void testValidateVehicleForDriverVehicleNotFound() {
        when(vehicleRepository.findById("vehicle-123")).thenReturn(Optional.empty());

        boolean result = rideService.validateVehicleForDriver("vehicle-123", "driver@test.com");

        assertFalse(result);
    }

    @Test
    void testValidateVehicleForDriverWrongOwner() {
        // Use constructor: ownerId, make, model, seats, plateHash
        Vehicle vehicle = new Vehicle("other-driver@test.com", "Make", "Model", 4, "PLATE123");
        
        when(vehicleRepository.findById("vehicle-123")).thenReturn(Optional.of(vehicle));

        boolean result = rideService.validateVehicleForDriver("vehicle-123", "driver@test.com");

        assertFalse(result);
    }
}
