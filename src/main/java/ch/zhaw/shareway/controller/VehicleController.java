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
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.shareway.model.Vehicle;
import ch.zhaw.shareway.model.VehicleCreateDTO;
import ch.zhaw.shareway.repository.VehicleRepository;
import ch.zhaw.shareway.service.UserService;

@RestController
@RequestMapping("/api")
public class VehicleController {

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    UserService userService;

    // POST /api/vehicles - Neues Fahrzeug erstellen
    @PostMapping("/vehicles")
    public ResponseEntity<Vehicle> createVehicle(@RequestBody VehicleCreateDTO dto) {
        // Nur driver oder admin dürfen Vehicles erstellen
        if (!userService.userHasRole("driver") && !userService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Vehicle vehicle = new Vehicle(
                dto.getOwnerId(),
                dto.getMake(),
                dto.getModel(),
                dto.getSeats(),
                dto.getPlateHash());

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return new ResponseEntity<>(savedVehicle, HttpStatus.CREATED);
    }

    // GET /api/vehicles - Alle Fahrzeuge
    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        return new ResponseEntity<>(allVehicles, HttpStatus.OK);
    }

    // GET /api/vehicles/{id} - Fahrzeug by ID
    @GetMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable String id) {
        Optional<Vehicle> optVehicle = vehicleRepository.findById(id);
        if (optVehicle.isPresent()) {
            return new ResponseEntity<>(optVehicle.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}