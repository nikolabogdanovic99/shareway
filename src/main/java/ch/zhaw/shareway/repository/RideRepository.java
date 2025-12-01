package ch.zhaw.shareway.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.model.RideStatusAggregationDTO;

/**
 * Repository interface for Ride entity
 * Provides CRUD operations and custom queries
 */
public interface RideRepository extends MongoRepository<Ride, String> {
    
    // ========== BASIC QUERIES ==========
    
    List<Ride> findByStatus(RideStatus status);
    List<Ride> findByPricePerSeatLessThanEqual(Double maxPrice);
    List<Ride> findBySeatsFreeGreaterThanEqual(Integer minSeats);
    List<Ride> findByStartLocationContainingIgnoreCase(String location);
    List<Ride> findByEndLocationContainingIgnoreCase(String location);
    List<Ride> findByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Ride> findByDriverId(String driverId);
    
    // Combined filters
    List<Ride> findByStatusAndSeatsFreeGreaterThanEqual(RideStatus status, Integer minSeats);
    List<Ride> findByStatusAndPricePerSeatLessThanEqual(RideStatus status, Double maxPrice);
    List<Ride> findByStatusAndPricePerSeatLessThanEqualAndSeatsFreeGreaterThanEqual(
        RideStatus status, Double maxPrice, Integer minSeats);
    
    // ========== AGGREGATION PIPELINE  ==========
    
    /**
     * Aggregation pipeline for driver dashboard
     * Groups rides by status with count and list of IDs
     * 
     * Pipeline stages:
     * 1. $match: Filter rides by driverId
     * 2. $group: Group by status, count documents, collect IDs
     * 
     * @param driverId The driver ID to filter by
     * @return List of aggregation results with status, count, and rideIds
     */
    @Aggregation({
        "{'$match': {'driverId': ?0}}",
        "{'$group': {" +
            "'_id': '$status', " +
            "'count': {'$sum': 1}, " +
            "'rideIds': {'$push': {'$toString': '$_id'}}" +
        "}}"
    })
    List<RideStatusAggregationDTO> getRidesDashboardForDriver(String driverId);
}
