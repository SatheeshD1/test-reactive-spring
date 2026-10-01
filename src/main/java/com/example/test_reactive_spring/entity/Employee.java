package com.example.test_reactive_spring.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="employee",schema = "public")
public class Employee {

    @Id
    @Column("emp_id")
    private Long empId;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("ctry_cd")
    private String ctryCd;
}
