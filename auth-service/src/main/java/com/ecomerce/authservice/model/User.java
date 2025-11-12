package com.ecomerce.authservice.model;

import com.ecomerce.authservice.model.common.BaseEntity;
import com.ecomerce.authservice.model.constant.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"), // Unique constraint already exists
    @Index(name = "idx_user_role_id", columnList = "role_id"), // Foreign key
    @Index(name = "idx_user_created_at", columnList = "created_at"), // For sorting/filtering
    @Index(name = "idx_user_username", columnList = "username") // For username lookup
})
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String username;
    @Column(nullable = false,unique = true)
    private String email;
    @Column(nullable = false)
    private String password;

    private LocalDate birthDate;

    private String address;
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String logoUrl;
    @ManyToOne
    @JoinColumn(name = "role_id")
    @ToString.Exclude
    private Role role;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    public User(String email,String username,String password,LocalDate birthDate,String address,Gender gender){
        this.email = email;
        this.username = username;
        this.password = password;
        this.birthDate = birthDate;
        this.address = address;
        this.gender = gender;

    }
}
