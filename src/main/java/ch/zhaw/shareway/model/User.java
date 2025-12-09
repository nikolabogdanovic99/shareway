package ch.zhaw.shareway.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Document("users")
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
public class User {
    @Id
    private String id;

    @NonNull
    private String auth0Id;

    @NonNull
    private String email;

    @NonNull
    private String name;

    @NonNull
    private UserRole role;

    @Setter
    private String firstName;

    @Setter
    private String lastName;

    @Setter
    private String profileImage;

    @Setter
    private String licenseImageFront;

    @Setter
    private String licenseImageBack;

    @Setter
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    private String pictureUrl;

    @Setter
    private String phoneNumber;

    @Setter
    private Double rating = 0.0;

    @Setter
    private Integer reviewCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Profil ist vollständig wenn Name und Nachname vorhanden
    public boolean isProfileComplete() {
        return firstName != null && !firstName.isEmpty() &&
                lastName != null && !lastName.isEmpty();
    }

    // Kann Rides erstellen wenn verifiziert
    public boolean canCreateRides() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }
}