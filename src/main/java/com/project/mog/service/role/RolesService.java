package com.project.mog.service.role;

import org.springframework.stereotype.Service;

import com.project.mog.repository.role.RoleAssignmentEntity;
import com.project.mog.repository.role.RoleAssignmentRepository;
import com.project.mog.repository.role.RolesEntity;
import com.project.mog.repository.role.RolesRepository;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;

@Service
public class RolesService {
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    
    public RolesService(UsersRepository usersRepository,  RolesRepository rolesRepository,RoleAssignmentRepository roleAssignmentRepository) {
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }
    
    public RoleAssignmentDto requestRoleAssignment(RoleRequestDto roleRequestDto, String authEmail) {
        UsersEntity usersEntity = usersRepository.findByEmail(authEmail).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
        RoleAssignmentEntity roleAssignmentEntity = roleAssignmentRepository.findByUsersId(usersEntity.getUsersId());
        if (roleAssignmentEntity.getRole().getRoleName().equals("SUPER_ADMIN")) {
            throw new IllegalArgumentException("최고 관리자는 역할을 변경할 수 없습니다");
        }
        if (roleAssignmentEntity.getRole().getRoleName().equals(roleRequestDto.getRoleName())) {
            throw new IllegalArgumentException("이미 해당 역할을 가지고 있습니다");
        }
        if (!roleAssignmentEntity.getRole().getRoleName().equals(roleRequestDto.getRoleName())) {
            RolesEntity rolesEntity = rolesRepository.findByRoleName(roleRequestDto.getRoleName()).orElseThrow(() -> new IllegalArgumentException("해당 역할을 찾을 수 없습니다"));
            roleAssignmentEntity.setRole(rolesEntity);
            roleAssignmentEntity.setIsActive(0L);
            roleAssignmentEntity.setExpiredAt(null);
            roleAssignmentEntity.setAssignedAt(null);
            roleAssignmentRepository.save(roleAssignmentEntity);    

            return RoleAssignmentDto.toDto(roleAssignmentEntity);
        }
        
        return RoleAssignmentDto.toDto(roleAssignmentEntity);
        
        
    }
    
}
