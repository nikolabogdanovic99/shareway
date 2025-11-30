package ch.zhaw.shareway.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import ch.zhaw.shareway.model.Review;

public interface ReviewRepository extends MongoRepository<Review, String> {
    
    // Query-Methoden für Review
    
    // Alle Reviews FÜR einen User (wird bewertet)
    List<Review> findByToUserId(String toUserId);
    
    // Alle Reviews VON einem User (hat bewertet)
    List<Review> findByFromUserId(String fromUserId);
    
    // Alle Reviews zu einer Ride
    List<Review> findByRideId(String rideId);
    
    // Custom Query für Rating-Aggregation (wie in Übung 3)
    // Später nützlich für Durchschnitts-Rating berechnen
    @Query("{ 'toUserId': ?0 }")
    List<Review> findRatingsForUser(String userId);
}