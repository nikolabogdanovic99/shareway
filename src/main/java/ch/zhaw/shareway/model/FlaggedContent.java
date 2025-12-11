package ch.zhaw.shareway.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document("flaggedContent")
public class FlaggedContent {
    
    @Id
    private String id;
    private String contentType;  // REVIEW, RIDE, BOOKING
    private String contentId;    // ID des Contents
    private String content;      // Der Text
    private String reason;       // KI-Begründung
    private String userId;       // Wer hat es geschrieben
    private LocalDateTime createdAt = LocalDateTime.now();
}