package com.ecomerce.authservice.model;

import com.ecomerce.authservice.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_role_name", columnList = "role_name"), // Unique constraint already exists
    @Index(name = "idx_role_active", columnList = "active"), // Filter by active status
    @Index(name = "idx_role_created_at", columnList = "created_at") // For sorting/filtering
})
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String roleName;

    private String roleDescription;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "roles_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @ToString.Exclude
    private Set<Permission> permissions;


    @OneToMany(mappedBy = "role")
    @ToString.Exclude
    private List<User> users;

    public Role(String roleName, String roleDescription) {
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.active = true;
    }

    public Role(String roleName, String roleDescription, boolean active) {
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.active = active;
    }

}
