package ch.zhaw.shareway.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Document("reviews")
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class Review {
    @Id
    private String id;
    
    @NonNull
    private String rideId;
    
    @NonNull
    private String fromUserId;
    
    @NonNull
    private String toUserId;
    
    @NonNull
    private Integer rating;
    
    @NonNull
    private String comment;
    
    private LocalDateTime createdAt = LocalDateTime.now();
}