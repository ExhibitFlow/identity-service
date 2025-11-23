package com.exhibitflow.identity.service;

import com.exhibitflow.identity.dto.TokenIntrospectionResponse;
import com.exhibitflow.identity.model.User;
import com.exhibitflow.identity.repository.UserRepository;
import com.exhibitflow.identity.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenIntrospectionService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public TokenIntrospectionResponse introspectToken(String token) {
        try {
            // Validate token expiration
            if (jwtUtil.isTokenExpired(token)) {
                log.debug("Token is expired");
                return TokenIntrospectionResponse.builder()
                        .active(false)
                        .build();
            }

            // Extract username and claims
            String username = jwtUtil.extractUsername(token);
            Claims claims = jwtUtil.extractAllClaims(token);

            // Fetch user details
            User user = userRepository.findByUsernameWithRolesAndPermissions(username)
                    .orElse(null);

            if (user == null || !user.getEnabled()) {
                log.debug("User not found or disabled: {}", username);
                return TokenIntrospectionResponse.builder()
                        .active(false)
                        .build();
            }

            // Extract role names
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList());

            // Extract permissions
            Set<String> permissionsSet = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> permission.getName())
                    .collect(Collectors.toSet());

            return TokenIntrospectionResponse.builder()
                    .active(true)
                    .username(username)
                    .sub(username)
                    .clientId("identity-service")
                    .exp(claims.getExpiration().getTime() / 1000)
                    .iat(claims.getIssuedAt().getTime() / 1000)
                    .roles(roles)
                    .permissions(new ArrayList<>(permissionsSet))
                    .build();

        } catch (Exception e) {
            log.error("Error introspecting token", e);
            return TokenIntrospectionResponse.builder()
                    .active(false)
                    .build();
        }
    }

    public boolean validateToken(String token) {
        try {
            if (jwtUtil.isTokenExpired(token)) {
                return false;
            }

            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByUsername(username).orElse(null);

            return user != null && user.getEnabled();
        } catch (Exception e) {
            log.error("Error validating token", e);
            return false;
        }
    }
}
