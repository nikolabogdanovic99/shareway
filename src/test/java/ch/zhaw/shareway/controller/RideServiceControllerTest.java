package ch.zhaw.shareway.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.BookingStatus;
import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.UserRole;
import ch.zhaw.shareway.model.VerificationStatus;
import ch.zhaw.shareway.repository.BookingRepository;
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.repository.UserRepository;
import ch.zhaw.shareway.security.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class RideServiceControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    BookingRepository bookingRepository;

    private static String test_user_id = "";
    private static String test_ride_id = "";
    private static String test_booking_id = "";
    private static String driver_user_id = "";
    private static String rider_user_id = "";

    // ==================== Setup ====================

    @Test
    @Order(5)
    public void testSetupDriverUser() throws Exception {
        userRepository.findByEmail("admin@test.com").ifPresent(u -> userRepository.delete(u));
        
        User driver = new User("admin-auth0-id", "admin@test.com", "Admin Driver", UserRole.ADMIN);
        driver.setFirstName("Admin");
        driver.setLastName("Driver");
        User savedDriver = userRepository.save(driver);
        driver_user_id = savedDriver.getId();
        System.out.println("created driver user with id " + driver_user_id);
    }

    @Test
    @Order(6)
    public void testSetupRiderUser() throws Exception {
        userRepository.findByEmail("user@test.com").ifPresent(u -> userRepository.delete(u));
        
        User rider = new User("user-auth0-id", "user@test.com", "Test Rider", UserRole.USER);
        rider.setFirstName("Test");
        rider.setLastName("Rider");
        User savedRider = userRepository.save(rider);
        rider_user_id = savedRider.getId();
        System.out.println("created rider user with id " + rider_user_id);
    }

    @Test
    @Order(7)
    public void testSetupVerificationUser() throws Exception {
        User user = new User("test-auth0-id", "testverify@test.com", "Test User", UserRole.USER);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setVerificationStatus(VerificationStatus.PENDING);
        User savedUser = userRepository.save(user);
        test_user_id = savedUser.getId();
        System.out.println("created test user with id " + test_user_id);
    }

    @Test
    @Order(8)
    public void testSetupRide() throws Exception {
        Ride ride = new Ride();
        ride.setDriverId("admin@test.com");
        ride.setStartLocation("TEST-BOOKING-START");
        ride.setEndLocation("TEST-BOOKING-END");
        ride.setDepartureTime(LocalDateTime.now().plusDays(1));
        ride.setPricePerSeat(20.0);
        ride.setSeatsTotal(4);
        ride.setSeatsFree(4);
        ride.setStatus(RideStatus.OPEN);

        Ride savedRide = rideRepository.save(ride);
        test_ride_id = savedRide.getId();
        System.out.println("created ride with id " + test_ride_id);
    }

    // ==================== Verification Tests ====================

    @Test
    @Order(20)
    public void testVerifyUserForbiddenForNonAdmin() throws Exception {
        mvc.perform(put("/api/service/admin/verify")
                .param("userId", test_user_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(30)
    public void testVerifyUserAsAdmin() throws Exception {
        mvc.perform(put("/api/service/admin/verify")
                .param("userId", test_user_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }

    @Test
    @Order(40)
    public void testRejectUserForbiddenForNonAdmin() throws Exception {
        mvc.perform(put("/api/service/admin/reject")
                .param("userId", test_user_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(50)
    public void testRejectUserAsAdmin() throws Exception {
        mvc.perform(put("/api/service/admin/reject")
                .param("userId", test_user_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("DENIED"));
    }

    @Test
    @Order(60)
    public void testVerifyNonExistentUser() throws Exception {
        mvc.perform(put("/api/service/admin/verify")
                .param("userId", "nonexistent-user-id-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(70)
    public void testRejectNonExistentUser() throws Exception {
        mvc.perform(put("/api/service/admin/reject")
                .param("userId", "nonexistent-user-id-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ==================== Booking Tests ====================

    @Test
    @Order(100)
    public void testBookRideForMe() throws Exception {
        mvc.perform(put("/api/service/me/bookride")
                .param("rideId", test_ride_id)
                .param("seats", "1")
                .param("pickupLocation", "Test Pickup")
                .param("message", "Please wait for me")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(test_ride_id))
                .andExpect(jsonPath("$.riderId").value("user@test.com"))
                .andExpect(jsonPath("$.seats").value(1))
                .andExpect(jsonPath("$.pickupLocation").value("Test Pickup"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    @Order(105)
    public void testGetBookingId() throws Exception {
        var bookings = bookingRepository.findByRiderId("user@test.com");
        if (!bookings.isEmpty()) {
            test_booking_id = bookings.get(0).getId();
            System.out.println("found booking with id " + test_booking_id);
        }
    }

    @Test
    @Order(110)
    public void testBookRideTwiceFails() throws Exception {
        mvc.perform(put("/api/service/me/bookride")
                .param("rideId", test_ride_id)
                .param("seats", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(115)
    public void testDriverCannotBookOwnRide() throws Exception {
        mvc.perform(put("/api/service/me/bookride")
                .param("rideId", test_ride_id)
                .param("seats", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(120)
    public void testApproveBookingAsDriver() throws Exception {
        if (test_booking_id.isEmpty()) {
            System.out.println("Skipping - no booking id");
            return;
        }

        mvc.perform(put("/api/service/me/approvebooking")
                .param("bookingId", test_booking_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // ==================== Complete Ride Tests ====================

    @Test
    @Order(130)
    public void testCompleteRideAsDriver() throws Exception {
        Ride ride = rideRepository.findById(test_ride_id).orElse(null);
        if (ride != null) {
            ride.setStatus(RideStatus.OPEN);
            rideRepository.save(ride);
        }

        mvc.perform(put("/api/service/me/completeride")
                .param("rideId", test_ride_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @Order(135)
    public void testCompleteRideNotFound() throws Exception {
        mvc.perform(put("/api/service/me/completeride")
                .param("rideId", "nonexistent-ride-id")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(138)
    public void testCompleteRideForbiddenForNonDriver() throws Exception {
        Ride ride = new Ride();
        ride.setDriverId("other-driver@test.com");
        ride.setStartLocation("OTHER-START");
        ride.setEndLocation("OTHER-END");
        ride.setDepartureTime(LocalDateTime.now().plusDays(1));
        ride.setPricePerSeat(10.0);
        ride.setSeatsTotal(2);
        ride.setSeatsFree(2);
        ride.setStatus(RideStatus.OPEN);
        Ride savedRide = rideRepository.save(ride);

        mvc.perform(put("/api/service/me/completeride")
                .param("rideId", savedRide.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());

        rideRepository.delete(savedRide);
    }

    // ==================== Cancel Booking Tests ====================

    @Test
    @Order(140)
    public void testSetupNewBookingForCancel() throws Exception {
        Ride ride = new Ride();
        ride.setDriverId("admin@test.com");
        ride.setStartLocation("CANCEL-TEST-START");
        ride.setEndLocation("CANCEL-TEST-END");
        ride.setDepartureTime(LocalDateTime.now().plusDays(3));
        ride.setPricePerSeat(15.0);
        ride.setSeatsTotal(3);
        ride.setSeatsFree(3);
        ride.setStatus(RideStatus.OPEN);
        Ride savedRide = rideRepository.save(ride);

        Booking booking = new Booking(savedRide.getId(), "user@test.com", 1);
        booking.setStatus(BookingStatus.REQUESTED);
        Booking savedBooking = bookingRepository.save(booking);
        test_booking_id = savedBooking.getId();
        test_ride_id = savedRide.getId();
        System.out.println("created booking " + test_booking_id + " for ride " + test_ride_id);
    }

    @Test
    @Order(150)
    public void testCancelBookingAsRider() throws Exception {
        if (test_booking_id.isEmpty()) {
            System.out.println("Skipping - no booking id");
            return;
        }

        mvc.perform(put("/api/service/me/cancelbooking")
                .param("bookingId", test_booking_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    // ==================== Reject Booking Tests ====================

    @Test
    @Order(160)
    public void testRejectBooking() throws Exception {
        Ride ride = new Ride();
        ride.setDriverId("admin@test.com");
        ride.setStartLocation("REJECT-TEST-START");
        ride.setEndLocation("REJECT-TEST-END");
        ride.setDepartureTime(LocalDateTime.now().plusDays(5));
        ride.setPricePerSeat(30.0);
        ride.setSeatsTotal(2);
        ride.setSeatsFree(2);
        ride.setStatus(RideStatus.OPEN);
        Ride savedRide = rideRepository.save(ride);

        Booking booking = new Booking(savedRide.getId(), "user@test.com", 1);
        booking.setStatus(BookingStatus.REQUESTED);
        Booking savedBooking = bookingRepository.save(booking);

        mvc.perform(put("/api/service/me/rejectbooking")
                .param("bookingId", savedBooking.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        bookingRepository.delete(savedBooking);
        rideRepository.delete(savedRide);
    }

    // ==================== Cleanup ====================

    @Test
    @Order(200)
    public void testCleanupTestUser() throws Exception {
        if (!test_user_id.isEmpty()) {
            userRepository.deleteById(test_user_id);
            System.out.println("deleted test user with id " + test_user_id);
        }
        
        bookingRepository.findByRiderId("user@test.com").forEach(b -> bookingRepository.delete(b));
        
        // Delete test rides by finding all and filtering
        rideRepository.findAll().stream()
            .filter(r -> r.getDriverId() != null && r.getDriverId().equals("admin@test.com"))
            .forEach(r -> rideRepository.delete(r));
        
        if (!driver_user_id.isEmpty()) {
            userRepository.deleteById(driver_user_id);
        }
        if (!rider_user_id.isEmpty()) {
            userRepository.deleteById(rider_user_id);
        }
        
        System.out.println("Cleanup completed");
    }
}
