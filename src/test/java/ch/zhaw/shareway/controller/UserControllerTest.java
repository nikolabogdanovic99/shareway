package ch.zhaw.shareway.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    private static String test_user_id = "";

    @Test
    @Order(10)
    public void testGetAllUsers() throws Exception {
        mvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(20)
    public void testGetMyUserCreatesNewUser() throws Exception {
        // Delete existing test user if exists
        userRepository.findByEmail("user@test.com").ifPresent(u -> userRepository.delete(u));

        // GET /users/me should create user if not exists
        mvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    @Order(30)
    public void testGetMyUserReturnsExisting() throws Exception {
        // GET /users/me should return existing user
        mvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    @Order(40)
    public void testUpdateMyProfile() throws Exception {
        String jsonBody = """
            {
                "firstName": "Test",
                "lastName": "User",
                "phoneNumber": "+41791234567"
            }
            """;

        mvc.perform(put("/api/users/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.phoneNumber").value("+41791234567"));
    }

    @Test
    @Order(45)
    public void testUpdateMyProfileCreatesNewUser() throws Exception {
        // Delete admin user if exists
        userRepository.findByEmail("admin@test.com").ifPresent(u -> userRepository.delete(u));

        String jsonBody = """
            {
                "firstName": "Admin",
                "lastName": "User",
                "phoneNumber": "+41799999999"
            }
            """;

        mvc.perform(put("/api/users/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @Order(50)
    public void testRequestVerificationUserNotFound() throws Exception {
        // Delete test user to test NOT_FOUND case
        userRepository.findByEmail("user@test.com").ifPresent(u -> userRepository.delete(u));

        String jsonBody = """
            {
                "licenseImageFront": "base64imagedata",
                "licenseImageBack": "base64imagedata"
            }
            """;

        mvc.perform(put("/api/users/me/verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(55)
    public void testSetupUserForVerification() throws Exception {
        // Create user first via /users/me
        mvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(60)
    public void testRequestVerificationWithBothImages() throws Exception {
        String jsonBody = """
            {
                "licenseImageFront": "base64imagefront",
                "licenseImageBack": "base64imageback"
            }
            """;

        mvc.perform(put("/api/users/me/verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"))
                .andExpect(jsonPath("$.licenseImageFront").value("base64imagefront"))
                .andExpect(jsonPath("$.licenseImageBack").value("base64imageback"));
    }

    @Test
    @Order(65)
    public void testRequestVerificationWithMissingImage() throws Exception {
        // Only front image - should set to UNVERIFIED
        String jsonBody = """
            {
                "licenseImageFront": "base64imagefront",
                "licenseImageBack": ""
            }
            """;

        mvc.perform(put("/api/users/me/verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("UNVERIFIED"));
    }

    @Test
    @Order(70)
    public void testSetupPendingUser() throws Exception {
        // Create a pending user for getPendingUsers test
        User pendingUser = new User(
                "pending-auth0-id",
                "pending@test.com",
                "Pending User",
                UserRole.USER
        );
        pendingUser.setFirstName("Pending");
        pendingUser.setLastName("User");
        pendingUser.setVerificationStatus(VerificationStatus.PENDING);
        User saved = userRepository.save(pendingUser);
        test_user_id = saved.getId();
        System.out.println("created pending user with id " + test_user_id);
    }

    @Test
    @Order(80)
    public void testGetPendingUsersAsAdmin() throws Exception {
        mvc.perform(get("/api/users/pending")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(85)
    public void testGetPendingUsersForbiddenForNonAdmin() throws Exception {
        mvc.perform(get("/api/users/pending")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(90)
    public void testCleanup() throws Exception {
        // Cleanup test users
        if (!test_user_id.isEmpty()) {
            userRepository.deleteById(test_user_id);
            System.out.println("deleted pending user with id " + test_user_id);
        }
        userRepository.findByEmail("user@test.com").ifPresent(u -> userRepository.delete(u));
        userRepository.findByEmail("admin@test.com").ifPresent(u -> userRepository.delete(u));
    }
}
