package com.project.mog.service.role;

import java.util.List;

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
public class RoleResponseDto {
    private Long usersId;
    private String usersName;
    private String nickName;
    private String email;
    private String phoneNum;
    private List<RoleAssignmentDto> roleAssignments;
}
