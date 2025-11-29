package ch.zhaw.shareway.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserCreateDTO {
    private String auth0Id;
    private String email;
    private String name;
    private UserRole role; // Verwendet das Enum!
    private String pictureUrl;
}