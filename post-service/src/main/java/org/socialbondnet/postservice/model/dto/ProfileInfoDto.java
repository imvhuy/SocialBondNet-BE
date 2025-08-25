package org.socialbondnet.postservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileInfoDto {
    private String fullName;
    private String gender;
    private String bio;
    private String website;
    private String location;
    private String avatarUrl;
    private String coverUrl;
    private LocalDateTime birthDate;
    private String visibility;
}
