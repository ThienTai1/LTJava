package com.hutech.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleName {
    ADMIN(1),
    USER(2),
    EMPLOYEE(3);

    private final long value;
}