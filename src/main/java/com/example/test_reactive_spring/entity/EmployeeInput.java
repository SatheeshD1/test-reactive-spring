package com.example.test_reactive_spring.entity;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;

@Data
public class EmployeeInput {
    private Long empId;
    private String firstName;
    private String lastName;
    private String ctryCd;
}
