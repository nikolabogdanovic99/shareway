package ch.zhaw.shareway.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.BookingStatus;
import ch.zhaw.shareway.model.Review;
import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.UserRole;
import ch.zhaw.shareway.repository.BookingRepository;
import ch.zhaw.shareway.repository.ReviewRepository;
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.repository.UserRepository;
import ch.zhaw.shareway.security.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_COMMENT = "TEST-COMMENT-abc...xyz";
    private static String test_ride_id = "";
    private static String review_id = "";

    // ==================== Setup ====================

    @Test
    @Order(5)
    public void testSetupUsers() throws Exception {
        // Create driver user
        userRepository.findByEmail("admin@test.com").ifPresent(u -> userRepository.delete(u));
        User driver = new User("admin-auth0", "admin@test.com", "Admin Driver", UserRole.ADMIN);
        driver.setFirstName("Admin");
        driver.setLastName("Driver");
        userRepository.save(driver);

        // Create rider user
        userRepository.findByEmail("user@test.com").ifPresent(u -> userRepository.delete(u));
        User rider = new User("user-auth0", "user@test.com", "Test Rider", UserRole.USER);
        rider.setFirstName("Test");
        rider.setLastName("Rider");
        userRepository.save(rider);
    }

    @Test
    @Order(6)
    public void testSetupCompletedRide() throws Exception {
        // Create a COMPLETED ride (required for reviews)
        Ride ride = new Ride();
        ride.setDriverId("admin@test.com");
        ride.setVehicleId("test-vehicle");
        ride.setStartLocation("REVIEW-TEST-START");
        ride.setEndLocation("REVIEW-TEST-END");
        ride.setDepartureTime(LocalDateTime.now().minusDays(1));
        ride.setPricePerSeat(20.0);
        ride.setSeatsTotal(4);
        ride.setSeatsFree(3);
        ride.setStatus(RideStatus.COMPLETED);
        Ride savedRide = rideRepository.save(ride);
        test_ride_id = savedRide.getId();
        System.out.println("created completed ride with id " + test_ride_id);
    }

    @Test
    @Order(7)
    public void testSetupApprovedBooking() throws Exception {
        // Create an APPROVED booking for user@test.com
        Booking booking = new Booking(test_ride_id, "user@test.com", 1);
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
        System.out.println("created approved booking for ride " + test_ride_id);
    }

    // ==================== createReview Tests ====================

    @Test
    @Order(10)
    public void testCreateReviewInvalidRating() throws Exception {
        String jsonBody = """
            {
                "rideId": "%s",
                "rating": 6,
                "comment": "Invalid rating"
            }
            """.formatted(test_ride_id);

        mvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    public void testCreateReviewRatingTooLow() throws Exception {
        String jsonBody = """
            {
                "rideId": "%s",
                "rating": 0,
                "comment": "Invalid rating"
            }
            """.formatted(test_ride_id);

        mvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(12)
    public void testCreateReviewRideNotFound() throws Exception {
        String jsonBody = """
            {
                "rideId": "nonexistent-ride-id",
                "rating": 5,
                "comment": "Great ride"
            }
            """;

        mvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(13)
    public void testCreateReviewRideNotCompleted() throws Exception {
        // Create an OPEN ride
        Ride openRide = new Ride();
        openRide.setDriverId("admin@test.com");
        openRide.setVehicleId("test-vehicle");
        openRide.setStartLocation("OPEN-RIDE");
        openRide.setEndLocation("OPEN-END");
        openRide.setDepartureTime(LocalDateTime.now().plusDays(1));
        openRide.setPricePerSeat(20.0);
        openRide.setSeatsTotal(4);
        openRide.setSeatsFree(4);
        openRide.setStatus(RideStatus.OPEN);
        Ride saved = rideRepository.save(openRide);

        String jsonBody = """
            {
                "rideId": "%s",
                "rating": 5,
                "comment": "Great ride"
            }
            """.formatted(saved.getId());

        mvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isBadRequest());

        rideRepository.delete(saved);
    }

    @Test
    @Order(14)
    public void testCreateReviewNoBooking() throws Exception {
        // Create a completed ride without booking for user
        Ride ride = new Ride();
        ride.setDriverId("other-driver@test.com");
        ride.setVehicleId("test-vehicle");
        ride.setStartLocation("NO-BOOKING-START");
        ride.setEndLocation("NO-BOOKING-END");
        ride.setDepartureTime(LocalDateTime.now().minusDays(2));
        ride.setPricePerSeat(15.0);
        ride.setSeatsTotal(3);
        ride.setSeatsFree(3);
        ride.setStatus(RideStatus.COMPLETED);
        Ride saved = rideRepository.save(ride);

        String jsonBody = """
            {
                "rideId": "%s",
                "rating": 5,
                "comment": "Great ride"
            }
            """.formatted(saved.getId());

        mvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());

        rideRepository.delete(saved);
    }

    @Test
    @Order(20)
    public void testCreateReviewSuccess() throws Exception {
        String jsonBody = """
            {
                "rideId": "%s",
                "rating": 5,
                "comment": "%s"
            }
            """.formatted(test_ride_id, TEST_COMMENT);

        var result = mvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value(TEST_COMMENT))
                .andExpect(jsonPath("$.fromUserId").value("user@test.com"))
                .andExpect(jsonPath("$.toUserId").value("admin@test.com"))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        review_id = jsonNode.get("id").asText();
        System.out.println("created review with id " + review_id);
    }

    @Test
    @Order(25)
    public void testCreateReviewDuplicate() throws Exception {
        // Same user trying to review same ride again
        String jsonBody = """
            {
                "rideId": "%s",
                "rating": 4,
                "comment": "Another review"
            }
            """.formatted(test_ride_id);

        mvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    // ==================== GET Tests ====================

    @Test
    @Order(30)
    public void testGetReviewsByRide() throws Exception {
        mvc.perform(get("/api/reviews/ride/" + test_ride_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].comment").value(TEST_COMMENT));
    }

    @Test
    @Order(35)
    public void testGetReviewsByUser() throws Exception {
        mvc.perform(get("/api/reviews/user/admin@test.com")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(36)
    public void testGetReviewsForNonExistentRide() throws Exception {
        mvc.perform(get("/api/reviews/ride/nonexistent-ride-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ==================== updateReview Tests ====================

    @Test
    @Order(40)
    public void testUpdateReviewNotFound() throws Exception {
        String jsonBody = """
            {
                "rideId": "%s",
                "rating": 4,
                "comment": "Updated comment"
            }
            """.formatted(test_ride_id);

        mvc.perform(put("/api/reviews/nonexistent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(42)
    public void testUpdateReviewForbidden() throws Exception {
        // Create a review from another user
        Review otherReview = new Review(test_ride_id, "other@test.com", "admin@test.com", 3, "Other review");
        Review saved = reviewRepository.save(otherReview);

        String jsonBody = """
            {
                "rideId": "%s",
                "rating": 4,
                "comment": "Trying to update"
            }
            """.formatted(test_ride_id);

        mvc.perform(put("/api/reviews/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());

        reviewRepository.delete(saved);
    }

    @Test
    @Order(43)
    public void testUpdateReviewInvalidRating() throws Exception {
        String jsonBody = """
            {
                "rideId": "%s",
                "rating": 10,
                "comment": "Invalid rating"
            }
            """.formatted(test_ride_id);

        mvc.perform(put("/api/reviews/" + review_id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(45)
    public void testUpdateReviewSuccess() throws Exception {
        String jsonBody = """
            {
                "rideId": "%s",
                "rating": 4,
                "comment": "Updated comment"
            }
            """.formatted(test_ride_id);

        mvc.perform(put("/api/reviews/" + review_id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Updated comment"));
    }

    @Test
    @Order(46)
    public void testUpdateReviewAsAdmin() throws Exception {
        String jsonBody = """
            {
                "rideId": "%s",
                "rating": 3,
                "comment": "Admin updated"
            }
            """.formatted(test_ride_id);

        mvc.perform(put("/api/reviews/" + review_id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(3));
    }

    // ==================== deleteReview Tests ====================

    @Test
    @Order(50)
    public void testDeleteReviewForbidden() throws Exception {
        // Create a review from another user
        Review otherReview = new Review(test_ride_id, "other@test.com", "admin@test.com", 2, "To delete");
        Review saved = reviewRepository.save(otherReview);

        // User trying to delete another user's review
        mvc.perform(delete("/api/reviews/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());

        reviewRepository.delete(saved);
    }

    @Test
    @Order(55)
    public void testDeleteReviewAsOwner() throws Exception {
        // Create a review owned by user@test.com
        Review review = new Review("some-ride", "user@test.com", "driver@test.com", 5, "My review");
        Review saved = reviewRepository.save(review);

        mvc.perform(delete("/api/reviews/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(60)
    public void testDeleteReviewAsAdmin() throws Exception {
        mvc.perform(delete("/api/reviews/" + review_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(65)
    public void testDeleteNonExistentReview() throws Exception {
        mvc.perform(delete("/api/reviews/nonexistent-id-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ==================== Cleanup ====================

    @Test
    @Order(90)
    public void testCleanup() throws Exception {
        // Delete test data
        bookingRepository.findByRiderId("user@test.com").forEach(b -> bookingRepository.delete(b));
        reviewRepository.findByRideId(test_ride_id).forEach(r -> reviewRepository.delete(r));
        if (!test_ride_id.isEmpty()) {
            rideRepository.deleteById(test_ride_id);
        }
        userRepository.findByEmail("admin@test.com").ifPresent(u -> userRepository.delete(u));
        userRepository.findByEmail("user@test.com").ifPresent(u -> userRepository.delete(u));
        System.out.println("Cleanup completed");
    }
}
