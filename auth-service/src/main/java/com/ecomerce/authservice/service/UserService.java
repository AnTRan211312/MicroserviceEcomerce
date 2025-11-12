package com.ecomerce.authservice.service;

import com.ecomerce.authservice.dto.request.user.SelfUpdatePasswordRequestDto;
import com.ecomerce.authservice.dto.request.user.SelfUserUpdateProfileRequestDto;
import com.ecomerce.authservice.dto.request.user.UserCreateRequestDto;
import com.ecomerce.authservice.dto.request.user.UserUpdateRequestDto;
import com.ecomerce.authservice.dto.response.user.DefaultUserResponseDto;
import com.ecomerce.authservice.dto.response.user.InternalUserResponseDto;
import com.ecomerce.authservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;


public interface UserService {
    DefaultUserResponseDto saveUser(UserCreateRequestDto userCreateRequestDto);
    Page<DefaultUserResponseDto> findAllUser(Specification<User> spec, Pageable pageable);
    DefaultUserResponseDto findUserById(Long id);
    DefaultUserResponseDto updateUser(UserUpdateRequestDto userUpdateRequestDto);
    DefaultUserResponseDto deleteUserById(Long id);
    User findByEmail(String email);
    DefaultUserResponseDto updateSelfUserProfile(SelfUserUpdateProfileRequestDto selfUserUpdateProfileRequestDto);
    DefaultUserResponseDto updateSelfUserPassword(SelfUpdatePasswordRequestDto selfUserUpdatePasswordRequestDto);
    void updateSelfUserAvatar(MultipartFile avatarFile);
    
    /**
     * Lấy thông tin user cho internal calls (inter-service communication)
     * Chỉ trả về các thông tin cần thiết: id, name, email, role
     * Không bao gồm thông tin nhạy cảm như dob, address, gender, logoUrl
     */
    InternalUserResponseDto findUserByIdForInternal(Long id);

}
