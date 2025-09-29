package com.project.mog.service.role;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import com.project.mog.repository.role.RoleAssignmentEntity;
import com.project.mog.repository.role.RoleAssignmentRepository;
import com.project.mog.repository.role.RolesEntity;
import com.project.mog.repository.role.RolesRepository;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;

import jakarta.transaction.Transactional;

@Service
public class RolesService {
    @Autowired
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    
    public RolesService(UsersRepository usersRepository,  RolesRepository rolesRepository,RoleAssignmentRepository roleAssignmentRepository) {
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }
    @Transactional
    public List<RoleAssignmentDto> requestRoleAssignment(RoleRequestDto roleRequestDto, String authEmail) {
        UsersEntity usersEntity = usersRepository.findByEmailWithRole(authEmail).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
        
        // roleAssignments가 null이면 초기화
        if (usersEntity.getRoleAssignments() == null) {
            usersEntity.setRoleAssignments(new ArrayList<>());
        }
        
        if (usersEntity.getRoleAssignments().stream().map(RoleAssignmentEntity::getRole).map(RolesEntity::getRoleName).collect(Collectors.toList()).contains("SUPER_ADMIN")) {
            throw new IllegalArgumentException("최고 관리자는 역할을 변경할 수 없습니다");
        }
        if (usersEntity.getRoleAssignments().stream().map(RoleAssignmentEntity::getRole).map(RolesEntity::getRoleName).collect(Collectors.toList()).contains(roleRequestDto.getRoleName())) {
            throw new IllegalArgumentException("이미 해당 역할을 가지고 있습니다");
        }
        if (!usersEntity.getRoleAssignments().stream().map(RoleAssignmentEntity::getRole).map(RolesEntity::getRoleName).collect(Collectors.toList()).contains(roleRequestDto.getRoleName())) {
            RolesEntity rolesEntity = rolesRepository.findByRoleName(roleRequestDto.getRoleName()).orElseThrow(() -> new IllegalArgumentException("해당 역할을 찾을 수 없습니다"));
            RoleAssignmentEntity roleAssignmentEntity = RoleAssignmentEntity.builder()
                    .role(rolesEntity)
                    .user(usersEntity)
                    .isActive(0L)
                    .assignedAt(null)
                    .expiredAt(null)
                    .build();
            // 기존 역할에 새 역할 추가
            usersEntity.getRoleAssignments().add(roleAssignmentEntity);
            usersRepository.save(usersEntity);
        }
        return usersEntity.getRoleAssignments().stream().map(RoleAssignmentDto::toDto).collect(Collectors.toList());
    }
    public List<RoleAssignmentDto> deleteRoleAssignment(RoleDeleteDto roleDeleteDto, Long usersId, String authEmail) {
        UsersEntity usersEntity = usersRepository.findByEmailWithRole(authEmail).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
        if(usersEntity.getRoleAssignments().stream().map(RoleAssignmentEntity::getRole).map(RolesEntity::getRoleName).collect(Collectors.toList()).contains("SUPER_ADMIN")) {
            UsersEntity targetUsersEntity = usersRepository.findById(usersId).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
            List<RoleAssignmentEntity> roleAssignmentEntity = targetUsersEntity.getRoleAssignments().stream().filter(roleAssignment->roleAssignment.getRole().getRoleName().equals(roleDeleteDto.getRoleName())).collect(Collectors.toList());
            roleAssignmentEntity.forEach(roleAssignment->roleAssignmentRepository.delete(roleAssignment));
            usersRepository.save(targetUsersEntity);
            return roleAssignmentEntity.stream().map(RoleAssignmentDto::toDto).collect(Collectors.toList());
        }
        else{
            throw new IllegalArgumentException("최고 관리자 외에는 권한을 삭제할 수 없습니다");
        }
    }
    public RoleAssignmentDto permitRoleAssignment(RolePermitDto rolePermitDto, Long usersId, String authEmail) {
        UsersEntity usersEntity = usersRepository.findByEmailWithRole(authEmail).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
        if(usersEntity.getRoleAssignments().stream().map(RoleAssignmentEntity::getRole).map(RolesEntity::getRoleName).collect(Collectors.toList()).contains("SUPER_ADMIN")) {
            UsersEntity targetUsersEntity = usersRepository.findById(usersId).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
            RoleAssignmentEntity roleAssignmentEntity = targetUsersEntity.getRoleAssignments().stream().filter(r -> r.getAssignmentId()==rolePermitDto.getAssignmentId()).findFirst().orElseThrow(() -> new IllegalArgumentException("해당 권한을 찾을 수 없습니다"));
            roleAssignmentEntity.setIsActive(1L);
            usersRepository.save(targetUsersEntity);
            return RoleAssignmentDto.toDto(roleAssignmentEntity);
        }
        else{
            throw new IllegalArgumentException("최고 관리자 외에는 권한을 허가할 수 없습니다");
        }
    }
    
}
