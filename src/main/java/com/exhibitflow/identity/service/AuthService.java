package com.exhibitflow.identity.service;

import com.exhibitflow.identity.dto.*;
import com.exhibitflow.identity.exception.InvalidTokenException;
import com.exhibitflow.identity.exception.ResourceNotFoundException;
import com.exhibitflow.identity.exception.UserAlreadyExistsException;
import com.exhibitflow.identity.model.RefreshToken;
import com.exhibitflow.identity.model.Role;
import com.exhibitflow.identity.model.User;
import com.exhibitflow.identity.repository.RefreshTokenRepository;
import com.exhibitflow.identity.repository.RoleRepository;
import com.exhibitflow.identity.repository.UserRepository;
import com.exhibitflow.identity.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    // private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public UserDto register(UserRegistrationDto registrationDto) {
        log.info("Registering new user: {}", registrationDto.getUsername());

        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + registrationDto.getUsername());
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + registrationDto.getEmail());
        }

        User user = User.builder()
                .username(registrationDto.getUsername())
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        // Assign default VIEWER role
        Role viewerRole = roleRepository.findByName("MANAGER")
                .orElseThrow(() -> new ResourceNotFoundException("Default manager role not found"));
        user.addRole(viewerRole);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        // publishUserEvent("USER_REGISTERED", savedUser);

        return convertToUserDto(savedUser);
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        log.info("User login attempt: {}", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Save refresh token
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        saveRefreshToken(user, refreshToken);
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in successfully: {}", loginRequest.getUsername());

        // publishAuthEvent("USER_LOGIN", user);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .build();
    }

    @Transactional
    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {
        log.info("Refreshing token");

        String token = request.getRefreshToken();
        
        if (jwtUtil.isTokenExpired(token)) {
            throw new InvalidTokenException("Refresh token is expired");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.getRevoked() || refreshToken.isExpired()) {
            throw new InvalidTokenException("Refresh token is revoked or expired");
        }

        String username = jwtUtil.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtUtil.generateToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Revoke old refresh token and save new one
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        
        User user = refreshToken.getUser();
        saveRefreshToken(user, newRefreshToken);

        log.info("Token refreshed successfully for user: {}", username);

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .build();
    }

    @Transactional
    public void logout(String username) {
        log.info("User logout: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        refreshTokenRepository.deleteByUser(user);
        
        // publishAuthEvent("USER_LOGOUT", user);

        log.info("User logged out successfully: {}", username);
    }

    private void saveRefreshToken(User user, String token) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtUtil.getExpirationTime() / 1000);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();
        
        refreshTokenRepository.save(refreshToken);
    }

    // private void publishUserEvent(String eventType, User user) {
    //     try {
    //         Map<String, Object> event = new HashMap<>();
    //         event.put("eventType", eventType);
    //         event.put("userId", user.getId().toString());
    //         event.put("username", user.getUsername());
    //         event.put("email", user.getEmail());
    //         event.put("timestamp", LocalDateTime.now().toString());
    //
    //         kafkaTemplate.send("user-events", event);
    //         log.debug("Published user event: {} for user: {}", eventType, user.getUsername());
    //     } catch (Exception e) {
    //         log.error("Failed to publish user event: {}", eventType, e);
    //     }
    // }

    // private void publishAuthEvent(String eventType, User user) {
    //     try {
    //         Map<String, Object> event = new HashMap<>();
    //         event.put("eventType", eventType);
    //         event.put("userId", user.getId().toString());
    //         event.put("username", user.getUsername());
    //         event.put("timestamp", LocalDateTime.now().toString());
    //
    //         kafkaTemplate.send("auth-events", event);
    //         log.debug("Published auth event: {} for user: {}", eventType, user.getUsername());
    //     } catch (Exception e) {
    //         log.error("Failed to publish auth event: {}", eventType, e);
    //     }
    // }

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
                        .map(permission -> permission.getName())
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
