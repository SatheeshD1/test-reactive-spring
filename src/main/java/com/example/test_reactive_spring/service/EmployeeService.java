package com.example.test_reactive_spring.service;

import com.example.test_reactive_spring.entity.Employee;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public interface EmployeeService {

    Flux<Employee> getAll();

    Mono<Employee> getById(Long empId);

    Flux<DataBuffer> exportEmployeesToExcel();
}
