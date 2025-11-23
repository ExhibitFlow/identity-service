package com.exhibitflow.identity.service;

import com.exhibitflow.identity.dto.CreatePermissionRequest;
import com.exhibitflow.identity.dto.PermissionResponse;
import com.exhibitflow.identity.dto.UpdatePermissionRequest;
import com.exhibitflow.identity.exception.ResourceNotFoundException;
import com.exhibitflow.identity.exception.UserAlreadyExistsException;
import com.exhibitflow.identity.model.Permission;
import com.exhibitflow.identity.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        log.info("Creating new permission: {}", request.getName());
        
        if (permissionRepository.findByName(request.getName()).isPresent()) {
            throw new UserAlreadyExistsException("Permission already exists with name: " + request.getName());
        }

        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .resource(request.getResource())
                .action(request.getAction())
                .build();

        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission created successfully: {}", savedPermission.getName());
        return convertToPermissionResponse(savedPermission);
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> getAllPermissions(Pageable pageable) {
        log.info("Getting all permissions with pagination");
        return permissionRepository.findAll(pageable).map(this::convertToPermissionResponse);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(UUID id) {
        log.info("Getting permission by id: {}", id);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
        return convertToPermissionResponse(permission);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionByName(String name) {
        log.info("Getting permission by name: {}", name);
        Permission permission = permissionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with name: " + name));
        return convertToPermissionResponse(permission);
    }

    @Transactional
    public PermissionResponse updatePermission(UUID id, UpdatePermissionRequest request) {
        log.info("Updating permission with id: {}", id);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));

        if (request.getDescription() != null) {
            permission.setDescription(request.getDescription());
        }

        Permission updatedPermission = permissionRepository.save(permission);
        log.info("Permission updated successfully: {}", updatedPermission.getName());
        return convertToPermissionResponse(updatedPermission);
    }

    @Transactional
    public void deletePermission(UUID id) {
        log.info("Deleting permission with id: {}", id);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));

        // Check if permission is assigned to any roles
        if (!permission.getRoles().isEmpty()) {
            log.warn("Cannot delete permission {} as it is assigned to {} roles", permission.getName(), permission.getRoles().size());
            throw new IllegalArgumentException("Cannot delete permission assigned to roles. Please remove from roles first.");
        }

        permissionRepository.delete(permission);
        log.info("Permission deleted successfully: {}", permission.getName());
    }

    private PermissionResponse convertToPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .action(permission.getAction())
                .createdAt(permission.getCreatedAt())
                .build();
    }
}
