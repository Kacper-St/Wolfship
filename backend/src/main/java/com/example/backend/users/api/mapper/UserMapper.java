package com.example.backend.users.api.mapper;

import com.example.backend.users.api.dto.AuthResponse;
import com.example.backend.users.api.dto.RegisterRequest;
import com.example.backend.users.api.dto.UserRequest;
import com.example.backend.users.api.dto.UserResponse;
import com.example.backend.users.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRequest request);

    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    UserResponse toResponse(User user);

    List<UserResponse> toResponse(List<User> users);

    @Mapping(target = "id",  ignore = true)
    @Mapping(target = "forcePasswordChange",  ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateUser(UserRequest userRequest, @MappingTarget User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "forcePasswordChange", ignore = true)
    @Mapping(target = "pesel", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "forcePasswordChange", source = "forcePasswordChange")
    AuthResponse toAuthResponse(User user);

    default Set<String> mapRoles(User user) {
        if (user.getRoles() == null) return null;
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(java.util.stream.Collectors.toSet());
    }
}
