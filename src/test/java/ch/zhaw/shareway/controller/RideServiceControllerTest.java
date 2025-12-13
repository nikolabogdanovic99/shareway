package ch.zhaw.shareway.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.UserRole;
import ch.zhaw.shareway.model.VerificationStatus;
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

    private static String test_user_id = "";

    @Test
    @Order(10)
    public void testSetupTestUser() throws Exception {
        // Create test user for verification tests (using constructor)
        User user = new User(
                "test-auth0-id", // auth0Id
                "testverify@test.com", // email
                "Test User", // name
                UserRole.USER // role
        );
        user.setFirstName("Test");
        user.setLastName("User");
        user.setVerificationStatus(VerificationStatus.PENDING);

        User savedUser = userRepository.save(user);
        test_user_id = savedUser.getId();
        System.out.println("created test user with id " + test_user_id);
    }

    @Test
    @Order(20)
    public void testVerifyUserForbiddenForNonAdmin() throws Exception {
        // User ohne Admin-Rolle darf nicht verifizieren
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
        // Admin kann User verifizieren
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
        // User ohne Admin-Rolle darf nicht ablehnen
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
        // Admin kann User ablehnen
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

    @Test
    @Order(80)
    public void testCleanupTestUser() throws Exception {
        // Cleanup: Delete test user
        userRepository.deleteById(test_user_id);
        System.out.println("deleted test user with id " + test_user_id);
    }
}