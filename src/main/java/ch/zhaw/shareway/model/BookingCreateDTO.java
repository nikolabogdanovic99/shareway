package ch.zhaw.shareway.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class BookingCreateDTO {
    private String rideId;
    private String riderId; // Später aus JWT (Auth0)
    private Integer seats;
    private String pickupLocation;  // Wo soll abgeholt werden?
    private String message; // Nachricht an den Fahrer
}