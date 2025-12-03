package ch.zhaw.shareway.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_START = "TEST-START-abc...xyz";
    private static final String TEST_END = "TEST-END-abc...xyz";
    private static String vehicle_id = "";
    private static String driver_id = "";
    private static String ride_id = "";

    @Test
    @Order(10)
    public void testCreateRide() throws Exception {
        // get valid vehicle
        var vehicle = vehicleRepository.findAll().get(0);
        vehicle_id = vehicle.getId();
        driver_id = vehicle.getOwnerId();
        System.out.println("using vehicle id " + vehicle_id);

        // create JSON directly
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

        // POST with ADMIN token
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

    @Test
    @Order(30)
    public void testDeleteRide() throws Exception {
        mvc.perform(delete("/api/rides/" + ride_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(40)
    public void testGetDeletedRide() throws Exception {
        mvc.perform(get("/api/rides/" + ride_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}