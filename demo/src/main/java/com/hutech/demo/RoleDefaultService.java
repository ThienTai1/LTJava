package com.hutech.demo;

import com.hutech.demo.model.Role;
import com.hutech.demo.repository.IRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoleDefaultService {

    @Autowired
    private IRoleRepository roleRepository;

    @PostConstruct
    public void RoleDefaultService() {
        List<Role> defaultRoles = new ArrayList<>();

        defaultRoles.add(createRole(RoleName.ADMIN, "Administrator"));
        defaultRoles.add(createRole(RoleName.USER, "User"));
        defaultRoles.add(createRole(RoleName.EMPLOYEE, "Employee"));

        roleRepository.saveAll(defaultRoles);
    }

    private Role createRole(RoleName roleName, String description) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    role.setDescription(description);
                    return role;
                });
    }
}