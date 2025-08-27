package com.project.mog.config;

import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;
import com.project.mog.repository.auth.AuthEntity;
import com.project.mog.repository.auth.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UsersRepository usersRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            // admin 계정이 이미 존재하는지 확인
            var existingAdmin = usersRepository.findByEmail("admin@mog.com");
            
            if (existingAdmin.isEmpty()) {
                // admin 계정 생성
                UsersEntity adminUser = UsersEntity.builder()
                        .usersName("관리자")
                        .nickName("admin")
                        .email("admin@mog.com")
                        .phoneNum("01000000000")
                        .role("ADMIN")
                        .build();

                // AuthEntity 생성 (비밀번호 설정 - 평문)
                AuthEntity adminAuth = new AuthEntity();
                adminAuth.setPassword("admin1234");

                // 연관관계 설정
                adminUser.setAuth(adminAuth);
                adminAuth.setUser(adminUser);

                // AuthEntity를 먼저 저장
                authRepository.save(adminAuth);
                
                // UsersEntity 저장
                usersRepository.save(adminUser);
                
                log.info("Admin 계정이 생성되었습니다: admin@mog.com / admin1234");
            }
        } catch (Exception e) {
            log.error("Admin 계정 생성 중 오류 발생: {}", e.getMessage());
        }
    }
}
