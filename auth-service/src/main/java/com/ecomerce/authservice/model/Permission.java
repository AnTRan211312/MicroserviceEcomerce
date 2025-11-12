package com.ecomerce.authservice.model;

import com.ecomerce.authservice.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_api_path", columnList = "api_path"), // For API path lookup
    @Index(name = "idx_permission_method", columnList = "method"), // For method filtering
    @Index(name = "idx_permission_module", columnList = "module"), // For module grouping
    @Index(name = "idx_permission_api_method", columnList = "api_path, method") // Composite for permission check
})
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "api_path", nullable = false)
    private String apiPath;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private String module;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;

    public Permission(String name, String apiPath, String method, String module) {
        this.name = name;
        this.apiPath = apiPath;
        this.method = method;
        this.module = module;
    }
}
