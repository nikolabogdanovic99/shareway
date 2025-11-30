package ch.zhaw.shareway.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api")
public class RideController {

    @Autowired
    RideRepository rideRepository;

    // POST /api/rides - Neue Fahrt erstellen
    @PostMapping("/rides")
    public ResponseEntity<Ride> createRide(@RequestBody RideCreateDTO dto) {
        Integer seatsFree = dto.getSeatsTotal();
        
        Ride ride = new Ride(
            dto.getDriverId(),
            dto.getVehicleId(),
            dto.getStartLocation(),
            dto.getEndLocation(),
            dto.getDepartureTime(),
            dto.getPricePerSeat(),
            dto.getSeatsTotal(),
            seatsFree
        );
        
        Ride savedRide = rideRepository.save(ride);
        return new ResponseEntity<>(savedRide, HttpStatus.CREATED);
    }

    //  GET /api/rides mit Filter-Parametern (wie in Übung 3!)
    @GetMapping("/rides")
    public ResponseEntity<List<Ride>> getAllRides(
        @RequestParam(required = false) RideStatus status,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) Integer minSeats,
        @RequestParam(required = false) String startLocation,
        @RequestParam(required = false) String endLocation
    ) {
        List<Ride> rides;
        
        // Filter-Logik (wie in Übung 3 mit min und type)
        if (status != null && maxPrice != null && minSeats != null) {
            // Alle 3 Filter kombiniert
            rides = rideRepository.findByStatusAndPricePerSeatLessThanEqualAndSeatsFreeGreaterThanEqual(
                status, maxPrice, minSeats
            );
        } else if (status != null && minSeats != null) {
            // Status + minSeats
            rides = rideRepository.findByStatusAndSeatsFreeGreaterThanEqual(status, minSeats);
        } else if (status != null && maxPrice != null) {
            // Status + maxPrice
            rides = rideRepository.findByStatusAndPricePerSeatLessThanEqual(status, maxPrice);
        } else if (status != null) {
            // Nur Status
            rides = rideRepository.findByStatus(status);
        } else if (maxPrice != null) {
            // Nur maxPrice
            rides = rideRepository.findByPricePerSeatLessThanEqual(maxPrice);
        } else if (minSeats != null) {
            // Nur minSeats
            rides = rideRepository.findBySeatsFreeGreaterThanEqual(minSeats);
        } else if (startLocation != null) {
            // Nur startLocation
            rides = rideRepository.findByStartLocationContainingIgnoreCase(startLocation);
        } else if (endLocation != null) {
            // Nur endLocation
            rides = rideRepository.findByEndLocationContainingIgnoreCase(endLocation);
        } else {
            // Keine Filter - alle Rides
            rides = rideRepository.findAll();
        }
        
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // GET /api/rides/{id} - Fahrt by ID
    @GetMapping("/rides/{id}")
    public ResponseEntity<Ride> getRideById(@PathVariable String id) {
        Optional<Ride> optRide = rideRepository.findById(id);
        if (optRide.isPresent()) {
            return new ResponseEntity<>(optRide.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}