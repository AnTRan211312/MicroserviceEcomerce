package com.ecomerce.authservice.service.impl;

import com.ecomerce.authservice.dto.request.user.SelfUpdatePasswordRequestDto;
import com.ecomerce.authservice.dto.request.user.SelfUserUpdateProfileRequestDto;
import com.ecomerce.authservice.dto.request.user.UserCreateRequestDto;
import com.ecomerce.authservice.dto.request.user.UserUpdateRequestDto;
import com.ecomerce.authservice.dto.response.user.DefaultUserResponseDto;
import com.ecomerce.authservice.dto.response.user.InternalUserResponseDto;
import com.ecomerce.authservice.model.Role;
import com.ecomerce.authservice.model.User;
import com.ecomerce.authservice.repository.RoleRepository;
import com.ecomerce.authservice.repository.UserRepository;
import com.ecomerce.authservice.service.S3Service;
import com.ecomerce.authservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;
    @Override
    public DefaultUserResponseDto saveUser(UserCreateRequestDto userCreateRequestDto) {
        if(userRepository.existsByEmail(userCreateRequestDto.getEmail())) {
            throw new DataIntegrityViolationException("Email Already Exists");
        }
        User user = new User(
                userCreateRequestDto.getEmail().trim(),
                userCreateRequestDto.getUsername(),
                passwordEncoder.encode(userCreateRequestDto.getPassword()),
                userCreateRequestDto.getDateBirth(),
                userCreateRequestDto.getAddress(),
                userCreateRequestDto.getGender()
        );
        User savedUser = userRepository.saveAndFlush(user);
        return mapToResponseDto(savedUser);
    }

    @Override
    public Page<DefaultUserResponseDto> findAllUser(Specification<User> spec, Pageable pageable) {
        return userRepository
                .findAll(spec,pageable)
                .map(this::mapToResponseDto);
    }

    @Override
    public DefaultUserResponseDto findUserById(Long id) {
        return userRepository
                .findById(id)
                .map(this::mapToResponseDto)
                .orElseThrow(() ->
                        new EntityNotFoundException("không tìm thấy người dùng")
                );
    }

    @Override
    public DefaultUserResponseDto updateUser(UserUpdateRequestDto userUpdateRequestDto) {
        User user = userRepository.findById(userUpdateRequestDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("không tìm thấy người dùng"));
        user.setUsername(userUpdateRequestDto.getName());
        user.setBirthDate(userUpdateRequestDto.getDateBirth());
        user.setAddress(userUpdateRequestDto.getAddress());
        user.setGender(userUpdateRequestDto.getGender());

        if (userUpdateRequestDto.getRole() != null) {
            Long roleId = userUpdateRequestDto.getRole().getId();
            if (roleId == -1) user.setRole(null);
            else handleSetRole(user, roleId);
        }

        User savedUser = userRepository.save(user);
        return mapToResponseDto(savedUser);
    }

    @Override
    public DefaultUserResponseDto deleteUserById(Long id) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("không tìm thấy người dùng"));
        user.setRole(null);

        userRepository.delete(user);
        return mapToResponseDto(user);
    }

    @Override
    public User findByEmail(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("không tìm thấy người dùng"));
        return user;
    }

    @Override
    public DefaultUserResponseDto updateSelfUserProfile(SelfUserUpdateProfileRequestDto selfUserUpdateProfileRequestDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findByEmail(email);
        user.setUsername(selfUserUpdateProfileRequestDto.getUsername());
        user.setBirthDate(selfUserUpdateProfileRequestDto.getBirthDate());
        user.setAddress(selfUserUpdateProfileRequestDto.getAddress());
        user.setGender(selfUserUpdateProfileRequestDto.getGender());
        User savedUser = userRepository.save(user);
        return mapToResponseDto(savedUser);
    }

    @Override
    public DefaultUserResponseDto updateSelfUserPassword(SelfUpdatePasswordRequestDto selfUserUpdatePasswordRequestDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findByEmail(email);
        if(!passwordEncoder.matches(selfUserUpdatePasswordRequestDto.getOldPassword(), user.getPassword())) {
            throw new DataIntegrityViolationException("mật khẩu hiện tại không chính xác");
        }

        String encodedPassword = passwordEncoder.encode(selfUserUpdatePasswordRequestDto.getNewPassword());
        user.setPassword(encodedPassword);
        User savedUser = userRepository.saveAndFlush(user);
        return mapToResponseDto(savedUser);
    }

    @Override
    public void updateSelfUserAvatar(MultipartFile avatarFile) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findByEmail(email);
        if(avatarFile != null  && !avatarFile.isEmpty()) {
            String url = s3Service.uploadFile(avatarFile,"avatar",user.getId().toString(),true);
            user.setLogoUrl(url);
        }
        user.setUpdatedAt(Instant.now());
        userRepository.saveAndFlush(user);

    }

    @Override
    public InternalUserResponseDto findUserByIdForInternal(Long id) {
        return userRepository
                .findById(id)
                .map(this::mapToInternalResponseDto)
                .orElseThrow(() ->
                        new EntityNotFoundException("không tìm thấy người dùng")
                );
    }

    private DefaultUserResponseDto mapToResponseDto(User user) {

        DefaultUserResponseDto.RoleInformationDto role = null;
        if(user.getRole() != null) {
            role = new DefaultUserResponseDto.RoleInformationDto(
                    user.getRole().getId(),
                    user.getRole().getRoleName(),
                    user.getRole().getRoleDescription()
            );
        }
        return new DefaultUserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBirthDate(),
                user.getAddress(),
                user.getGender(),
                user.getLogoUrl(),
                role,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }


    private InternalUserResponseDto mapToInternalResponseDto(User user) {
        InternalUserResponseDto.RoleInfo role = null;
        if(user.getRole() != null) {
            role = new InternalUserResponseDto.RoleInfo(
                    user.getRole().getId(),
                    user.getRole().getRoleName(),
                    user.getRole().getRoleDescription()
            );
        }
        return new InternalUserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                role
        );
    }

    private void handleSetRole(User user, Long id) {
        Role role = roleRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chức vụ"));
        user.setRole(role);
    }



}
