package com.exhibitflow.identity.controller;

import com.exhibitflow.identity.dto.AdminUserCreationRequest;
import com.exhibitflow.identity.dto.UserDto;
import com.exhibitflow.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Admin endpoints for creating and managing users")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user by admin", description = "Admin creates a new user with specific roles")
    public ResponseEntity<UserDto> createUserByAdmin(@Valid @RequestBody AdminUserCreationRequest request) {
        UserDto userDto = userService.createUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }
}
