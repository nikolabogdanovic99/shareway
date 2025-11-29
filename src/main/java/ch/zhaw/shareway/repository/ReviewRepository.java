package ch.zhaw.shareway.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import ch.zhaw.shareway.model.Review;

public interface ReviewRepository extends MongoRepository<Review, String> {
    // Query-Methoden
    List<Review> findByToUserId(String toUserId); // Alle Bewertungen FÜR einen User
    List<Review> findByFromUserId(String fromUserId); // Alle Bewertungen VON einem User
    List<Review> findByRideId(String rideId); // Alle Bewertungen zu einer Fahrt
    
    // Custom Query für Rating-Aggregation (später für Durchschnitt)
    @Query("{ 'toUserId': ?0 }")
    List<Review> findRatingsForUser(String userId);
}