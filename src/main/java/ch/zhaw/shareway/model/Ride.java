package ch.zhaw.shareway.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;  // ← NEU!

@Document("rides")
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter  // ← NEU! Generiert alle Setter-Methoden
public class Ride {
    @Id
    private String id;

    @NonNull
    private String driverId;

    @NonNull
    private String vehicleId;

    @NonNull
    private String startLocation;

    @NonNull
    private String endLocation;

    @NonNull
    private LocalDateTime departureTime;

    @NonNull
    private Double pricePerSeat;

    @NonNull
    private Integer seatsTotal;

    @NonNull
    private Integer seatsFree;

    private RideStatus status = RideStatus.OPEN;

    private String description;

    private Double routeRadiusKm = 5.0;

    private Double distanceKm;

    private Integer durationMinutes;

    private LocalDateTime createdAt = LocalDateTime.now();
}