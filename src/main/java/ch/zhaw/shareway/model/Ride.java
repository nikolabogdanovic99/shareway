package ch.zhaw.shareway.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Document("rides")
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
public class Ride {
    @Id
    private String id;

    @NonNull
    private String driverId; // Referenz zu User (muss DRIVER sein)

    @NonNull
    private String vehicleId; // Referenz zu Vehicle

    @NonNull
    private String startLocation; // z.B. "Zürich HB"

    @NonNull
    private String endLocation; // z.B. "Bern HB"

    @NonNull
    private LocalDateTime departureTime; // Abfahrtszeit

    @NonNull
    private Double pricePerSeat; // Preis pro Platz (berechnet)

    @NonNull
    private Integer seatsTotal; // Gesamtanzahl Plätze

    @NonNull
    private Integer seatsFree; // Freie Plätze

    private RideStatus status = RideStatus.OPEN; // Status (Enum!)

    private String description; // Beschreibung (optional, später KI-moderiert)

    private Double routeRadiusKm = 5.0; // WOW-Feature! Umwegsbereitschaft

    private Double distanceKm; // Distanz in km (optional)

    private Integer durationMinutes; // Dauer in Minuten (optional)

    private LocalDateTime createdAt = LocalDateTime.now(); // Erstellungszeitpunkt
}