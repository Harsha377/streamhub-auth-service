package com.streamhub.auth.service;

import com.streamhub.auth.dto.request.RegisterRequest;
import com.streamhub.auth.dto.response.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    RegisterResponse register(RegisterRequest request, HttpServletRequest httpServletRequest,
                              HttpServletResponse httpServletResponse);
}
