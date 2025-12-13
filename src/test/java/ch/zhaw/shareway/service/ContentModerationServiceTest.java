package ch.zhaw.shareway.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.openai.OpenAiChatModel;

import ch.zhaw.shareway.model.FlaggedContent;
import ch.zhaw.shareway.repository.FlaggedContentRepository;

@ExtendWith(MockitoExtension.class)
public class ContentModerationServiceTest {

    @Mock
    private OpenAiChatModel chatModel;

    @Mock
    private FlaggedContentRepository flaggedContentRepository;

    @InjectMocks
    private ContentModerationService contentModerationService;

    @Test
    void testCheckContentNullContent() {
        contentModerationService.checkContent("REVIEW", "123", null, "user@test.com");

        verify(chatModel, never()).call(anyString());
        verify(flaggedContentRepository, never()).save(any());
    }

    @Test
    void testCheckContentEmptyContent() {
        contentModerationService.checkContent("REVIEW", "123", "", "user@test.com");

        verify(chatModel, never()).call(anyString());
        verify(flaggedContentRepository, never()).save(any());
    }

    @Test
    void testCheckContentBlankContent() {
        contentModerationService.checkContent("REVIEW", "123", "   ", "user@test.com");

        verify(chatModel, never()).call(anyString());
        verify(flaggedContentRepository, never()).save(any());
    }

    @Test
    void testCheckContentOkResponse() {
        when(chatModel.call(anyString())).thenReturn("OK");

        contentModerationService.checkContent("REVIEW", "123", "Great ride!", "user@test.com");

        verify(chatModel).call(anyString());
        verify(flaggedContentRepository, never()).save(any());
    }

    @Test
    void testCheckContentFlaggedResponse() {
        when(chatModel.call(anyString())).thenReturn("FLAGGED: Contains inappropriate language");
        when(flaggedContentRepository.save(any(FlaggedContent.class))).thenAnswer(i -> i.getArgument(0));

        contentModerationService.checkContent("REVIEW", "review-123", "Bad content", "user@test.com");

        verify(chatModel).call(anyString());
        verify(flaggedContentRepository).save(any(FlaggedContent.class));
    }

    @Test
    void testCheckContentFlaggedLowercase() {
        when(chatModel.call(anyString())).thenReturn("flagged: some reason");
        when(flaggedContentRepository.save(any(FlaggedContent.class))).thenAnswer(i -> i.getArgument(0));

        contentModerationService.checkContent("COMMENT", "comment-123", "Bad content", "user@test.com");

        verify(flaggedContentRepository).save(any(FlaggedContent.class));
    }

    @Test
    void testCheckContentException() {
        when(chatModel.call(anyString())).thenThrow(new RuntimeException("API Error"));

        // Should not throw, just log error
        contentModerationService.checkContent("REVIEW", "123", "Some content", "user@test.com");

        verify(chatModel).call(anyString());
        verify(flaggedContentRepository, never()).save(any());
    }
}
