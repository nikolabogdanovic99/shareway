package ch.zhaw.shareway.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.BookingStatus;
import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.repository.BookingRepository;
import ch.zhaw.shareway.repository.RideRepository;

/**
 * Service layer for Booking business logic
 * Handles validation and state transitions for bookings
 */
@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private RideRepository rideRepository;
    
    /**
     * Create a booking for a ride
     * 
     * Business Rules:
     * - Ride must exist
     * - Ride must be OPEN
     * - Enough seats must be available
     * 
     * @param rideId The ride to book
     * @param riderId The rider (email) requesting the booking
     * @param seats Number of seats to book
     * @return Optional containing the created booking, or empty if validation fails
     */
    public Optional<Booking> createBooking(String rideId, String riderId, int seats) {
        return createBooking(rideId, riderId, seats, null, null);
    }
    
    /**
     * Create a booking for a ride with pickup location and message
     * 
     * @param rideId The ride to book
     * @param riderId The rider (email) requesting the booking
     * @param seats Number of seats to book
     * @param pickupLocation Where to pick up the rider
     * @param message Optional message to the driver
     * @return Optional containing the created booking, or empty if validation fails
     */
    public Optional<Booking> createBooking(String rideId, String riderId, int seats, 
                                           String pickupLocation, String message) {
        Optional<Ride> rideOpt = rideRepository.findById(rideId);
        
        if (rideOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Ride ride = rideOpt.get();
        
        // Validate: Ride must be OPEN
        if (ride.getStatus() != RideStatus.OPEN) {
            return Optional.empty();
        }
        
        // Validate: Enough seats available
        if (ride.getSeatsFree() < seats) {
            return Optional.empty();
        }
        
        // Create booking using constructor
        Booking booking = new Booking(rideId, riderId, seats);
        booking.setUpdatedAt(LocalDateTime.now());
        
        // Set pickup location
        if (pickupLocation != null && !pickupLocation.isEmpty()) {
            booking.setPickupLocation(pickupLocation);
        }
        
        // Set message
        if (message != null && !message.isEmpty()) {
            booking.setMessage(message);
        }
        
        return Optional.of(bookingRepository.save(booking));
    }
    
    /**
     * Approve a booking and update available seats
     * 
     * Business Rules:
     * - Booking must exist
     * - Associated ride must exist
     * - Only ride driver can approve
     * - Booking must be REQUESTED
     * - Enough seats must be available
     * - Update seats and ride status
     * 
     * @param bookingId The booking to approve
     * @param driverId The driver requesting approval
     * @return Optional containing the approved booking, or empty if validation fails
     */
    public Optional<Booking> approveBooking(String bookingId, String driverId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        
        if (bookingOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Booking booking = bookingOpt.get();
        
        // Get associated ride
        Optional<Ride> rideOpt = rideRepository.findById(booking.getRideId());
        if (rideOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Ride ride = rideOpt.get();
        
        // Validate: Only ride driver can approve
        if (!ride.getDriverId().equals(driverId)) {
            return Optional.empty();
        }
        
        // Validate: Booking must be REQUESTED
        if (booking.getStatus() != BookingStatus.REQUESTED) {
            return Optional.empty();
        }
        
        // Validate: Enough seats available
        if (ride.getSeatsFree() < booking.getSeats()) {
            return Optional.empty();
        }
        
        // Update booking status
        booking.setStatus(BookingStatus.APPROVED);
        booking.setUpdatedAt(LocalDateTime.now());
        
        // Update available seats
        ride.setSeatsFree(ride.getSeatsFree() - booking.getSeats());
        
        // Update ride status if full
        if (ride.getSeatsFree() == 0) {
            ride.setStatus(RideStatus.FULL);
        }
        
        rideRepository.save(ride);
        return Optional.of(bookingRepository.save(booking));
    }
    
    /**
     * Reject a booking
     * 
     * Business Rules:
     * - Booking must exist
     * - Associated ride must exist
     * - Only ride driver can reject
     * - Booking must be REQUESTED
     * 
     * @param bookingId The booking to reject
     * @param driverId The driver requesting rejection
     * @return Optional containing the rejected booking, or empty if validation fails
     */
    public Optional<Booking> rejectBooking(String bookingId, String driverId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        
        if (bookingOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Booking booking = bookingOpt.get();
        
        // Get associated ride
        Optional<Ride> rideOpt = rideRepository.findById(booking.getRideId());
        if (rideOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Ride ride = rideOpt.get();
        
        // Validate: Only ride driver can reject
        if (!ride.getDriverId().equals(driverId)) {
            return Optional.empty();
        }
        
        // Validate: Booking must be REQUESTED
        if (booking.getStatus() != BookingStatus.REQUESTED) {
            return Optional.empty();
        }
        
        // Update booking status
        booking.setStatus(BookingStatus.REJECTED);
        booking.setUpdatedAt(LocalDateTime.now());
        
        return Optional.of(bookingRepository.save(booking));
    }
}