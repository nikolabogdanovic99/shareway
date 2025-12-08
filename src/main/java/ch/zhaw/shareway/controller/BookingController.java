package ch.zhaw.shareway.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.BookingCreateDTO;
import ch.zhaw.shareway.repository.BookingRepository;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Autowired
    BookingRepository bookingRepository;

    // POST /api/bookings - Neue Buchung erstellen
    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking(@RequestBody BookingCreateDTO dto) {
        // TODO: Später Validierung:
        // - Ride existiert?
        // - Genug freie Plätze?
        // - Ride status = OPEN?
        
        Booking booking = new Booking(
            dto.getRideId(),
            dto.getRiderId(),
            dto.getSeats()
        );
        
        // Pickup Location setzen (Pflicht)
        booking.setPickupLocation(dto.getPickupLocation());
        
        // Message setzen (optional)
        if (dto.getMessage() != null && !dto.getMessage().isEmpty()) {
            booking.setMessage(dto.getMessage());
        }
        
        // status = REQUESTED (default)
        // createdAt = now (default)
        
        Booking savedBooking = bookingRepository.save(booking);
        return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
    }

    // GET /api/bookings - Alle Buchungen
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> allBookings = bookingRepository.findAll();
        return new ResponseEntity<>(allBookings, HttpStatus.OK);
    }

    // GET /api/bookings/{id} - Buchung by ID
    @GetMapping("/bookings/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable String id) {
        Optional<Booking> optBooking = bookingRepository.findById(id);
        if (optBooking.isPresent()) {
            return new ResponseEntity<>(optBooking.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}