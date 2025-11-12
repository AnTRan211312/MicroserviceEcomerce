package com.ecomerce.authservice.init;

import com.ecomerce.authservice.model.Role;
import com.ecomerce.authservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Component để khởi tạo dữ liệu Roles khi ứng dụng khởi động
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataValidator implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Đang kiểm tra và khởi tạo dữ liệu Roles...");

        // 1. Kiểm tra và tạo vai trò ADMIN
        if (!roleRepository.existsByRoleName("ADMIN")) {
            Role adminRole = new Role();
            adminRole.setRoleName("ADMIN");
            adminRole.setRoleDescription("Vai trò Quản trị viên cao nhất");
            adminRole.setActive(true); // Đặt là true để vai trò có hiệu lực

            roleRepository.save(adminRole);
            log.info("Đã tạo vai trò (Role) ADMIN thành công.");
        }

        // 2. Kiểm tra và tạo vai trò USER
        if (!roleRepository.existsByRoleName("USER")) {
            Role userRole = new Role();
            userRole.setRoleName("USER");
            userRole.setRoleDescription("Vai trò người dùng mặc định");
            userRole.setActive(true);

            roleRepository.save(userRole);
            log.info("Đã tạo vai trò (Role) USER thành công.");
        }

        log.info("Hoàn tất khởi tạo dữ liệu Roles.");
    }
}
