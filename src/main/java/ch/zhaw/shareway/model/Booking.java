package ch.zhaw.shareway.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Document("bookings")
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class Booking {
    @Id
    private String id;
    
    @NonNull
    private String rideId;
    
    @NonNull
    private String riderId;
    
    @NonNull
    private Integer seats;
    
    private BookingStatus status = BookingStatus.REQUESTED;
    
    private String pickupLocation;
    
    private String message;
    
    // Promo-Code Felder
    private String promoCode;
    private Double discountPercent;
    private Double discountAmount;
    private Double originalPrice;
    private Double finalPrice;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
}