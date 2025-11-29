package ch.zhaw.shareway.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class VehicleCreateDTO {
    private String ownerId; // Später aus JWT (Auth0)
    private String make;
    private String model;
    private Integer seats;
    private String plateHash;
    private String color;
    private Integer year;
}