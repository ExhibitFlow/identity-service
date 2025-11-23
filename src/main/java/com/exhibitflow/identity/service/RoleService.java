package com.exhibitflow.identity.service;

import com.exhibitflow.identity.dto.*;
import com.exhibitflow.identity.exception.ResourceNotFoundException;
import com.exhibitflow.identity.exception.UserAlreadyExistsException;
import com.exhibitflow.identity.model.Permission;
import com.exhibitflow.identity.model.Role;
import com.exhibitflow.identity.repository.PermissionRepository;
import com.exhibitflow.identity.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        log.info("Creating new role: {}", request.getName());
        
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new UserAlreadyExistsException("Role already exists with name: " + request.getName());
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully: {}", savedRole.getName());
        return convertToRoleResponse(savedRole);
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        log.info("Getting all roles with pagination");
        return roleRepository.findAll(pageable).map(this::convertToRoleResponse);
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {
        log.info("Getting role by id: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return convertToRoleResponse(role);
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(String name) {
        log.info("Getting role by name: {}", name);
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
        return convertToRoleResponse(role);
    }

    @Transactional
    public RoleResponse updateRole(UUID id, UpdateRoleRequest request) {
        log.info("Updating role with id: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        // Prevent modification of ADMIN role
        if ("ADMIN".equals(role.getName())) {
            throw new IllegalArgumentException("Cannot modify ADMIN role");
        }

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        Role updatedRole = roleRepository.save(role);
        log.info("Role updated successfully: {}", updatedRole.getName());
        return convertToRoleResponse(updatedRole);
    }

    @Transactional
    public void deleteRole(UUID id) {
        log.info("Deleting role with id: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        // Prevent deletion of ADMIN role
        if ("ADMIN".equals(role.getName())) {
            throw new IllegalArgumentException("Cannot delete ADMIN role");
        }

        // Check if role has users
        if (!role.getUsers().isEmpty()) {
            log.warn("Cannot delete role {} as it has {} associated users", role.getName(), role.getUsers().size());
            throw new IllegalArgumentException("Cannot delete role with existing users. Please reassign users first.");
        }

        roleRepository.delete(role);
        log.info("Role deleted successfully: {}", role.getName());
    }

    @Transactional
    public RoleResponse assignPermissionsToRole(UUID roleId, AssignPermissionsRequest request) {
        log.info("Assigning permissions to role with id: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Set<Permission> permissions = request.getPermissionIds().stream()
                .map(permissionId -> permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId)))
                .collect(Collectors.toSet());

        // Add all permissions to the role
        permissions.forEach(role::addPermission);

        Role updatedRole = roleRepository.save(role);
        log.info("Permissions assigned successfully to role: {}", updatedRole.getName());
        return convertToRoleResponse(updatedRole);
    }

    @Transactional
    public RoleResponse removePermissionFromRole(UUID roleId, UUID permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId));

        role.removePermission(permission);

        Role updatedRole = roleRepository.save(role);
        log.info("Permission removed successfully from role: {}", updatedRole.getName());
        return convertToRoleResponse(updatedRole);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getRolePermissions(UUID roleId) {
        log.info("Getting permissions for role with id: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        return role.getPermissions().stream()
                .map(this::convertToPermissionResponse)
                .collect(Collectors.toList());
    }

    private RoleResponse convertToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissionCount(role.getPermissions().size())
                .userCount(role.getUsers().size())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
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
