package ch.zhaw.shareway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.UserRole;
import ch.zhaw.shareway.model.VerificationStatus;
import ch.zhaw.shareway.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private Jwt createJwt(String email, String subject, String name, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("sub", subject);
        claims.put("name", name);
        claims.put("user_roles", roles);

        return new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "none"),
            claims
        );
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================== userHasRole Tests ====================

    @Test
    void testUserHasRoleTrue() {
        Jwt jwt = createJwt("user@test.com", "sub123", "Test User", List.of("admin", "user"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        assertTrue(userService.userHasRole("admin"));
    }

    @Test
    void testUserHasRoleFalse() {
        Jwt jwt = createJwt("user@test.com", "sub123", "Test User", List.of("user"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        assertFalse(userService.userHasRole("admin"));
    }

    @Test
    void testUserHasRoleNullRoles() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "user@test.com");
        claims.put("sub", "sub123");
        // user_roles is not set (null)

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("alg", "none"), claims);
        when(authentication.getPrincipal()).thenReturn(jwt);

        assertFalse(userService.userHasRole("admin"));
    }

    // ==================== getEmail Tests ====================

    @Test
    void testGetEmail() {
        Jwt jwt = createJwt("test@example.com", "sub123", "Test", List.of("user"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        assertEquals("test@example.com", userService.getEmail());
    }

    // ==================== getAuth0Id Tests ====================

    @Test
    void testGetAuth0Id() {
        Jwt jwt = createJwt("test@example.com", "auth0|12345", "Test", List.of("user"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        assertEquals("auth0|12345", userService.getAuth0Id());
    }

    // ==================== getName Tests ====================

    @Test
    void testGetNameWithName() {
        Jwt jwt = createJwt("test@example.com", "sub123", "John Doe", List.of("user"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        assertEquals("John Doe", userService.getName());
    }

    @Test
    void testGetNameWithNullName() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");
        claims.put("sub", "sub123");
        // name is not set (null)

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("alg", "none"), claims);
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Should return email when name is null
        assertEquals("test@example.com", userService.getName());
    }

    // ==================== isAdmin Tests ====================

    @Test
    void testIsAdminTrue() {
        Jwt jwt = createJwt("admin@test.com", "sub123", "Admin", List.of("admin"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        assertTrue(userService.isAdmin());
    }

    @Test
    void testIsAdminFalse() {
        Jwt jwt = createJwt("user@test.com", "sub123", "User", List.of("user"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        assertFalse(userService.isAdmin());
    }

    // ==================== canCreateRides Tests ====================

    @Test
    void testCanCreateRidesAsAdmin() {
        Jwt jwt = createJwt("admin@test.com", "sub123", "Admin", List.of("admin"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        assertTrue(userService.canCreateRides());
    }

    @Test
    void testCanCreateRidesVerifiedUser() {
        Jwt jwt = createJwt("user@test.com", "sub123", "User", List.of("user"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        User verifiedUser = new User("auth0", "user@test.com", "User", UserRole.USER);
        verifiedUser.setVerificationStatus(VerificationStatus.VERIFIED);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(verifiedUser));

        assertTrue(userService.canCreateRides());
    }

    @Test
    void testCanCreateRidesUnverifiedUser() {
        Jwt jwt = createJwt("user@test.com", "sub123", "User", List.of("user"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        User unverifiedUser = new User("auth0", "user@test.com", "User", UserRole.USER);
        unverifiedUser.setVerificationStatus(VerificationStatus.UNVERIFIED);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(unverifiedUser));

        assertFalse(userService.canCreateRides());
    }

    @Test
    void testCanCreateRidesUserNotFound() {
        Jwt jwt = createJwt("user@test.com", "sub123", "User", List.of("user"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

        assertFalse(userService.canCreateRides());
    }

    @Test
    void testCanCreateRidesPendingUser() {
        Jwt jwt = createJwt("user@test.com", "sub123", "User", List.of("user"));
        when(authentication.getPrincipal()).thenReturn(jwt);

        User pendingUser = new User("auth0", "user@test.com", "User", UserRole.USER);
        pendingUser.setVerificationStatus(VerificationStatus.PENDING);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(pendingUser));

        assertFalse(userService.canCreateRides());
    }
}
