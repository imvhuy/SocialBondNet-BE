package com.socialbondnet.users.service;

import com.socialbondnet.users.entity.Users;

public interface IJwtService {
    String generateAccessToken(Users user);
    String generateRefreshToken(Users user);
    String extractEmailFromToken(String token);
    boolean isTokenValid(String token);
    boolean isRefreshTokenValid(String refreshToken);
    Users getUserFromRefreshToken(String refreshToken);
    Users getUserFromAccessToken(String accessToken);
}
