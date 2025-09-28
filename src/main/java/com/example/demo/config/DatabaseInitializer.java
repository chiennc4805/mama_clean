package com.example.demo.config;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.domain.Role;
import com.example.demo.domain.Service;
import com.example.demo.domain.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;

@org.springframework.stereotype.Service
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServiceRepository serviceRepository;

    public DatabaseInitializer(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, ServiceRepository serviceRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.serviceRepository = serviceRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");

        long countUser = this.userRepository.count();
        long countRole = this.roleRepository.count();
        long countService = this.serviceRepository.count();

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

        if (countService == 0) {
            Service hour_service = new Service();
            hour_service.setName("Dọn nhà theo giờ");
            hour_service.setDescription(
                    "Dịch vụ dọn nhà theo giờ chuyên nghiệp, linh hoạt theo nhu cầu của bạn. Nhân viên được đào tạo kỹ lưỡng, đảm bảo sạch sẽ, gọn gàng và tiết kiệm thời gian. Phù hợp cho nhà ở, căn hộ, văn phòng nhỏ hoặc phòng trọ.");
            this.serviceRepository.save(hour_service);

            Service period_service = new Service();
            period_service.setName("Dọn nhà định kỳ");
            period_service.setDescription(
                    "Dịch vụ dọn dẹp định kỳ chuyên nghiệp, lặp lại hằng tuần theo lịch trình cố định, phù hợp với nhu cầu của bạn. Nhân viên được đào tạo bài bản, đảm bảo không gian luôn sạch sẽ, ngăn nắp, tiết kiệm thời gian. Lý tưởng cho nhà ở, căn hộ, văn phòng nhỏ hoặc phòng trọ.");
            this.serviceRepository.save(period_service);

            Service clean_all_service = new Service();
            clean_all_service.setName("Tổng vệ sinh");
            clean_all_service.setDescription(
                    "Dịch vụ tổng vệ sinh chuyên nghiệp, thực hiện định kỳ theo lịch trình hằng tuần hoặc theo yêu cầu, đảm bảo làm sạch sâu toàn bộ không gian. Đội ngũ nhân viên được đào tạo kỹ lưỡng, sử dụng thiết bị và dung dịch vệ sinh hiện đại, mang lại không gian sạch sẽ, gọn gàng, tiết kiệm thời gian. Phù hợp cho nhà ở, căn hộ, văn phòng nhỏ hoặc phòng trọ, đáp ứng mọi nhu cầu làm sạch kỹ lưỡng.");
            this.serviceRepository.save(clean_all_service);

        }

    }

}
