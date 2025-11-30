package ch.zhaw.shareway.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;

public interface RideRepository extends MongoRepository<Ride, String> {
    
    // ✅ Query-Methoden für Filter (wie in Übung 3 mit findByEarningsGreaterThan)
    
    // Filter nach Status
    List<Ride> findByStatus(RideStatus status);
    
    // Filter nach Preis (maxPrice)
    List<Ride> findByPricePerSeatLessThanEqual(Double maxPrice);
    
    // Filter nach freien Plätzen (minSeats)
    List<Ride> findBySeatsFreeGreaterThanEqual(Integer minSeats);
    
    // Filter nach Startort
    List<Ride> findByStartLocationContainingIgnoreCase(String location);
    
    // Filter nach Zielort
    List<Ride> findByEndLocationContainingIgnoreCase(String location);
    
    // Filter nach Datum (zwischen zwei Zeitpunkten)
    List<Ride> findByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);
    
    // Filter nach Driver
    List<Ride> findByDriverId(String driverId);
    
    // ✅ Kombinierte Filter (wie in Übung 3 mit "And")
    
    // Status + minSeats
    List<Ride> findByStatusAndSeatsFreeGreaterThanEqual(
        RideStatus status, 
        Integer minSeats
    );
    
    // Status + maxPrice
    List<Ride> findByStatusAndPricePerSeatLessThanEqual(
        RideStatus status,
        Double maxPrice
    );
    
    // Status + minSeats + maxPrice (alle 3!)
    List<Ride> findByStatusAndPricePerSeatLessThanEqualAndSeatsFreeGreaterThanEqual(
        RideStatus status,
        Double maxPrice,
        Integer minSeats
    );
}