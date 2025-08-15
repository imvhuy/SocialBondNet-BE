package com.socialbondnet.users.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    @Size(max = 80)  private String fullName;
    @Size(max = 10)  private String gender;
    @Size(max = 500) private String bio;

    @URL(regexp = "https?://.*", message = "website phải bắt đầu bằng http(s)://")
    @Size(max = 120) private String website;

    @Size(max = 120) private String location;

    private LocalDateTime birthDate;

    private String visibility;
}
