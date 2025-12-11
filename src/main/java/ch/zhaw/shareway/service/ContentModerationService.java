package ch.zhaw.shareway.service;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.zhaw.shareway.model.FlaggedContent;
import ch.zhaw.shareway.repository.FlaggedContentRepository;

@Service
public class ContentModerationService {

    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private FlaggedContentRepository flaggedContentRepository;

    public void checkContent(String contentType, String contentId, String content, String userId) {
        if (content == null || content.isBlank()) {
            return;
        }

        String prompt = """
            Du bist Content-Moderator für eine Mitfahrplattform.
            Prüfe diesen Text auf Beleidigungen, Hassrede, Diskriminierung oder unangemessene Inhalte.
            
            Text: "%s"
            
            Antworte NUR mit:
            - OK (wenn der Text in Ordnung ist)
            - FLAGGED: [kurzer Grund] (wenn problematisch)
            """.formatted(content);

        try {
            String response = chatModel.call(prompt);

            if (response.toUpperCase().contains("FLAGGED")) {
                FlaggedContent flagged = new FlaggedContent();
                flagged.setContentType(contentType);
                flagged.setContentId(contentId);
                flagged.setContent(content);
                flagged.setReason(response.replace("FLAGGED:", "").trim());
                flagged.setUserId(userId);
                flaggedContentRepository.save(flagged);
            }
        } catch (Exception e) {
            System.err.println("Content moderation error: " + e.getMessage());
        }
    }
}