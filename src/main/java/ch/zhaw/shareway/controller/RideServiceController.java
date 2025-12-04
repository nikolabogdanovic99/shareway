package ch.zhaw.shareway.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.BookingActionDTO;
import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideCompleteDTO;
import ch.zhaw.shareway.model.RideStatusAggregationDTO;
import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.VerificationStatus;
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.repository.UserRepository;
import ch.zhaw.shareway.service.BookingService;
import ch.zhaw.shareway.service.RideService;
import ch.zhaw.shareway.service.UserService;

/**
 * Controller for service-related endpoints
 * Handles business logic operations for rides and bookings
 */
@RestController
@RequestMapping("/api/service")
public class RideServiceController {
    
    @Autowired
    private RideService rideService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    
    /**
     * Complete a ride (Driver only)
     * PUT /api/service/rides/complete
     */
    @PutMapping("/rides/complete")
    public ResponseEntity<Ride> completeRide(@RequestBody RideCompleteDTO dto) {
        Optional<Ride> ride = rideService.completeRide(dto.getRideId(), dto.getDriverId());
        
        if (ride.isPresent()) {
            return ResponseEntity.ok(ride.get());
        }
        
        return ResponseEntity.badRequest().build();
    }
    
    /**
     * Approve a booking (Driver only)
     * PUT /api/service/bookings/approve
     */
    @PutMapping("/bookings/approve")
    public ResponseEntity<Booking> approveBooking(@RequestBody BookingActionDTO dto) {
        Optional<Booking> booking = bookingService.approveBooking(
            dto.getBookingId(), 
            dto.getDriverId()
        );
        
        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        }
        
        return ResponseEntity.badRequest().build();
    }
    
    /**
     * Reject a booking (Driver only)
     * PUT /api/service/bookings/reject
     */
    @PutMapping("/bookings/reject")
    public ResponseEntity<Booking> rejectBooking(@RequestBody BookingActionDTO dto) {
        Optional<Booking> booking = bookingService.rejectBooking(
            dto.getBookingId(), 
            dto.getDriverId()
        );
        
        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        }
        
        return ResponseEntity.badRequest().build();
    }
    
    /**
     * Get driver dashboard with rides grouped by status
     * GET /api/service/dashboard/driver?driverId=...
     */
    @GetMapping("/dashboard/driver")
    public ResponseEntity<List<RideStatusAggregationDTO>> getDriverDashboard(
        @RequestParam String driverId
    ) {
        List<RideStatusAggregationDTO> dashboard = 
            rideRepository.getRidesDashboardForDriver(driverId);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Book a ride for myself (Rider)
     * PUT /api/service/me/bookride?rideId=...&seats=...
     */
    @PutMapping("/me/bookride")
    public ResponseEntity<Booking> bookRideForMe(
            @RequestParam String rideId,
            @RequestParam(defaultValue = "1") int seats) {
        String userEmail = userService.getEmail();
        Optional<Booking> booking = bookingService.createBooking(rideId, userEmail, seats);

        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Approve a booking for my ride (Driver)
     * PUT /api/service/me/approvebooking?bookingId=...
     */
    @PutMapping("/me/approvebooking")
    public ResponseEntity<Booking> approveMyBooking(@RequestParam String bookingId) {
        String userEmail = userService.getEmail();
        Optional<Booking> booking = bookingService.approveBooking(bookingId, userEmail);

        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Reject a booking for my ride (Driver)
     * PUT /api/service/me/rejectbooking?bookingId=...
     */
    @PutMapping("/me/rejectbooking")
    public ResponseEntity<Booking> rejectMyBooking(@RequestParam String bookingId) {
        String userEmail = userService.getEmail();
        Optional<Booking> booking = bookingService.rejectBooking(bookingId, userEmail);

        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Verify a user (Admin only)
     * PUT /api/service/admin/verify?userId=...
     */
    @PutMapping("/admin/verify")
    public ResponseEntity<User> verifyUser(@RequestParam String userId) {
        if (!userService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = optUser.get();
        user.setVerificationStatus(VerificationStatus.VERIFIED);
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * Reject a user verification (Admin only)
     * PUT /api/service/admin/reject?userId=...
     */
    @PutMapping("/admin/reject")
    public ResponseEntity<User> rejectUser(@RequestParam String userId) {
        if (!userService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = optUser.get();
        user.setVerificationStatus(VerificationStatus.DENIED);
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }
}