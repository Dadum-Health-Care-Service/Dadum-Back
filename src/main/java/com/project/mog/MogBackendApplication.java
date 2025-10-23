package com.project.mog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.project.mog.repository")
@EnableJpaRepositories(basePackages = "com.project.mog.repository")
@EnableScheduling
public class MogBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MogBackendApplication.class, args);
    }

}
