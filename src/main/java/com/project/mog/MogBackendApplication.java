package com.project.mog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.project.mog.repository")
@EnableJpaRepositories(basePackages = "com.project.mog.repository")
public class MogBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MogBackendApplication.class, args);
    }

}
