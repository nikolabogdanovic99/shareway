package ch.zhaw.shareway.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Document("reviews")
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
public class Review {
    @Id
    private String id;
    
    @NonNull
    private String rideId; // Referenz zu Ride
    
    @NonNull
    private String fromUserId; // Wer bewertet (Rider oder Driver)
    
    @NonNull
    private String toUserId; // Wer wird bewertet (Rider oder Driver)
    
    @NonNull
    private Integer rating; // Bewertung 1-5 Sterne
    
    @NonNull
    private String comment; // Kommentar (später KI-moderiert!)
    
    private LocalDateTime createdAt = LocalDateTime.now(); // Erstellungszeitpunkt
}