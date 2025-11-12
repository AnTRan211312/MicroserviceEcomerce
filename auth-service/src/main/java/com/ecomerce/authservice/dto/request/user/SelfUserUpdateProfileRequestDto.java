package com.ecomerce.authservice.dto.request.user;

import com.ecomerce.authservice.model.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SelfUserUpdateProfileRequestDto {

    private String username;

    private LocalDate birthDate;

    private String address;
    private Gender gender;

//    private String logoUrl;
}
