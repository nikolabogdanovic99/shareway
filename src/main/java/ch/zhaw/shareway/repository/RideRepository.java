package ch.zhaw.shareway.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import org.springframework.data.domain.Pageable;

/**
 * Repository interface for Ride entity
 * Provides CRUD operations and custom queries
 */
public interface RideRepository extends MongoRepository<Ride, String> {

    // ========== BASIC QUERIES ==========

    List<Ride> findByStatus(RideStatus status);
   // ========== PAGINATION QUERIES ==========

    Page<Ride> findByStatus(RideStatus status, Pageable pageable);

    Page<Ride> findByPricePerSeatLessThanEqual(Double maxPrice, Pageable pageable);

    Page<Ride> findByStatusAndPricePerSeatLessThanEqual(RideStatus status, Double maxPrice, Pageable pageable);

}
