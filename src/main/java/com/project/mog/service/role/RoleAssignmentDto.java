package com.project.mog.service.role;

import java.time.LocalDateTime;

import com.project.mog.repository.role.RoleAssignmentEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleAssignmentDto {
    private Long assignmentId;
    private Long isActive;
    private LocalDateTime assignedAt;
    private LocalDateTime expiredAt;
    private RolesDto rolesDto;

    public RoleAssignmentEntity toEntity() {
        return RoleAssignmentEntity.builder()
                .assignmentId(assignmentId)
                .isActive(isActive)
                .assignedAt(assignedAt)
                .expiredAt(expiredAt)
                .role(rolesDto.toEntity())
                .build();
    }

    public static RoleAssignmentDto toDto(RoleAssignmentEntity rEntity) {
        return RoleAssignmentDto.builder()
                .assignmentId(rEntity.getAssignmentId())
                .isActive(rEntity.getIsActive())
                .assignedAt(rEntity.getAssignedAt())
                .expiredAt(rEntity.getExpiredAt())
                .rolesDto(RolesDto.toDto(rEntity.getRole()))
                .build();
    }
    
}