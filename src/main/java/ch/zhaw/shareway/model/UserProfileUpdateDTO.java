package ch.zhaw.shareway.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileUpdateDTO {
    private String firstName;
    private String lastName;
    private String profileImage;
    private String licenseImageFront;
    private String licenseImageBack;
    private String phoneNumber;
}