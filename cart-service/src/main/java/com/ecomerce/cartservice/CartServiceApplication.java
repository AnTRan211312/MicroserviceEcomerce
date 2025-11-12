package com.ecomerce.cartservice;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaRepositories
@EnableFeignClients
public class CartServiceApplication {

    @PostConstruct
    public void init() {
        // Set timezone cho toàn bộ ứng dụng
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    public static void main(String[] args) {
        loadEnvFile();
        SpringApplication.run(CartServiceApplication.class, args);
    }
    
    private static void loadEnvFile() {
        try {
            // Tìm file .env ở nhiều vị trí
            String userDir = System.getProperty("user.dir");
            String[] searchPaths = {
                "../",  // Parent directory (khi chạy từ service folder)
                "../..", // Parent of parent (khi chạy từ service/target)
                ".",    // Current directory
                userDir, // Working directory
                userDir + "/..", // Parent of working directory
                userDir + "/../.." // Parent of parent of working directory
            };
            
            Dotenv dotenv = null;
            String foundPath = null;
            
            for (String path : searchPaths) {
                try {
                    java.io.File envFile = new java.io.File(path, ".env");
                    if (envFile.exists() && envFile.isFile()) {
                        dotenv = Dotenv.configure()
                                .directory(path)
                                .ignoreIfMissing()
                                .load();
                        foundPath = envFile.getAbsolutePath();
                        System.out.println("🔍 Tìm thấy file .env tại: " + foundPath);
                        break;
                    }
                } catch (Exception e) {
                    // Continue searching
                }
            }
            
            if (dotenv == null) {
                // Thử load từ default location (parent directory)
                try {
                    dotenv = Dotenv.configure()
                            .directory("../")
                            .ignoreIfMissing()
                            .load();
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            if (dotenv == null) {
                System.out.println("⚠️ Không tìm thấy file .env, sử dụng giá trị mặc định hoặc environment variables");
                System.out.println("⚠️ Đang tìm trong: " + userDir);
                return;
            }
            
            int loadedCount = 0;
            for (var entry : dotenv.entries()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value != null && !value.trim().isEmpty()) {
                    if (System.getProperty(key) == null && System.getenv(key) == null) {
                        System.setProperty(key, value);
                        loadedCount++;
                    }
                }
            }
            
            if (foundPath != null) {
                System.out.println("✅ Đã load file .env từ: " + foundPath);
                System.out.println("✅ Đã load " + loadedCount + " biến môi trường từ file .env");
            } else {
                System.out.println("✅ Đã load " + loadedCount + " biến môi trường từ file .env");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Không tìm thấy file .env, sử dụng giá trị mặc định hoặc environment variables");
            System.out.println("⚠️ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
