package com.example.test_reactive_spring.controller;

import com.example.test_reactive_spring.dto.CurlRequest;
import com.example.test_reactive_spring.dto.CurlResponse;
import com.example.test_reactive_spring.service.EmployeeService;
import com.example.test_reactive_spring.service.impl.CurlExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
public class ReportsController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping(path = "/api/v5/downloadAll", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Flux<DataBuffer> exportEmployees() {
        return employeeService.exportEmployeesToExcel();
    }

    @Autowired
    private CurlExecutorService curlExecutorService;

    @GetMapping("/execute")
    public String executeFromDefaultFile() {
        CompletableFuture.runAsync(() -> curlExecutorService.bruteForceOtps());
        return "OTP brute-force started (1,000,000 combinations). Check console for results.";
    }

}
