package ch.zhaw.shareway.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.service.BookingService;
import ch.zhaw.shareway.service.RideService;

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
    
    /**
     * Complete a ride (Driver only)
     * PUT /api/service/rides/complete
     * 
     * @param dto Contains rideId and driverId
     * @return The completed ride or BAD_REQUEST if validation fails
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
     * 
     * @param dto Contains bookingId and driverId
     * @return The approved booking or BAD_REQUEST if validation fails
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
     * 
     * @param dto Contains bookingId and driverId
     * @return The rejected booking or BAD_REQUEST if validation fails
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
     * 
     * @param driverId The driver ID to get dashboard for
     * @return List of aggregated ride statistics grouped by status
     */
    @GetMapping("/dashboard/driver")
    public ResponseEntity<List<RideStatusAggregationDTO>> getDriverDashboard(
        @RequestParam String driverId
    ) {
        List<RideStatusAggregationDTO> dashboard = 
            rideRepository.getRidesDashboardForDriver(driverId);
        return ResponseEntity.ok(dashboard);
    }
}
