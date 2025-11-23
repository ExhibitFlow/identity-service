package com.exhibitflow.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequest {

    @NotBlank(message = "Permission name is required")
    @Size(min = 3, max = 100, message = "Permission name must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-z][a-z0-9_-]*:[a-z][a-z0-9_-]*$", message = "Permission name must follow pattern: resource:action (e.g., user:read)")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotBlank(message = "Resource is required")
    @Size(max = 100, message = "Resource must not exceed 100 characters")
    @Pattern(regexp = "^[a-z][a-z0-9_-]*$", message = "Resource must contain only lowercase letters, numbers, underscores, and hyphens")
    private String resource;

    @NotBlank(message = "Action is required")
    @Size(max = 50, message = "Action must not exceed 50 characters")
    @Pattern(regexp = "^[a-z][a-z0-9_-]*$", message = "Action must contain only lowercase letters, numbers, underscores, and hyphens")
    private String action;
}
