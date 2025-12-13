package ch.zhaw.shareway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.BookingStatus;
import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.UserRole;
import ch.zhaw.shareway.repository.BookingRepository;
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RideRepository rideRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Ride testRide;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testUser = new User("auth0-id", "rider@test.com", "Test Rider", UserRole.USER);
        // Note: id is set by MongoDB, not manually
        testUser.setFirstName("Test");
        testUser.setLastName("Rider");

        testRide = new Ride();
        testRide.setId("ride-123");
        testRide.setDriverId("driver@test.com");
        testRide.setVehicleId("vehicle-123");
        testRide.setStartLocation("Start");
        testRide.setEndLocation("End");
        testRide.setDepartureTime(LocalDateTime.now().plusDays(1));
        testRide.setPricePerSeat(20.0);
        testRide.setSeatsTotal(4);
        testRide.setSeatsFree(4);
        testRide.setStatus(RideStatus.OPEN);

        testBooking = new Booking("ride-123", "rider@test.com", 1);
        testBooking.setId("booking-123");
        testBooking.setStatus(BookingStatus.REQUESTED);
    }

    // ==================== createBooking Tests ====================

    @Test
    void testCreateBookingSuccess() {
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.existsByRideIdAndRiderId("ride-123", "rider@test.com")).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1);

        assertTrue(result.isPresent());
        assertEquals("ride-123", result.get().getRideId());
        assertEquals("rider@test.com", result.get().getRiderId());
        assertEquals(1, result.get().getSeats());
    }

    @Test
    void testCreateBookingUserNotFound() {
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.empty());

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1);

        assertFalse(result.isPresent());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testCreateBookingUserProfileIncomplete() {
        User incompleteUser = new User("auth0-id", "rider@test.com", "Test", UserRole.USER);
        // firstName and lastName are null -> isProfileComplete() returns false
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(incompleteUser));

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1);

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateBookingRideNotFound() {
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.empty());

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1);

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateBookingDriverCannotBookOwnRide() {
        when(userRepository.findByEmail("driver@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));

        Optional<Booking> result = bookingService.createBooking("ride-123", "driver@test.com", 1);

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateBookingAlreadyBooked() {
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.existsByRideIdAndRiderId("ride-123", "rider@test.com")).thenReturn(true);

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1);

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateBookingRideNotOpen() {
        testRide.setStatus(RideStatus.COMPLETED);
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.existsByRideIdAndRiderId("ride-123", "rider@test.com")).thenReturn(false);

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1);

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateBookingNotEnoughSeats() {
        testRide.setSeatsFree(1);
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.existsByRideIdAndRiderId("ride-123", "rider@test.com")).thenReturn(false);

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 3);

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateBookingWithPickupAndMessage() {
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.existsByRideIdAndRiderId("ride-123", "rider@test.com")).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1, 
                "Pickup Location", "Please wait");

        assertTrue(result.isPresent());
        assertEquals("Pickup Location", result.get().getPickupLocation());
        assertEquals("Please wait", result.get().getMessage());
    }

    @Test
    void testCreateBookingWithValidPromoCode() {
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.existsByRideIdAndRiderId("ride-123", "rider@test.com")).thenReturn(false);
        when(discountService.isValidCode("WELCOME10")).thenReturn(true);
        when(discountService.getDiscountPercent("WELCOME10")).thenReturn(10);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1, 
                null, null, "WELCOME10");

        assertTrue(result.isPresent());
        assertEquals("WELCOME10", result.get().getPromoCode());
        assertEquals(10.0, result.get().getDiscountPercent());
        assertEquals(2.0, result.get().getDiscountAmount()); // 10% of 20
        assertEquals(18.0, result.get().getFinalPrice()); // 20 - 2
    }

    @Test
    void testCreateBookingWithInvalidPromoCode() {
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.existsByRideIdAndRiderId("ride-123", "rider@test.com")).thenReturn(false);
        when(discountService.isValidCode("INVALID")).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1, 
                null, null, "INVALID");

        assertTrue(result.isPresent());
        assertEquals(20.0, result.get().getFinalPrice()); // No discount
    }

    // ==================== approveBooking Tests ====================

    @Test
    void testApproveBookingSuccess() {
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.approveBooking("booking-123", "driver@test.com");

        assertTrue(result.isPresent());
        assertEquals(BookingStatus.APPROVED, result.get().getStatus());
    }

    @Test
    void testApproveBookingNotFound() {
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.empty());

        Optional<Booking> result = bookingService.approveBooking("booking-123", "driver@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testApproveBookingRideNotFound() {
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.empty());

        Optional<Booking> result = bookingService.approveBooking("booking-123", "driver@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testApproveBookingNotDriver() {
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));

        Optional<Booking> result = bookingService.approveBooking("booking-123", "other@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testApproveBookingNotRequestedStatus() {
        testBooking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));

        Optional<Booking> result = bookingService.approveBooking("booking-123", "driver@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testApproveBookingNotEnoughSeats() {
        testRide.setSeatsFree(0);
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));

        Optional<Booking> result = bookingService.approveBooking("booking-123", "driver@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testApproveBookingFillsRide() {
        testRide.setSeatsFree(1);
        testBooking.setSeats(1);
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.approveBooking("booking-123", "driver@test.com");

        assertTrue(result.isPresent());
        assertEquals(RideStatus.FULL, testRide.getStatus());
    }

    // ==================== rejectBooking Tests ====================

    @Test
    void testRejectBookingSuccess() {
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.rejectBooking("booking-123", "driver@test.com");

        assertTrue(result.isPresent());
        assertEquals(BookingStatus.REJECTED, result.get().getStatus());
    }

    @Test
    void testRejectBookingNotFound() {
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.empty());

        Optional<Booking> result = bookingService.rejectBooking("booking-123", "driver@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testRejectBookingRideNotFound() {
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.empty());

        Optional<Booking> result = bookingService.rejectBooking("booking-123", "driver@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testRejectBookingNotDriver() {
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));

        Optional<Booking> result = bookingService.rejectBooking("booking-123", "other@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testRejectBookingNotRequestedStatus() {
        testBooking.setStatus(BookingStatus.CANCELED);
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));

        Optional<Booking> result = bookingService.rejectBooking("booking-123", "driver@test.com");

        assertFalse(result.isPresent());
    }

    // ==================== cancelBooking Tests ====================

    @Test
    void testCancelBookingRequestedSuccess() {
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.cancelBooking("booking-123", "rider@test.com");

        assertTrue(result.isPresent());
        assertEquals(BookingStatus.CANCELED, result.get().getStatus());
    }

    @Test
    void testCancelBookingApprovedRestoresSeats() {
        testBooking.setStatus(BookingStatus.APPROVED);
        testRide.setSeatsFree(3);
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.cancelBooking("booking-123", "rider@test.com");

        assertTrue(result.isPresent());
        assertEquals(BookingStatus.CANCELED, result.get().getStatus());
        assertEquals(4, testRide.getSeatsFree()); // Seats restored
    }

    @Test
    void testCancelBookingApprovedReopensFullRide() {
        testBooking.setStatus(BookingStatus.APPROVED);
        testRide.setSeatsFree(0);
        testRide.setStatus(RideStatus.FULL);
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.cancelBooking("booking-123", "rider@test.com");

        assertTrue(result.isPresent());
        assertEquals(RideStatus.OPEN, testRide.getStatus());
    }

    @Test
    void testCancelBookingNotFound() {
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.empty());

        Optional<Booking> result = bookingService.cancelBooking("booking-123", "rider@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testCancelBookingNotRider() {
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));

        Optional<Booking> result = bookingService.cancelBooking("booking-123", "other@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testCancelBookingWrongStatus() {
        testBooking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));

        Optional<Booking> result = bookingService.cancelBooking("booking-123", "rider@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testCancelBookingApprovedRideNotFound() {
        testBooking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.cancelBooking("booking-123", "rider@test.com");

        assertTrue(result.isPresent());
        assertEquals(BookingStatus.CANCELED, result.get().getStatus());
    }

    @Test
    void testCancelBookingApprovedRideNotFull() {
        testBooking.setStatus(BookingStatus.APPROVED);
        testRide.setSeatsFree(2);
        testRide.setStatus(RideStatus.OPEN); // Not FULL
        when(bookingRepository.findById("booking-123")).thenReturn(Optional.of(testBooking));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.cancelBooking("booking-123", "rider@test.com");

        assertTrue(result.isPresent());
        assertEquals(RideStatus.OPEN, testRide.getStatus()); // Still OPEN
        assertEquals(3, testRide.getSeatsFree()); // Seats restored
    }

    @Test
    void testCreateBookingWithEmptyPickupLocation() {
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.existsByRideIdAndRiderId("ride-123", "rider@test.com")).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1, 
                "", "Message");

        assertTrue(result.isPresent());
        // pickupLocation should not be set when empty
    }

    @Test
    void testCreateBookingWithEmptyMessage() {
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.existsByRideIdAndRiderId("ride-123", "rider@test.com")).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1, 
                "Pickup", "");

        assertTrue(result.isPresent());
        // message should not be set when empty
    }

    @Test
    void testCreateBookingWithEmptyPromoCode() {
        when(userRepository.findByEmail("rider@test.com")).thenReturn(Optional.of(testUser));
        when(rideRepository.findById("ride-123")).thenReturn(Optional.of(testRide));
        when(bookingRepository.existsByRideIdAndRiderId("ride-123", "rider@test.com")).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Booking> result = bookingService.createBooking("ride-123", "rider@test.com", 1, 
                null, null, "");

        assertTrue(result.isPresent());
        assertEquals(20.0, result.get().getFinalPrice()); // No discount
    }
}
