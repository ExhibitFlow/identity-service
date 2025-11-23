package com.exhibitflow.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private UUID id;
    private String name;
    private String description;
    private Integer permissionCount;
    private Integer userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
