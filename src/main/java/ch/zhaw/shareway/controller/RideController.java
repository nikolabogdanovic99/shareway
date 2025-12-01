package ch.zhaw.shareway.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideCreateDTO;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.service.RideService;

/**
 * REST Controller for Ride endpoints
 * Handles CRUD operations for rides with filtering
 */
@RestController
@RequestMapping("/api")
public class RideController {
    
    @Autowired
    private RideRepository rideRepository;
    
    @Autowired
    private RideService rideService;  // NEW: Service injection for validation
    
    /**
     * Create new ride with vehicle validation
     * POST /api/rides
     * 
     * BUG FIX: Now validates that vehicle exists and belongs to driver
     * 
     * @param rideDTO The ride data to create
     * @return CREATED with ride data, or BAD_REQUEST if validation fails
     */
    @PostMapping("/rides")
    public ResponseEntity<Ride> createRide(@RequestBody RideCreateDTO rideDTO) {
        // BUG FIX: Validate vehicle exists and belongs to driver
        if (!rideService.validateVehicleForDriver(
            rideDTO.getVehicleId(), 
            rideDTO.getDriverId()
        )) {
            return ResponseEntity.badRequest().build();
        }
        
        Ride ride = new Ride(
            rideDTO.getDriverId(),
            rideDTO.getVehicleId(),
            rideDTO.getStartLocation(),
            rideDTO.getEndLocation(),
            rideDTO.getDepartureTime(),
            rideDTO.getPricePerSeat(),
            rideDTO.getSeatsTotal(),
            rideDTO.getSeatsTotal()  // seatsFree = seatsTotal initially
        );
        
        Ride savedRide = rideRepository.save(ride);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRide);
    }
    
    /**
     * Get all rides with optional filters
     * GET /api/rides?status=...&maxPrice=...&minSeats=...&startLocation=...&endLocation=...
     * 
     * @param status Filter by ride status (OPEN, FULL, IN_PROGRESS, COMPLETED, CANCELLED)
     * @param maxPrice Filter by maximum price per seat
     * @param minSeats Filter by minimum free seats
     * @param startLocation Filter by start location (case-insensitive partial match)
     * @param endLocation Filter by end location (case-insensitive partial match)
     * @return List of rides matching the filters
     */
    @GetMapping("/rides")
    public ResponseEntity<List<Ride>> getAllRides(
        @RequestParam(required = false) RideStatus status,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) Integer minSeats,
        @RequestParam(required = false) String startLocation,
        @RequestParam(required = false) String endLocation
    ) {
        List<Ride> rides;
        
        // Filter logic (from Übung 3)
        if (status != null && maxPrice != null && minSeats != null) {
            rides = rideRepository.findByStatusAndPricePerSeatLessThanEqualAndSeatsFreeGreaterThanEqual(
                status, maxPrice, minSeats);
        } else if (status != null && minSeats != null) {
            rides = rideRepository.findByStatusAndSeatsFreeGreaterThanEqual(status, minSeats);
        } else if (status != null && maxPrice != null) {
            rides = rideRepository.findByStatusAndPricePerSeatLessThanEqual(status, maxPrice);
        } else if (status != null) {
            rides = rideRepository.findByStatus(status);
        } else if (maxPrice != null) {
            rides = rideRepository.findByPricePerSeatLessThanEqual(maxPrice);
        } else if (minSeats != null) {
            rides = rideRepository.findBySeatsFreeGreaterThanEqual(minSeats);
        } else if (startLocation != null) {
            rides = rideRepository.findByStartLocationContainingIgnoreCase(startLocation);
        } else if (endLocation != null) {
            rides = rideRepository.findByEndLocationContainingIgnoreCase(endLocation);
        } else {
            rides = rideRepository.findAll();
        }
        
        return ResponseEntity.ok(rides);
    }
    
    /**
     * Get ride by ID
     * GET /api/rides/{id}
     * 
     * @param id The ride ID
     * @return The ride or NOT_FOUND
     */
    @GetMapping("/rides/{id}")
    public ResponseEntity<Ride> getRideById(@PathVariable String id) {
        Optional<Ride> ride = rideRepository.findById(id);
        if (ride.isPresent()) {
            return ResponseEntity.ok(ride.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Delete ride by ID
     * DELETE /api/rides/{id}
     * 
     * @param id The ride ID to delete
     * @return NO_CONTENT if deleted, NOT_FOUND if ride doesn't exist
     */
    @DeleteMapping("/rides/{id}")
    public ResponseEntity<Void> deleteRide(@PathVariable String id) {
        Optional<Ride> ride = rideRepository.findById(id);
        
        if (ride.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        rideRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
