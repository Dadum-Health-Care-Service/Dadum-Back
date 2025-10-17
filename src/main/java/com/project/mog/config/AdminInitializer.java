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
import java.util.List;

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
            var existingAdmin = usersRepository.findByEmailWithRole("admin@dadum.com");

            
            if (existingAdmin.isEmpty()) {
                // SUPER_ADMIN 역할 확인 또는 생성
                RolesEntity role = rolesRepository.findByRoleName("SUPER_ADMIN")
                        .orElseGet(() -> {
                            RolesEntity newRole = RolesEntity.builder()
                                    .roleName("SUPER_ADMIN")
                                    .roleDescription("SUPER_ADMIN")
                                    .build();
                            return rolesRepository.save(newRole);
                        });
                
                // admin 계정 생성
                UsersEntity adminUser = UsersEntity.builder()
                        .usersName("관리자")
                        .nickName("admin")
                        .email("admin@dadum.com")
                        .phoneNum("01000000000")
                        .build();
                
                RoleAssignmentEntity roleAssignment = RoleAssignmentEntity.builder()
                        .role(role)
                        .isActive(1L)
                        .assignedAt(LocalDateTime.now())
                        .expiredAt(LocalDateTime.now().plusDays(30))
                        .build();
                roleAssignment.setUser(adminUser);
                adminUser.getRoleAssignments().add(roleAssignment);
                
                // AuthEntity 생성 (비밀번호 설정 - 평문)
                AuthEntity adminAuth = new AuthEntity();
                adminAuth.setPassword("admin1234");

                // 연관관계 설정
                adminUser.setAuth(adminAuth);
                adminAuth.setUser(adminUser);
                // AuthEntity를 먼저 저장
                authRepository.save(adminAuth);
                
                log.info("Super Admin 계정이 생성되었습니다: admin@dadum.com / admin1234");

                // USER, SELLER 역할 확인 또는 생성
                rolesRepository.findByRoleName("USER")
                        .orElseGet(() -> {
                            RolesEntity userRole = RolesEntity.builder()
                                    .roleName("USER")
                                    .roleDescription("USER")
                                    .build();
                            return rolesRepository.save(userRole);
                        });
                
                rolesRepository.findByRoleName("SELLER")
                        .orElseGet(() -> {
                            RolesEntity sellerRole = RolesEntity.builder()
                                    .roleName("SELLER")
                                    .roleDescription("SELLER")
                                    .build();
                            return rolesRepository.save(sellerRole);
                        });
                
                log.info("역할이 정상적으로 생성되었습니다.");

            } else {
                // 기존 admin 계정을 SUPER_ADMIN으로 업데이트 (널 세이프)
                UsersEntity existingUser = existingAdmin.get();
                var assignment = existingUser.getRoleAssignments().get(0);
                if (assignment == null || assignment.getRole() == null ||
                        !"SUPER_ADMIN".equals(assignment.getRole().getRoleName())) {
                    // 역할 갱신은 운영 초기화 단계에서만 필요하므로, NPE 방지를 위해 안전하게 스킵 또는 이후 마이그레이션에서 처리
                    log.info("기존 admin 계정 존재. 역할 업데이트는 건너뜁니다(널 세이프 처리).");
                }
            }
        
    }
}
