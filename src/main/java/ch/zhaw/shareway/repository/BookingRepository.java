package ch.zhaw.shareway.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.BookingStatus;

public interface BookingRepository extends MongoRepository<Booking, String> {
    
    // ✅ Query-Methoden für Booking
    
    // Alle Bookings von einem Rider
    List<Booking> findByRiderId(String riderId);
    
    // Alle Bookings für eine Ride
    List<Booking> findByRideId(String rideId);
    
    // Alle Bookings mit einem bestimmten Status
    List<Booking> findByStatus(BookingStatus status);
    
    // Bookings von einem Rider mit bestimmtem Status
    List<Booking> findByRiderIdAndStatus(String riderId, BookingStatus status);
    
    // Bookings für eine Ride mit bestimmtem Status
    List<Booking> findByRideIdAndStatus(String rideId, BookingStatus status);

    List<Booking> findByRiderIdAndRideId(String riderId, String rideId);
    
    // NEU: Prüfen ob Rider bereits für diese Ride gebucht hat
    boolean existsByRideIdAndRiderId(String rideId, String riderId);
}