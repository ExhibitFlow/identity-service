package com.exhibitflow.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenIntrospectionResponse {

    private Boolean active;
    private String username;
    private String sub;
    private String clientId;
    private Long exp;
    private Long iat;
    private List<String> roles;
    private List<String> permissions;
}
