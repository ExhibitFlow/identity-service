package com.exhibitflow.identity.util;

import com.exhibitflow.identity.model.User;
import com.exhibitflow.identity.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${jwt.include-roles:true}")
    private Boolean includeRoles;

    @Value("${jwt.include-permissions:true}")
    private Boolean includePermissions;

    @Value("${jwt.include-user-details:true}")
    private Boolean includeUserDetails;

    @Value("${server.servlet.context-path:/api/v1}")
    private String issuer;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = buildClaims(userDetails.getUsername());
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    private Map<String, Object> buildClaims(String username) {
        Map<String, Object> claims = new HashMap<>();
        
        // Fetch user with roles and permissions
        Optional<User> userOptional = userRepository.findByUsernameWithRolesAndPermissions(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Add user ID and details
            if (includeUserDetails) {
                claims.put("userId", user.getId().toString());
                claims.put("username", user.getUsername());
                claims.put("email", user.getEmail());
            }
            
            // Extract role names (without ROLE_ prefix)
            List<String> roleNames = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList());
            
            // Add roles array
            if (includeRoles) {
                claims.put("roles", roleNames);
            }
            
            // Build authorities list (ROLE_ prefixed roles + permissions)
            List<String> authorities = new ArrayList<>();
            
            // Add ROLE_ prefixed roles
            roleNames.forEach(roleName -> authorities.add("ROLE_" + roleName));
            
            // Extract and add permissions
            Set<String> permissions = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> permission.getName())
                    .collect(Collectors.toSet());
            
            authorities.addAll(permissions);
            
            claims.put("authorities", authorities);
            
            // Add permissions array separately
            if (includePermissions) {
                claims.put("permissions", new ArrayList<>(permissions));
            }
        }
        
        return claims;
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Long getExpirationTime() {
        return expiration;
    }
}
