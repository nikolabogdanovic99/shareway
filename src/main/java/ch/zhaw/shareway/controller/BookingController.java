package ch.zhaw.shareway.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.repository.BookingRepository;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Autowired
    BookingRepository bookingRepository;

    // GET /api/bookings - Alle Buchungen
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> allBookings = bookingRepository.findAll();
        return new ResponseEntity<>(allBookings, HttpStatus.OK);
    }
}