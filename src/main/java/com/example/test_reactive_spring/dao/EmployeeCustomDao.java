package com.example.test_reactive_spring.dao;

import com.example.test_reactive_spring.entity.Employee;
import com.example.test_reactive_spring.entity.EmployeeInput;
import reactor.core.publisher.Flux;

public interface EmployeeCustomDao {

    public Flux<Employee> search(EmployeeInput employee);
}
