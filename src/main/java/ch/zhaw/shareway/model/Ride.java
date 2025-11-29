package ch.zhaw.shareway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Document("rides")
public class Ride {
    @Id
    private String id;

    @NonNull
    private String startLocation;

    @NonNull
    private String endLocation;

    @NonNull
    private String dateTime;

    @NonNull
    private Integer seatsAvailable;
}
