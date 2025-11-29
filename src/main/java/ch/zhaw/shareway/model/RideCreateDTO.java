package ch.zhaw.shareway.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class RideCreateDTO {
    private String driverId; // Später aus JWT (Auth0)
    private String vehicleId;
    private String startLocation;
    private String endLocation;
    private LocalDateTime departureTime; // ISO Format: "2025-12-15T14:30:00"
    private Double pricePerSeat;
    private Integer seatsTotal;
    private String description;
    private Double routeRadiusKm; // Optional, default 5.0
    private Double distanceKm; // Optional
    private Integer durationMinutes; // Optional
}