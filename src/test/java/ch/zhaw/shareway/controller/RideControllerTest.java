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

import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.UserRole;
import ch.zhaw.shareway.model.Vehicle;
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.repository.UserRepository;
import ch.zhaw.shareway.repository.VehicleRepository;
import ch.zhaw.shareway.security.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class RideControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    UserRepository userRepository;

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_START = "TEST-START-abc...xyz";
    private static final String TEST_END = "TEST-END-abc...xyz";
    private static String vehicle_id = "";
    private static String driver_id = "";
    private static String ride_id = "";
    private static String admin_vehicle_id = "";

    // ==================== Setup ====================

    @Test
    @Order(5)
    public void testSetupAdminVehicle() throws Exception {
        // Create admin user if not exists
        userRepository.findByEmail("admin@test.com").ifPresent(u -> userRepository.delete(u));
        User admin = new User("admin-auth0", "admin@test.com", "Admin User", UserRole.ADMIN);
        admin.setFirstName("Admin");
        admin.setLastName("User");
        userRepository.save(admin);

        // Create a vehicle for admin@test.com using constructor
        Vehicle adminVehicle = new Vehicle("admin@test.com", "ADMIN-MAKE", "ADMIN-MODEL", 5, "ADMIN-PLATE-456");
        Vehicle savedAdmin = vehicleRepository.save(adminVehicle);
        admin_vehicle_id = savedAdmin.getId();
        System.out.println("created admin vehicle with id " + admin_vehicle_id);
    }

    // ==================== Basic CRUD Tests ====================

    @Test
    @Order(10)
    public void testCreateRide() throws Exception {
        // get valid vehicle
        var vehicle = vehicleRepository.findAll().get(0);
        vehicle_id = vehicle.getId();
        driver_id = vehicle.getOwnerId();
        System.out.println("using vehicle id " + vehicle_id);

        String jsonBody = """
            {
                "driverId": "%s",
                "vehicleId": "%s",
                "startLocation": "%s",
                "endLocation": "%s",
                "departureTime": "2025-12-15T14:30:00",
                "pricePerSeat": 25.0,
                "seatsTotal": 3
            }
            """.formatted(driver_id, vehicle_id, TEST_START, TEST_END);

        var result = mvc.perform(post("/api/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        ride_id = jsonNode.get("id").asText();
        System.out.println("created ride with id " + ride_id);
    }

    @Test
    @Order(15)
    public void testCreateRideWithAllFields() throws Exception {
        String jsonBody = """
            {
                "driverId": "admin@test.com",
                "vehicleId": "%s",
                "startLocation": "EXTENDED-TEST-START",
                "endLocation": "EXTENDED-TEST-END",
                "departureTime": "2025-12-20T10:00:00",
                "pricePerSeat": 35.0,
                "seatsTotal": 4,
                "description": "Test ride with description",
                "durationMinutes": 60,
                "distanceKm": 45.5,
                "routeRadiusKm": 10.0
            }
            """.formatted(admin_vehicle_id);

        mvc.perform(post("/api/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Test ride with description"))
                .andExpect(jsonPath("$.durationMinutes").value(60))
                .andExpect(jsonPath("$.distanceKm").value(45.5))
                .andExpect(jsonPath("$.routeRadiusKm").value(10.0));
    }

    @Test
    @Order(16)
    public void testCreateRideWithInvalidVehicle() throws Exception {
        String jsonBody = """
            {
                "driverId": "admin@test.com",
                "vehicleId": "nonexistent-vehicle-id",
                "startLocation": "TEST-START",
                "endLocation": "TEST-END",
                "departureTime": "2025-12-20T10:00:00",
                "pricePerSeat": 20.0,
                "seatsTotal": 3
            }
            """;

        mvc.perform(post("/api/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(17)
    public void testCreateRideWithWrongDriverVehicle() throws Exception {
        String jsonBody = """
            {
                "driverId": "wrong-driver@test.com",
                "vehicleId": "%s",
                "startLocation": "TEST-START",
                "endLocation": "TEST-END",
                "departureTime": "2025-12-20T10:00:00",
                "pricePerSeat": 20.0,
                "seatsTotal": 3
            }
            """.formatted(admin_vehicle_id);

        mvc.perform(post("/api/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(20)
    public void testGetRide() throws Exception {
        mvc.perform(get("/api/rides/" + ride_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startLocation").value(TEST_START))
                .andExpect(jsonPath("$.endLocation").value(TEST_END))
                .andExpect(jsonPath("$.vehicleId").value(vehicle_id));
    }

    // ==================== Filter & Pagination Tests ====================

    @Test
    @Order(25)
    public void testGetAllRidesNoFilter() throws Exception {
        mvc.perform(get("/api/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(26)
    public void testGetAllRidesWithStatusFilter() throws Exception {
        mvc.perform(get("/api/rides")
                .param("status", "OPEN")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(27)
    public void testGetAllRidesWithMaxPriceFilter() throws Exception {
        mvc.perform(get("/api/rides")
                .param("maxPrice", "50.0")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(28)
    public void testGetAllRidesWithBothFilters() throws Exception {
        mvc.perform(get("/api/rides")
                .param("status", "OPEN")
                .param("maxPrice", "100.0")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(29)
    public void testGetAllRidesWithPagination() throws Exception {
        mvc.perform(get("/api/rides")
                .param("pageNumber", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // ==================== Update Tests ====================

    @Test
    @Order(50)
    public void testUpdateRide() throws Exception {
        Ride ride = new Ride();
        ride.setDriverId("admin@test.com");
        ride.setVehicleId(admin_vehicle_id);
        ride.setStartLocation("UPDATE-TEST-START");
        ride.setEndLocation("UPDATE-TEST-END");
        ride.setDepartureTime(LocalDateTime.now().plusDays(1));
        ride.setPricePerSeat(25.0);
        ride.setSeatsTotal(3);
        ride.setSeatsFree(3);
        ride.setStatus(RideStatus.OPEN);
        Ride saved = rideRepository.save(ride);

        String updateJson = """
            {
                "departureTime": "2025-12-21T11:00:00",
                "pricePerSeat": 40.0,
                "description": "Updated description",
                "routeRadiusKm": 15.0
            }
            """;

        mvc.perform(put("/api/rides/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricePerSeat").value(40.0))
                .andExpect(jsonPath("$.description").value("Updated description"));

        rideRepository.delete(saved);
    }

    @Test
    @Order(51)
    public void testUpdateRideNotFound() throws Exception {
        String updateJson = """
            {
                "departureTime": "2025-12-21T11:00:00",
                "pricePerSeat": 40.0,
                "description": "Updated",
                "routeRadiusKm": 15.0
            }
            """;

        mvc.perform(put("/api/rides/nonexistent-ride-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(52)
    public void testUpdateRideForbiddenForNonOwner() throws Exception {
        Ride ride = new Ride();
        ride.setDriverId("admin@test.com");
        ride.setVehicleId(admin_vehicle_id);
        ride.setStartLocation("FORBIDDEN-TEST");
        ride.setEndLocation("FORBIDDEN-END");
        ride.setDepartureTime(LocalDateTime.now().plusDays(1));
        ride.setPricePerSeat(25.0);
        ride.setSeatsTotal(3);
        ride.setSeatsFree(3);
        ride.setStatus(RideStatus.OPEN);
        Ride saved = rideRepository.save(ride);

        String updateJson = """
            {
                "departureTime": "2025-12-21T11:00:00",
                "pricePerSeat": 40.0,
                "description": "Should not work",
                "routeRadiusKm": 15.0
            }
            """;

        mvc.perform(put("/api/rides/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());

        rideRepository.delete(saved);
    }

    @Test
    @Order(53)
    public void testUpdateCompletedRideFails() throws Exception {
        Ride completedRide = new Ride();
        completedRide.setDriverId("admin@test.com");
        completedRide.setVehicleId(admin_vehicle_id);
        completedRide.setStartLocation("COMPLETED-START");
        completedRide.setEndLocation("COMPLETED-END");
        completedRide.setDepartureTime(LocalDateTime.now().minusDays(1));
        completedRide.setPricePerSeat(25.0);
        completedRide.setSeatsTotal(3);
        completedRide.setSeatsFree(3);
        completedRide.setStatus(RideStatus.COMPLETED);
        Ride saved = rideRepository.save(completedRide);

        String updateJson = """
            {
                "departureTime": "2025-12-21T11:00:00",
                "pricePerSeat": 40.0,
                "description": "Should not work",
                "routeRadiusKm": 15.0
            }
            """;

        mvc.perform(put("/api/rides/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isBadRequest());

        rideRepository.delete(saved);
    }

    // ==================== Delete Tests ====================

    @Test
    @Order(60)
    public void testDeleteRideForbiddenForNonOwner() throws Exception {
        Ride otherRide = new Ride();
        otherRide.setDriverId("other-driver@test.com");
        otherRide.setVehicleId(admin_vehicle_id);
        otherRide.setStartLocation("OTHER-START");
        otherRide.setEndLocation("OTHER-END");
        otherRide.setDepartureTime(LocalDateTime.now().plusDays(1));
        otherRide.setPricePerSeat(20.0);
        otherRide.setSeatsTotal(2);
        otherRide.setSeatsFree(2);
        otherRide.setStatus(RideStatus.OPEN);
        Ride saved = rideRepository.save(otherRide);

        mvc.perform(delete("/api/rides/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());

        rideRepository.delete(saved);
    }

    @Test
    @Order(70)
    public void testDeleteRide() throws Exception {
        mvc.perform(delete("/api/rides/" + ride_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(80)
    public void testGetDeletedRide() throws Exception {
        mvc.perform(get("/api/rides/" + ride_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(85)
    public void testDeleteRideNotFound() throws Exception {
        mvc.perform(delete("/api/rides/nonexistent-ride-id")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ==================== Cleanup ====================

    @Test
    @Order(90)
    public void testCleanup() throws Exception {
        if (!admin_vehicle_id.isEmpty()) {
            vehicleRepository.deleteById(admin_vehicle_id);
        }
        // Delete test rides by finding all and filtering
        rideRepository.findAll().stream()
            .filter(r -> r.getDriverId() != null && r.getDriverId().equals("admin@test.com"))
            .forEach(r -> rideRepository.delete(r));
        userRepository.findByEmail("admin@test.com").ifPresent(u -> userRepository.delete(u));
        System.out.println("Cleanup completed");
    }
}
