package ch.zhaw.shareway.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

import ch.zhaw.shareway.model.FlaggedContent;
import ch.zhaw.shareway.repository.FlaggedContentRepository;
import ch.zhaw.shareway.security.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    FlaggedContentRepository flaggedContentRepository;

    private static String flagged_content_id = "";

    @Test
    @Order(10)
    public void testSetupFlaggedContent() throws Exception {
        // Create test flagged content
        FlaggedContent flagged = new FlaggedContent();
        flagged.setContentType("REVIEW");
        flagged.setContentId("test-review-id");
        flagged.setContent("TEST-FLAGGED-CONTENT-abc...xyz");
        flagged.setReason("Contains inappropriate language");
        flagged.setUserId("testuser@test.com");
        flagged.setCreatedAt(LocalDateTime.now());

        FlaggedContent saved = flaggedContentRepository.save(flagged);
        flagged_content_id = saved.getId();
        System.out.println("created flagged content with id " + flagged_content_id);
    }

    @Test
    @Order(20)
    public void testGetFlaggedContentAsAdmin() throws Exception {
        mvc.perform(get("/api/admin/flagged")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(30)
    public void testGetFlaggedContentForbiddenForNonAdmin() throws Exception {
        mvc.perform(get("/api/admin/flagged")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(40)
    public void testDeleteFlaggedContentForbiddenForNonAdmin() throws Exception {
        mvc.perform(delete("/api/admin/flagged/" + flagged_content_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(50)
    public void testDeleteFlaggedContentAsAdmin() throws Exception {
        mvc.perform(delete("/api/admin/flagged/" + flagged_content_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(60)
    public void testDeleteNonExistentFlaggedContent() throws Exception {
        // This should still return OK (deleteById doesn't throw if not found)
        mvc.perform(delete("/api/admin/flagged/nonexistent-id-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
