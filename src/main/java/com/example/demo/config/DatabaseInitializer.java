package com.example.demo.config;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.domain.CleanerProfile;
import com.example.demo.domain.Role;
import com.example.demo.domain.Service;
import com.example.demo.domain.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.CleanerProfileRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;

@org.springframework.stereotype.Service
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final CleanerProfileRepository cleanerProfileRepository;

    public DatabaseInitializer(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, ServiceRepository serviceRepository,
            BookingRepository bookingRepository, CleanerProfileRepository cleanerProfileRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.serviceRepository = serviceRepository;
        this.bookingRepository = bookingRepository;
        this.cleanerProfileRepository = cleanerProfileRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");

        long countUser = this.userRepository.count();
        long countRole = this.roleRepository.count();
        long countService = this.serviceRepository.count();
        long countBooking = this.bookingRepository.count();

        if (countRole == 0) {
            Role adminRole = new Role();
            adminRole.setName("SUPER_ADMIN");
            adminRole.setDescription("Admin full quyền");
            adminRole.setActive(true);
            this.roleRepository.save(adminRole);

            Role customerRule = new Role();
            customerRule.setName("CUSTOMER");
            customerRule.setDescription("Role for normal user");
            customerRule.setActive(true);
            this.roleRepository.save(customerRule);

            Role cleanerRole = new Role();
            cleanerRole.setName("CLEANER");
            cleanerRole.setDescription("Role for normal cleaner");
            cleanerRole.setActive(true);
            this.roleRepository.save(cleanerRole);
        }

        if (countUser == 0) {
            User adminUser = new User();
            adminUser.setName("Quản trị viên");
            adminUser.setUsername("admin@gmail.com");
            adminUser.setEmail("admin@gmail.com");
            adminUser.setPhone("0868686868");
            adminUser.setGender(true);
            adminUser.setPassword(this.passwordEncoder.encode("mamascleanfa25"));

            Optional<Role> roleOptional = this.roleRepository.findByName("SUPER_ADMIN");
            Role adminRole = roleOptional.isPresent() ? roleOptional.get() : null;
            if (adminRole != null) {
                adminUser.setRole(adminRole);
            }

            User customer = new User();
            customer.setName("Nguyễn Lương Gia Phát");
            customer.setUsername("customer@gmail.com");
            customer.setEmail("customer@gmail.com");
            customer.setPhone("0888866666");
            customer.setGender(true);
            customer.setPassword(this.passwordEncoder.encode("mamascleanfa25"));
            roleOptional = this.roleRepository.findByName("CUSTOMER");
            Role customerRole = roleOptional.isPresent() ? roleOptional.get() : null;
            if (customerRole != null) {
                customer.setRole(customerRole);
            }

            User cleaner = new User();
            cleaner.setName("Nguyễn Công Chiến");
            cleaner.setUsername("cleaner@gmail.com");
            cleaner.setEmail("cleaner@gmail.com");
            cleaner.setPhone("0666688888");
            cleaner.setGender(true);
            cleaner.setPassword(this.passwordEncoder.encode("mamascleanfa25"));
            roleOptional = this.roleRepository.findByName("CLEANER");
            Role cleanerRole = roleOptional.isPresent() ? roleOptional.get() : null;
            if (cleanerRole != null) {
                cleaner.setRole(cleanerRole);
            }
            CleanerProfile cleanerProfile = new CleanerProfile();
            cleanerProfile.setAddress("Đường Ga Đông Anh, Đông Anh, Hà Nội");
            cleanerProfile.setBank("MB Bank");
            cleanerProfile.setBankNo("999999999");
            cleanerProfile.setDob(LocalDate.now());
            cleanerProfile.setIdDate(LocalDate.now());
            cleanerProfile.setIdPlace("Đường Ga Đông Anh, Đông Anh, Hà Nội");
            cleanerProfile.setIdNumber("001205066JQK");
            cleanerProfile.setUser(cleaner);

            User deletedUser = new User();
            deletedUser.setName("UNKNOWN");
            deletedUser.setEmail("alternative.user@mamasclean.com");
            deletedUser.setUsername("alternative.user@mamasclean.com");
            deletedUser.setPassword("mamascleanfa25");

            this.userRepository.save(adminUser);
            this.userRepository.save(customer);
            this.userRepository.save(cleaner);
            this.userRepository.save(deletedUser);
            this.cleanerProfileRepository.save(cleanerProfile);
        }

        // if (countService == 0) {
        // }

    }

}
