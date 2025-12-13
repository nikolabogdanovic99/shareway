package ch.zhaw.shareway.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import ch.zhaw.shareway.model.Review;
import ch.zhaw.shareway.repository.ReviewRepository;
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

    private static final String TEST_COMMENT = "TEST-COMMENT-abc...xyz";
    private static final String TEST_RIDE_ID = "test-ride-id-12345";
    private static final String TEST_DRIVER_ID = "driver@test.com";
    private static String review_id = "";

    @Test
    @Order(10)
    public void testSetupReview() throws Exception {
        // Create test review directly in DB
        Review review = new Review(
            TEST_RIDE_ID,
            "user@test.com",
            TEST_DRIVER_ID,
            5,
            TEST_COMMENT
        );
        Review savedReview = reviewRepository.save(review);
        review_id = savedReview.getId();
        System.out.println("created review with id " + review_id);
    }

    @Test
    @Order(20)
    public void testGetReviewsByRide() throws Exception {
        mvc.perform(get("/api/reviews/ride/" + TEST_RIDE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].comment").value(TEST_COMMENT));
    }

    @Test
    @Order(30)
    public void testGetReviewsByUser() throws Exception {
        mvc.perform(get("/api/reviews/user/" + TEST_DRIVER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(40)
    public void testGetReviewsForNonExistentRide() throws Exception {
        mvc.perform(get("/api/reviews/ride/nonexistent-ride-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Order(50)
    public void testDeleteReviewAsAdmin() throws Exception {
        // Admin kann alle Reviews löschen
        mvc.perform(delete("/api/reviews/" + review_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(60)
    public void testDeleteNonExistentReview() throws Exception {
        mvc.perform(delete("/api/reviews/nonexistent-id-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}