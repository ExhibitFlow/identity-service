package com.exhibitflow.identity.controller;

import com.exhibitflow.identity.dto.*;
import com.exhibitflow.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns currently authenticated user information")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        UserDto userDto = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Returns user by their ID (Admin only)")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Returns paginated list of all users (Admin only)")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Deletes a user by ID (Admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status", description = "Enables or disables a user account (Admin only)")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam boolean enabled) {
        UserDto userDto = userService.updateUserStatus(id, enabled);
        return ResponseEntity.ok(userDto);
    }



    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign roles to user", description = "Admin assigns multiple roles to a user")
    public ResponseEntity<UserDto> assignRolesToUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRolesRequest request) {
        UserDto userDto = userService.assignRolesToUser(userId, request);
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove role from user", description = "Admin removes a single role from a user")
    public ResponseEntity<UserDto> removeRoleFromUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        UserDto userDto = userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user roles", description = "Admin retrieves all roles assigned to a user")
    public ResponseEntity<List<RoleResponse>> getUserRoles(@PathVariable UUID userId) {
        List<RoleResponse> roles = userService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }
}
