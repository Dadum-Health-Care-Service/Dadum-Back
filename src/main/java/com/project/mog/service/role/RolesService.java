package com.project.mog.service.role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.mog.repository.role.RoleAssignmentEntity;
import com.project.mog.repository.role.RolesEntity;
import com.project.mog.repository.role.RoleAssignmentRepository;
import com.project.mog.repository.role.RolesRepository;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;
import com.project.mog.service.users.UsersDto;

import jakarta.transaction.Transactional;

@Service
public class RolesService {
    @Autowired
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    
    public RolesService(UsersRepository usersRepository,  RolesRepository rolesRepository, RoleAssignmentRepository roleAssignmentRepository) {
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }
    @Transactional
    public List<RoleAssignmentDto> requestRoleAssignment(RoleRequestDto roleRequestDto, String authEmail) {
        UsersEntity usersEntity = usersRepository.findByEmailWithRole(authEmail).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
        
         
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
    @Transactional
    public List<RoleAssignmentDto> deleteRoleAssignment(RoleDeleteDto roleDeleteDto, Long usersId, String authEmail) {
        UsersEntity currentUser = usersRepository.findByEmailWithRole(authEmail).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
        if (!currentUser.getRoleAssignments().stream().map(RoleAssignmentEntity::getRole).map(RolesEntity::getRoleName).collect(Collectors.toList()).contains("SUPER_ADMIN")) {
            throw new IllegalArgumentException("최고 관리자 외에는 권한을 삭제할 수 없습니다");
        }

        // 삭제 대상 엔티티를 먼저 식별해 반환용 DTO 생성
        UsersEntity targetUser = usersRepository.findByIdWithRole(usersId)
            .orElseThrow(() -> new IllegalArgumentException("삭제 대상 사용자를 찾을 수 없습니다"));
        RoleAssignmentEntity toDelete = targetUser.getRoleAssignments().stream()
            .filter(r -> r.getAssignmentId().equals(roleDeleteDto.getAssignmentId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("해당 권한을 찾을 수 없습니다"));
        RoleAssignmentDto deletedDto = RoleAssignmentDto.toDto(toDelete);

        // 사용자/권한 식별자로 직접 삭제 (네이티브 쿼리)
        roleAssignmentRepository.deleteByUserIdAndAssignmentId(usersId, roleDeleteDto.getAssignmentId());

        // 삭제된 항목만 반환
        return java.util.List.of(deletedDto);
    }
    public RoleAssignmentDto permitRoleAssignment(RolePermitDto rolePermitDto, Long usersId, String authEmail) {
        UsersEntity usersEntity = usersRepository.findByEmailWithRole(authEmail).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
        if(usersEntity.getRoleAssignments().stream().map(RoleAssignmentEntity::getRole).map(RolesEntity::getRoleName).collect(Collectors.toList()).contains("SUPER_ADMIN")) {
            UsersEntity targetUsersEntity = usersRepository.findById(usersId).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
            RoleAssignmentEntity roleAssignmentEntity = targetUsersEntity.getRoleAssignments().stream().filter(r -> r.getAssignmentId()==rolePermitDto.getAssignmentId()).findFirst().orElseThrow(() -> new IllegalArgumentException("해당 권한을 찾을 수 없습니다"));
            roleAssignmentEntity.setIsActive(1L);
            roleAssignmentEntity.setExpiredAt(LocalDateTime.now().plusDays(30));
            roleAssignmentEntity.setAssignedAt(LocalDateTime.now());
            usersRepository.save(targetUsersEntity);
            return RoleAssignmentDto.toDto(roleAssignmentEntity);
        }
        else{
            throw new IllegalArgumentException("최고 관리자 외에는 권한을 허가할 수 없습니다");
        }
    }
    public List<UsersDto> getRoleAssignment(String authEmail) {
        UsersEntity usersEntity = usersRepository.findByEmailWithRole(authEmail).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
        if(usersEntity.getRoleAssignments().stream().map(RoleAssignmentEntity::getRole).map(RolesEntity::getRoleName).collect(Collectors.toList()).contains("SUPER_ADMIN")) {
            List<UsersDto> users = usersRepository.findAll().stream().map(UsersDto::toDto).collect(Collectors.toList());
            return users;
        }
        else{
            throw new IllegalArgumentException("최고 관리자 외에는 권한을 조회할 수 없습니다");
        }
    }
    public List<RoleResponseDto> getRoleRequests(String authEmail) {
        UsersEntity usersEntity = usersRepository.findByEmailWithRole(authEmail).orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다"));
        if(usersEntity.getRoleAssignments().stream().map(RoleAssignmentEntity::getRole).map(RolesEntity::getRoleName).collect(Collectors.toList()).contains("SUPER_ADMIN")) {
            List<RoleResponseDto> roleResponseDtos = usersRepository.findAll().stream().filter(user->user.getRoleAssignments().stream().map(roleAssignment -> roleAssignment.getIsActive()).collect(Collectors.toList()).contains(0L)).map(user -> RoleResponseDto.builder()
                    .usersId(user.getUsersId())
                    .usersName(user.getUsersName())
                    .nickName(user.getNickName())
                    .email(user.getEmail())
                    .phoneNum(user.getPhoneNum())
                    .roleAssignments(user.getRoleAssignments().stream().filter(roleAssignment->roleAssignment.getIsActive()==0L).map(RoleAssignmentDto::toDto).collect(Collectors.toList())).build()).collect(Collectors.toList());
            return roleResponseDtos;
        }
        else{
            throw new IllegalArgumentException("최고 관리자 외에는 권한을 조회할 수 없습니다");
        }
    }
    
}
