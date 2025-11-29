package ch.zhaw.shareway.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.BookingStatus;

public interface BookingRepository extends MongoRepository<Booking, String> {
    // Query-Methoden (wie in Übung 3)
    List<Booking> findByRiderId(String riderId);
    List<Booking> findByRideId(String rideId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByRiderIdAndStatus(String riderId, BookingStatus status);
    List<Booking> findByRideIdAndStatus(String rideId, BookingStatus status);
}