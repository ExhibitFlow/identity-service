package com.exhibitflow.identity.controller;

import com.exhibitflow.identity.dto.CreatePermissionRequest;
import com.exhibitflow.identity.dto.PermissionResponse;
import com.exhibitflow.identity.dto.UpdatePermissionRequest;
import com.exhibitflow.identity.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "Admin endpoints for managing permissions")
@SecurityRequirement(name = "Bearer Authentication")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new permission", description = "Admin creates a new permission in the system")
    public ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        PermissionResponse response = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all permissions", description = "Admin retrieves all permissions with pagination")
    public ResponseEntity<Page<PermissionResponse>> getAllPermissions(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PermissionResponse> permissions = permissionService.getAllPermissions(pageable);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get permission by ID", description = "Admin retrieves a specific permission by its ID")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable UUID id) {
        PermissionResponse response = permissionService.getPermissionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get permission by name", description = "Admin retrieves a specific permission by its name")
    public ResponseEntity<PermissionResponse> getPermissionByName(@PathVariable String name) {
        PermissionResponse response = permissionService.getPermissionByName(name);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update permission", description = "Admin updates an existing permission")
    public ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        PermissionResponse response = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete permission", description = "Admin deletes a permission (fails if assigned to roles)")
    public ResponseEntity<Void> deletePermission(@PathVariable UUID id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
