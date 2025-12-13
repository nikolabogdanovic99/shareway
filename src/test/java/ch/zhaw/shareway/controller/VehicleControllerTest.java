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

import ch.zhaw.shareway.security.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class VehicleControllerTest {

    @Autowired
    private MockMvc mvc;

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_MAKE = "TEST-MAKE-abc...xyz";
    private static final String TEST_MODEL = "TEST-MODEL-abc...xyz";
    private static String vehicle_id = "";

    @Test
    @Order(10)
    public void testCreateVehicle() throws Exception {
        // create JSON - ownerId muss dem eingeloggten User entsprechen für späteren DELETE
        String jsonBody = """
            {
                "ownerId": "user@test.com",
                "make": "%s",
                "model": "%s",
                "seats": 4,
                "plateHash": "ZH123456"
            }
            """.formatted(TEST_MAKE, TEST_MODEL);

        // POST with USER token
        var result = mvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value(TEST_MAKE))
                .andExpect(jsonPath("$.model").value(TEST_MODEL))
                .andExpect(jsonPath("$.seats").value(4))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        vehicle_id = jsonNode.get("id").asText();
        System.out.println("created vehicle with id " + vehicle_id);
    }

    @Test
    @Order(20)
    public void testGetAllVehicles() throws Exception {
        mvc.perform(get("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(30)
    public void testDeleteVehicleForbiddenForNonOwner() throws Exception {
        // ADMIN ist nicht der Owner (user@test.com), also FORBIDDEN
        mvc.perform(delete("/api/vehicles/" + vehicle_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(40)
    public void testDeleteVehicleAsOwner() throws Exception {
        // USER (user@test.com) ist der Owner, also OK
        mvc.perform(delete("/api/vehicles/" + vehicle_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(50)
    public void testDeleteNonExistentVehicle() throws Exception {
        mvc.perform(delete("/api/vehicles/nonexistent-id-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}