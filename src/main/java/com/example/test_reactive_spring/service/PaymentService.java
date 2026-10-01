package com.example.test_reactive_spring.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public interface PaymentService {

    public Mono<Map<String,Object>> processPayment(Map<String,String> payMap);
}
