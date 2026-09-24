package com.streamhub.auth.mapper;

import com.streamhub.auth.dto.request.RegisterRequest;
import com.streamhub.auth.dto.response.RegisterResponse;
import com.streamhub.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(RegisterRequest request);
    @Mapping(target = "message",ignore = true)
    RegisterResponse toRegisterResponse(User user);
}
