package com.socialbondnet.users.model.response;

import com.socialbondnet.users.model.dto.AccountInfoDto;
import com.socialbondnet.users.model.dto.ProfileInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileResponse {
    private AccountInfoDto account;
    private ProfileInfoDto profile;

}
