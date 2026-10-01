package com.example.test_reactive_spring.dao;

import com.example.test_reactive_spring.entity.Employee;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface EmployeeDao extends ReactiveCrudRepository<Employee,Long>
        , ReactiveSortingRepository<Employee,Long>,EmployeeCustomDao {

}
