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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideCreateDTO;
import ch.zhaw.shareway.repository.RideRepository;

@RestController
@RequestMapping("/api")
public class RideController {

    @Autowired
    RideRepository rideRepository;

    // POST /api/rides - Neue Fahrt erstellen (AKTUALISIERT!)
    @PostMapping("/rides")
    public ResponseEntity<Ride> createRide(@RequestBody RideCreateDTO dto) {
        // seatsFree = seatsTotal bei Erstellung
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
        
        // Optional: routeRadiusKm, distanceKm, durationMinutes
        // status = OPEN (default)
        // createdAt = now (default)
        
        Ride savedRide = rideRepository.save(ride);
        return new ResponseEntity<>(savedRide, HttpStatus.CREATED);
    }

    // GET /api/rides - Alle Fahrten
    @GetMapping("/rides")
    public ResponseEntity<List<Ride>> getAllRides() {
        List<Ride> allRides = rideRepository.findAll();
        return new ResponseEntity<>(allRides, HttpStatus.OK);
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