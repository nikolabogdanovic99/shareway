package ch.zhaw.shareway.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Document("users")
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
public class User {
    @Id
    private String id;
    
    @NonNull
    private String auth0Id; // unique - von Auth0 Login
    
    @NonNull
    private String email; // unique
    
    @NonNull
    private String name;
    
    @NonNull
    private UserRole role; // RIDER, DRIVER, ADMIN (Enum!)
    
    private String pictureUrl; // Profilbild URL (optional)
    private Double rating = 0.0; // Durchschnittsbewertung
    private Integer reviewCount = 0; // Anzahl Bewertungen
    private LocalDateTime createdAt = LocalDateTime.now();
}