package com.exhibitflow.identity.service;

import com.exhibitflow.identity.dto.UserDto;
import com.exhibitflow.identity.exception.ResourceNotFoundException;
import com.exhibitflow.identity.model.Permission;
import com.exhibitflow.identity.model.Role;
import com.exhibitflow.identity.model.User;
import com.exhibitflow.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

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
}
