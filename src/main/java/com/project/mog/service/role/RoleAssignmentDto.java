package com.project.mog.service.role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.project.mog.repository.role.RoleAssignmentEntity;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(hidden = true)
    private Long assignmentId;
    @Schema(hidden = true)
    private Long isActive;
    @Schema(hidden = true)
    private LocalDateTime assignedAt;
    @Schema(hidden = true)
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

    public static List<RoleAssignmentDto> toDto(List<RoleAssignmentEntity> rEntity) {
        return rEntity.stream().map(RoleAssignmentDto::toDto).collect(Collectors.toList());
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