package com.hutech.demo.repository;

import com.hutech.demo.RoleName;
import com.hutech.demo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long>{
    Optional<Role> findByRoleName(RoleName roleName);

}
