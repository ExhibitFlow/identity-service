package com.exhibitflow.identity.service;

import com.exhibitflow.identity.dto.AdminUserCreationRequest;
import com.exhibitflow.identity.dto.AssignRolesRequest;
import com.exhibitflow.identity.dto.RoleResponse;
import com.exhibitflow.identity.dto.UserDto;
import com.exhibitflow.identity.exception.ResourceNotFoundException;
import com.exhibitflow.identity.exception.UserAlreadyExistsException;
import com.exhibitflow.identity.model.Permission;
import com.exhibitflow.identity.model.Role;
import com.exhibitflow.identity.model.User;
import com.exhibitflow.identity.repository.RoleRepository;
import com.exhibitflow.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        log.info("Getting user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToUserDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        log.info("Getting user by username: {}", username);
        User user = userRepository.findByUsernameWithRolesAndPermissions(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToUserDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.info("Getting all users with pagination");
        return userRepository.findAll(pageable).map(this::convertToUserDto);
    }

    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getUsername());
    }

    @Transactional
    public UserDto updateUserStatus(UUID id, boolean enabled) {
        log.info("Updating user status for id: {}, enabled: {}", id, enabled);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        log.info("User status updated successfully: {}", user.getUsername());
        return convertToUserDto(updatedUser);
    }

    @Transactional
    public UserDto createUserByAdmin(AdminUserCreationRequest request) {
        log.info("Admin creating new user: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with username: " + request.getUsername());
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(request.getEnabled())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(new HashSet<>())
                .build();

        // Assign roles if provided
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = request.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId)))
                    .collect(Collectors.toSet());
            roles.forEach(user::addRole);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully by admin: {}", savedUser.getUsername());
        return convertToUserDto(savedUser);
    }

    @Transactional
    public UserDto assignRolesToUser(UUID userId, AssignRolesRequest request) {
        log.info("Assigning roles to user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Set<Role> roles = request.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId)))
                .collect(Collectors.toSet());

        roles.forEach(user::addRole);

        User updatedUser = userRepository.save(user);
        log.info("Roles assigned successfully to user: {}", updatedUser.getUsername());
        return convertToUserDto(updatedUser);
    }

    @Transactional
    public UserDto removeRoleFromUser(UUID userId, UUID roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        // Prevent users from removing their own ADMIN role
        if ("ADMIN".equals(role.getName())) {
            throw new IllegalArgumentException("Cannot remove ADMIN role from user. This must be done by another admin.");
        }

        user.removeRole(role);

        User updatedUser = userRepository.save(user);
        log.info("Role removed successfully from user: {}", updatedUser.getUsername());
        return convertToUserDto(updatedUser);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getUserRoles(UUID userId) {
        log.info("Getting roles for user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return user.getRoles().stream()
                .map(this::convertToRoleResponse)
                .collect(Collectors.toList());
    }

    private UserDto convertToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .permissions(user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(Permission::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
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
}
