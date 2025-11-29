package ch.zhaw.shareway.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class RideCreateDTO {
    private String startLocation;
    private String endLocation;
    private String dateTime;
    private Integer seatsAvailable;
}
