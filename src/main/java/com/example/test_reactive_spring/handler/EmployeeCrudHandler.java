package com.example.test_reactive_spring.handler;

import com.example.test_reactive_spring.dao.EmployeeDao;
import com.example.test_reactive_spring.entity.Employee;
import com.example.test_reactive_spring.generator.EmployeeGenerator;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EmployeeCrudHandler {

    @Autowired
    private EmployeeDao employeeDao;

    public Mono<ServerResponse> load(ServerRequest serverRequest){
        Flux<Employee> employeeFlux = employeeDao.findAll();
        return ServerResponse.ok().body(employeeFlux, Employee.class);
    }

    public Mono<ServerResponse> save(ServerRequest serverRequest){
        Mono<Employee> employeeMono =  serverRequest.bodyToMono(Employee.class);
        return employeeMono.flatMap(employeeDao::save)
                .flatMap(saved->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(saved,Employee.class))
                .onErrorResume(error-> ServerResponse.badRequest().bodyValue("Save Operation Failed"+error.getMessage()));
    }

    public Mono<ServerResponse> update(ServerRequest serverRequest){
        Mono<Employee> employeeMono =  serverRequest.bodyToMono(Employee.class);
        return employeeMono.flatMap(employeeDao::save).flatMap(updated ->
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(updated))
                .onErrorResume(error->
                        ServerResponse.badRequest().bodyValue("Update Operation Failed"+error.getMessage()));
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest){
        long empId = Long.valueOf(serverRequest.pathVariable("empId"));
        employeeDao.deleteById(empId);
        return Mono.empty();
    }

    public Mono<ServerResponse> saveAll(ServerRequest request){
        long start =  System.currentTimeMillis();
        List<Employee> employeeList = EmployeeGenerator.generateEmployees(1000000);
        long end =  System.currentTimeMillis();
        System.out.println("Time Taken for Data: "+ (end-start)+" ms");

        AtomicLong fluxStart = new AtomicLong();

         return Flux.fromIterable(employeeList)
                 .doOnSubscribe(subscription -> {
                     fluxStart.set(System.currentTimeMillis());
                     System.out.println("Bulk insertion process started.");
                 })
                .buffer(1000)
//                .delayElements(Duration.ofMillis(10))
                .flatMap(chunk->
                        employeeDao.saveAll(chunk)
                                .collectList().doOnNext(savedBatch-> System.out.println("BATCH INSERTED: "
                                +savedBatch.size()+" "+Thread.currentThread().getName())))
                 .doFinally(signalType -> {
                     long fluxEnd = System.currentTimeMillis();
                     System.out.println("Bulk insertion completed. "+ (fluxEnd - fluxStart.get()));
                 })
                .then(ServerResponse.ok().body(Mono.just("Data Inserted"),String.class));
    }

    public Mono<ServerResponse> getLimited(ServerRequest request){
        String limit = request.pathVariable("limit");
        Flux<Employee> employeeFlux = employeeDao.findAll().take(Long.valueOf(limit));
        return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(employeeFlux, Employee.class);
    }
 }
