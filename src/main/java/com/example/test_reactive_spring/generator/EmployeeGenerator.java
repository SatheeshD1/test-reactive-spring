package com.example.test_reactive_spring.generator;

import com.example.test_reactive_spring.entity.Employee;
import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

public class EmployeeGenerator {

    public static List<Employee> generateEmployees(int recordCount){
        Faker faker = new Faker();
        List<Employee> employeeList = new ArrayList<>();
        for(int i=1; i<=recordCount;i++){
            employeeList.add(new Employee(null,
                    faker.name().firstName(),
                    faker.name().lastName(),
                    faker.country().countryCode2().toUpperCase()
                    ));
        }
        return employeeList;
    }
}
