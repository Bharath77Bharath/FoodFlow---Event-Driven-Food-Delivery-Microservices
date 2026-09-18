package com.foodflow.userservice.Config;

import com.foodflow.userservice.Entity.User;
import com.foodflow.userservice.Entity.UserRole;
import com.foodflow.userservice.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.name}")
    private String adminName;

    @Value("${admin.phone}")
    private String adminPhone;

    @Value("${admin.address}")
    private String adminAddress;

    @Override
    public void run(String... args) {

        if (userRepo.existsByEmail(adminEmail)) {
            return;
        }

        User admin = new User();

        admin.setName(adminName);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setPhone(adminPhone);
        admin.setAddress(adminAddress);
        admin.setRole(UserRole.ADMIN);

        userRepo.save(admin);

        System.out.println("FoodFlow admin account created: " + adminEmail);
    }
}