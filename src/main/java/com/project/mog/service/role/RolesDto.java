package com.project.mog.service.role;

import com.project.mog.repository.role.RolesEntity;

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
public class RolesDto {
    private Long roleId;
    private String roleName;
    private String roleDescription;

    public RolesEntity toEntity() {
        return RolesEntity.builder()
                .roleName(roleName)
                .roleDescription(roleDescription)
                .build();
    }

    public static RolesDto toDto(RolesEntity rEntity) {
        return RolesDto.builder()
                .roleId(rEntity.getRoleId())
                .roleName(rEntity.getRoleName())
                .roleDescription(rEntity.getRoleDescription())
                .build();
    }


}
