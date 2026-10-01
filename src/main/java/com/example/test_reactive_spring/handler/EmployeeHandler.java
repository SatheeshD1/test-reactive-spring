package com.example.test_reactive_spring.handler;

import com.example.test_reactive_spring.dao.EmployeeDao;
import com.example.test_reactive_spring.entity.Employee;
import com.example.test_reactive_spring.generator.EmployeeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.print.attribute.standard.Media;
import java.time.Duration;
import java.util.List;

@Service
public class EmployeeHandler {

    @Autowired
    private EmployeeDao employeeDao;

    public Mono<ServerResponse> getEmployeeList(ServerRequest serverRequest){
        Flux<Employee> employeeList = employeeDao.findAll();
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(employeeList,Employee.class);
    }

    public Mono<ServerResponse> getEmployeeStream(ServerRequest serverRequest){
        Flux<String> stringList = Flux.range(1,50)
                .delayElements(Duration.ofMillis(1000))
                .doOnNext(i-> System.out.println("processing count: "+i))
                .map(data-> String.valueOf(data));
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(stringList,String.class);
    }

    public Mono<ServerResponse> getByEmpId(ServerRequest serverRequest){
        long empId = Long.valueOf(serverRequest.pathVariable("empId"));
        Mono<Employee> employeeMono= employeeDao.findAll().filter(data-> data.getEmpId()==empId).next();
        return ServerResponse.ok().body(employeeMono,Employee.class);
    }

    public Mono<ServerResponse> save(ServerRequest serverRequest){
        Mono<Employee> employeeMono = serverRequest.bodyToMono(Employee.class);
        Mono<String> stringMono = employeeMono.map(data-> data.getEmpId()+"_"+data.getFirstName());
        Mono<String> successMono = Mono.just("Saved Succesfully");
        return ServerResponse.ok().body(employeeMono,Employee.class);
    }

}
