package ch.zhaw.shareway.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.BookingStatus;
import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.repository.BookingRepository;
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.repository.UserRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<Booking> createBooking(String rideId, String riderId, int seats) {
        return createBooking(rideId, riderId, seats, null, null);
    }

    public Optional<Booking> createBooking(String rideId, String riderId, int seats,
            String pickupLocation, String message) {
        
        // NEU: Prüfen ob User existiert und Profil vollständig ist
        Optional<User> userOpt = userRepository.findByEmail(riderId);
        if (userOpt.isEmpty() || !userOpt.get().isProfileComplete()) {
            return Optional.empty();
        }

        Optional<Ride> rideOpt = rideRepository.findById(rideId);

        if (rideOpt.isEmpty()) {
            return Optional.empty();
        }

        Ride ride = rideOpt.get();

        // Validate: Rider cannot book their own ride
        if (ride.getDriverId().equals(riderId)) {
            return Optional.empty();
        }

        // Validate: Rider cannot book the same ride twice
        if (bookingRepository.existsByRideIdAndRiderId(rideId, riderId)) {
            return Optional.empty();
        }

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

    public Optional<Booking> approveBooking(String bookingId, String driverId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            return Optional.empty();
        }

        Booking booking = bookingOpt.get();

        Optional<Ride> rideOpt = rideRepository.findById(booking.getRideId());
        if (rideOpt.isEmpty()) {
            return Optional.empty();
        }

        Ride ride = rideOpt.get();

        if (!ride.getDriverId().equals(driverId)) {
            return Optional.empty();
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            return Optional.empty();
        }

        if (ride.getSeatsFree() < booking.getSeats()) {
            return Optional.empty();
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setUpdatedAt(LocalDateTime.now());

        ride.setSeatsFree(ride.getSeatsFree() - booking.getSeats());

        if (ride.getSeatsFree() == 0) {
            ride.setStatus(RideStatus.FULL);
        }

        rideRepository.save(ride);
        return Optional.of(bookingRepository.save(booking));
    }

    public Optional<Booking> rejectBooking(String bookingId, String driverId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            return Optional.empty();
        }

        Booking booking = bookingOpt.get();

        Optional<Ride> rideOpt = rideRepository.findById(booking.getRideId());
        if (rideOpt.isEmpty()) {
            return Optional.empty();
        }

        Ride ride = rideOpt.get();

        if (!ride.getDriverId().equals(driverId)) {
            return Optional.empty();
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            return Optional.empty();
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setUpdatedAt(LocalDateTime.now());

        return Optional.of(bookingRepository.save(booking));
    }

    public Optional<Booking> cancelBooking(String bookingId, String riderId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            return Optional.empty();
        }

        Booking booking = bookingOpt.get();

        if (!booking.getRiderId().equals(riderId)) {
            return Optional.empty();
        }

        if (booking.getStatus() != BookingStatus.REQUESTED &&
                booking.getStatus() != BookingStatus.APPROVED) {
            return Optional.empty();
        }

        if (booking.getStatus() == BookingStatus.APPROVED) {
            Optional<Ride> rideOpt = rideRepository.findById(booking.getRideId());
            if (rideOpt.isPresent()) {
                Ride ride = rideOpt.get();
                ride.setSeatsFree(ride.getSeatsFree() + booking.getSeats());

                if (ride.getStatus() == RideStatus.FULL) {
                    ride.setStatus(RideStatus.OPEN);
                }

                rideRepository.save(ride);
            }
        }

        booking.setStatus(BookingStatus.CANCELED);
        booking.setUpdatedAt(LocalDateTime.now());

        return Optional.of(bookingRepository.save(booking));
    }
}