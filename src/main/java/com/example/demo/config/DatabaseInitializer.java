package com.example.demo.config;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

@Service
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");

        long countUser = this.userRepository.count();
        long countRole = this.roleRepository.count();

        if (countRole == 0) {
            Role adminRole = new Role();
            adminRole.setName("SUPER_ADMIN");
            adminRole.setDescription("Admin full quyền");
            adminRole.setActive(true);

            this.roleRepository.save(adminRole);
        }

        if (countUser == 0) {
            User adminUser = new User();
            adminUser.setName("I'm super admin");
            adminUser.setUsername("admin@gmail.com");
            adminUser.setPassword(this.passwordEncoder.encode("123456"));

            Optional<Role> roleOptional = this.roleRepository.findByName("SUPER_ADMIN");
            Role adminRole = roleOptional.isPresent() ? roleOptional.get() : null;
            if (adminRole != null) {
                adminUser.setRole(adminRole);
            }

            this.userRepository.save(adminUser);
        }

    }

}
