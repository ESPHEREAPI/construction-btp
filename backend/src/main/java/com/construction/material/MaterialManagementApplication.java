package com.construction.material;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MaterialManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaterialManagementApplication.class, args);
    }
}
