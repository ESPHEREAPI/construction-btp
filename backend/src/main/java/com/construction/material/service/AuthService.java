package com.construction.material.service;

import com.construction.material.dto.request.ChangePasswordRequest;
import com.construction.material.dto.request.LoginRequest;
import com.construction.material.dto.request.SignupRequest;
import com.construction.material.dto.response.JwtResponse;
import com.construction.material.dto.response.MessageResponse;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
    MessageResponse registerUser(SignupRequest signupRequest);
    MessageResponse changePassword(String username, ChangePasswordRequest request);
}
