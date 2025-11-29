package ch.zhaw.shareway.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.shareway.model.Vehicle;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    // Query-Methoden
    List<Vehicle> findByOwnerId(String ownerId);
    List<Vehicle> findBySeatsGreaterThanEqual(Integer minSeats);
    List<Vehicle> findByMake(String make);
    List<Vehicle> findByModel(String model);
}