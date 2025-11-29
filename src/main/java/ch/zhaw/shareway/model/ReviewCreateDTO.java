package ch.zhaw.shareway.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReviewCreateDTO {
    private String rideId;
    private String fromUserId; // Später aus JWT (Auth0)
    private String toUserId;
    private Integer rating; // 1-5
    private String comment;
}