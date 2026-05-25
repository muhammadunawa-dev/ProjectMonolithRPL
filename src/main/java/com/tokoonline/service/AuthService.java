package com.tokoonline.service;

import com.tokoonline.dto.AuthRequest;
import com.tokoonline.dto.AuthResponse;
import com.tokoonline.dto.RegisterRequest;
import com.tokoonline.dto.UserResponse;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    UserResponse register(RegisterRequest request);
}
