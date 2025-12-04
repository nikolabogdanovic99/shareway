package ch.zhaw.shareway.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import ch.zhaw.shareway.service.UserService;

@RestController
@RequestMapping("/api")
public class RideController {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private RideService rideService;

    @Autowired
    private UserService userService;

    @PostMapping("/rides")
    public ResponseEntity<Ride> createRide(@RequestBody RideCreateDTO rideDTO) {
        // Nur driver oder admin dürfen Rides erstellen
        if (!userService.userHasRole("driver") && !userService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Validate vehicle exists and belongs to driver
        if (!rideService.validateVehicleForDriver(
                rideDTO.getVehicleId(),
                rideDTO.getDriverId())) {
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
                rideDTO.getSeatsTotal());

        // Optional fields
        if (rideDTO.getDescription() != null) {
            ride.setDescription(rideDTO.getDescription());
        }
        if (rideDTO.getDurationMinutes() != null) {
            ride.setDurationMinutes(rideDTO.getDurationMinutes());
        }
        if (rideDTO.getDistanceKm() != null) {
            ride.setDistanceKm(rideDTO.getDistanceKm());
        }

        Ride savedRide = rideRepository.save(ride);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRide);
    }

    @GetMapping("/rides")
    public ResponseEntity<Page<Ride>> getAllRides(
            @RequestParam(required = false) RideStatus status,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize) {

        Page<Ride> rides;

        if (status == null && maxPrice == null) {
            rides = rideRepository.findAll(PageRequest.of(pageNumber - 1, pageSize));
        } else if (status != null && maxPrice != null) {
            rides = rideRepository.findByStatusAndPricePerSeatLessThanEqual(status, maxPrice,
                    PageRequest.of(pageNumber - 1, pageSize));
        } else if (maxPrice != null) {
            rides = rideRepository.findByPricePerSeatLessThanEqual(maxPrice,
                    PageRequest.of(pageNumber - 1, pageSize));
        } else {
            rides = rideRepository.findByStatus(status, PageRequest.of(pageNumber - 1, pageSize));
        }

        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    @GetMapping("/rides/{id}")
    public ResponseEntity<Ride> getRideById(@PathVariable String id) {
        Optional<Ride> ride = rideRepository.findById(id);
        if (ride.isPresent()) {
            return ResponseEntity.ok(ride.get());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/rides/{id}")
    public ResponseEntity<String> deleteRide(@PathVariable String id) {
        Optional<Ride> ride = rideRepository.findById(id);

        if (ride.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Admin darf alle löschen, Driver nur eigene
        String userEmail = userService.getEmail();
        boolean isAdmin = userService.userHasRole("admin");
        boolean isOwnRide = ride.get().getDriverId().equals(userEmail);

        if (!isAdmin && !isOwnRide) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        rideRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("DELETED");
    }
}