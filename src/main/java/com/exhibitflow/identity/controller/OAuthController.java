package com.exhibitflow.identity.controller;

import com.exhibitflow.identity.dto.TokenIntrospectionResponse;
import com.exhibitflow.identity.service.TokenIntrospectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Tag(name = "OAuth Token Management", description = "OAuth2 token introspection and validation endpoints")
public class OAuthController {

    private final TokenIntrospectionService tokenIntrospectionService;

    @PostMapping("/introspect")
    @Operation(
        summary = "Token introspection (RFC 7662)",
        description = "Introspect an access token and return its metadata including user, roles, and permissions"
    )
    public ResponseEntity<TokenIntrospectionResponse> introspectToken(@RequestParam String token) {
        TokenIntrospectionResponse response = tokenIntrospectionService.introspectToken(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @Operation(
        summary = "Simple token validation",
        description = "Validates if a token is active and associated with an enabled user"
    )
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestParam String token) {
        boolean isValid = tokenIntrospectionService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}
