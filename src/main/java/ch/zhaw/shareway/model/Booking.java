package ch.zhaw.shareway.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Document("bookings")
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
public class Booking {
    @Id
    private String id;
    
    @NonNull
    private String rideId; // Referenz zu Ride
    
    @NonNull
    private String riderId; // Referenz zu User (Mitfahrer)
    
    @NonNull
    private Integer seats; // Anzahl gebuchte Plätze
    
    private BookingStatus status = BookingStatus.REQUESTED; // Status (Enum!)
    
    private String message; // Nachricht an Driver (optional)
    
    private LocalDateTime createdAt = LocalDateTime.now(); // Erstellungszeitpunkt
    
    private LocalDateTime updatedAt; // Letzte Änderung (optional)
}