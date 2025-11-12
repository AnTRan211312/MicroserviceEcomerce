package com.ecomerce.authservice.repository;

import com.ecomerce.authservice.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface PermissionRepository  extends JpaRepository<Permission,Long>, JpaSpecificationExecutor<Permission> {

//    @Query("SELECT DISTINCT p.module from Permission p")
//    List<String> finDistinctModules();
}
