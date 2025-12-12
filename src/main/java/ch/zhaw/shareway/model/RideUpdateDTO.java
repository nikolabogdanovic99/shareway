package ch.zhaw.shareway.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RideUpdateDTO {
    private String departureTime;
    private Double pricePerSeat;
    private String description;
    private Double routeRadiusKm;
}