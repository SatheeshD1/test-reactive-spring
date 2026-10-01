package com.example.test_reactive_spring.dao;

import com.example.test_reactive_spring.entity.Employee;
import com.example.test_reactive_spring.entity.EmployeeInput;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Repository
public interface PaymentDao {
    public Mono<Long> processPayment(Map<String,String> payMap);
}
