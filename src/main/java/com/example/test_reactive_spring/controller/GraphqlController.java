package com.example.test_reactive_spring.controller;

import com.example.test_reactive_spring.dao.EmployeeDao;
import com.example.test_reactive_spring.entity.Employee;
import com.example.test_reactive_spring.entity.EmployeeInput;
import com.example.test_reactive_spring.entity.MessageInput;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
@Controller
public class GraphqlController {

    private final ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate;

    @Autowired
    private EmployeeDao employeeDao;

//    @Autowired
//    private HazelcastInstance hazelcastInstance;

    @Value("${message.topic}")
    private String topic;

    @QueryMapping
    public Flux<Employee> getLimited(@Argument String limit){
//        Map<String,String> cache = hazelcastInstance.getMap("my-cache");
//        cache.put("satheesh",limit);
//        System.out.println("cache info:"+ cache.get("satheesh"));
        Flux<Employee> employeeFlux = employeeDao.findAll().take(Long.valueOf(limit));
        return employeeFlux;
    }

    @MutationMapping
    public Mono<Boolean> post(@Argument MessageInput message){
        return reactiveKafkaProducerTemplate.send(topic, message.toString())
                .map(result -> true)
                .onErrorResume(e -> Mono.just(false));
    }

    @QueryMapping
    public Flux<Employee> search(@Argument EmployeeInput employee){
        return employeeDao.search(employee);
    }

    @SubscriptionMapping
    public Flux<Employee> searchStream(@Argument EmployeeInput employee){
        return employeeDao.search(employee);
    }
}
