package com.example.test_reactive_spring.controller;

import com.example.test_reactive_spring.entity.Employee;
import com.example.test_reactive_spring.service.EmployeeService;
import com.example.test_reactive_spring.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/")
public class ReactiveController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PaymentService paymentService;

    //Need to remove
    private Map<String,String> cacheMap = new HashMap<>();

    @GetMapping("getLog")
    public Mono<String> getLog(){
        return Mono.just("SpringReactive");
    }

    @GetMapping("getAll")
    public Flux<Employee> getAll(){
        System.out.println("getAll api: "+Thread.currentThread().getName());
        return employeeService.getAll();
    }

    @GetMapping({"getById/{id}"})
    public Mono<Employee> getById(@PathVariable("id") Long empId){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("getById api: "+Thread.currentThread().getName());
        return employeeService.getById(empId);
    }

    @GetMapping(value = "getReactiveStream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getReactiveStream(){
        Flux<String> stream = Flux.range(1,10).delayElements(Duration.ofMillis(2000))
                .doOnNext(i-> System.out.println("processing: "+i))
                .map(data-> String.valueOf(data));
        return stream;
    }

    @PostMapping("processPayment")
    public Mono<Map<String,Object>> processPayment(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                                   @RequestBody Map<String,String> payMap){
        if(cacheMap.containsKey(idempotencyKey)){
            return Mono.just(Map.of("message","Payment Paid Already"));
        }
        cacheMap.put(idempotencyKey,"true");
        return paymentService.processPayment(payMap);
    }

}
