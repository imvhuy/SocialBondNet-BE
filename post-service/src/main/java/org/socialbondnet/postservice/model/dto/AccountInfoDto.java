package org.socialbondnet.postservice.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfoDto {
    private String id;
    private String email;
    private Boolean isActive;
    private Boolean isPrivate;
    private LocalDateTime createdAt;

}
