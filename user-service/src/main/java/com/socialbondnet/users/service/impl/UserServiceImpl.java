package com.socialbondnet.users.service.impl;

import com.socialbondnet.users.constants.OtpType;
import com.socialbondnet.users.model.request.SignUpRequest;
import com.socialbondnet.users.model.request.VerifyOtpRequest;
import com.socialbondnet.users.repository.UserRepository;
import com.socialbondnet.users.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

}
