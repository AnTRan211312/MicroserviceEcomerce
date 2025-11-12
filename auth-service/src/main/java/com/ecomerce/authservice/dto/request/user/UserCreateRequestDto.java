package com.ecomerce.authservice.dto.request.user;

import com.ecomerce.authservice.model.constant.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserCreateRequestDto {

    @NotBlank(message = "Tên người dùng không được để trống")
    private String username;

    @NotBlank(message = "Email người dùng không được để trống")
    @Email(
            message = "Định dạng email không hợp lệ",
            regexp = "^[\\w\\-.]+@([\\w\\-]+\\.)+[\\w\\-]{2,4}$"
    )
    private String email;

    @NotBlank(message = "Mật khẩu người dùng không được để trống")
    private String password;

    private LocalDate dateBirth;

    private String address;

    private Gender gender;

    private RoleIdDto role;

}