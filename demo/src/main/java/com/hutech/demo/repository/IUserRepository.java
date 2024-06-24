package com.hutech.demo.repository;

import com.hutech.demo.RoleName;
import com.hutech.demo.model.Role;
import com.hutech.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    //@Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = ?1")
    List<User> findByAuthorities_RoleName(RoleName roleName);
}
