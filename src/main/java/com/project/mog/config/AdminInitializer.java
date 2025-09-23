package com.project.mog.config;

import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;
import com.project.mog.service.role.RoleAssignmentDto;
import com.project.mog.service.role.RolesDto;
import com.project.mog.repository.auth.AuthEntity;
import com.project.mog.repository.auth.AuthRepository;
import com.project.mog.repository.role.RoleAssignmentEntity;
import com.project.mog.repository.role.RolesEntity;
import com.project.mog.repository.role.RolesRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UsersRepository usersRepository;
    private final AuthRepository authRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        
            // admin 계정이 이미 존재하는지 확인
            var existingAdmin = usersRepository.findByEmail("admin@mog.com");
            
            if (existingAdmin.isEmpty()) {
                // admin 계정 생성 (SUPER_ADMIN 역할)
                UsersEntity adminUser = UsersEntity.builder()
                        .usersName("관리자")
                        .nickName("admin")
                        .email("admin@mog.com")
                        .phoneNum("01000000000")
                        .build();
                RolesEntity role = RolesEntity.builder().roleName("SUPER_ADMIN").roleDescription("SUPER_ADMIN").build();
                RoleAssignmentEntity roleAssignment = RoleAssignmentEntity.builder().role(role).isActive(1L).assignedAt(LocalDateTime.now()).expiredAt(LocalDateTime.now().plusDays(30)).build();
                roleAssignment.setUser(adminUser);
                adminUser.setRoleAssignment(roleAssignment);
                // AuthEntity 생성 (비밀번호 설정 - 평문)
                AuthEntity adminAuth = new AuthEntity();
                adminAuth.setPassword("admin1234");

                // 연관관계 설정
                adminUser.setAuth(adminAuth);
                adminAuth.setUser(adminUser);
                // AuthEntity를 먼저 저장
                authRepository.save(adminAuth);
                
                
                log.info("Super Admin 계정이 생성되었습니다: admin@mog.com / admin1234");

                // //admin 생성 이후 역할 생성
                RolesEntity userRole = RolesEntity.builder().roleName("USER").roleDescription("USER").build();
                RolesEntity sellerRole = RolesEntity.builder().roleName("SELLER").roleDescription("SELLER").build();
                rolesRepository.save(userRole);
                rolesRepository.save(sellerRole);
                log.info("역할이 정상적으로 생성되었습니다.");

            } else {
                // 기존 admin 계정을 SUPER_ADMIN으로 업데이트
                UsersEntity existingUser = existingAdmin.get();
                if (!"SUPER_ADMIN".equals(existingUser.getRoleAssignment().getRole().getRoleName())) {
                    existingUser.setRoleAssignment(RoleAssignmentDto.builder().rolesDto(RolesDto.builder().roleName("SUPER_ADMIN").build()).build().toEntity());
                    usersRepository.save(existingUser);
                    log.info("기존 admin 계정이 SUPER_ADMIN으로 업데이트되었습니다.");
                }
            }
        
    }
}
