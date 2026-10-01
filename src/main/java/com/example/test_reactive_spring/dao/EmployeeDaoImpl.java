package com.example.test_reactive_spring.dao;

import com.example.test_reactive_spring.entity.Employee;
import com.example.test_reactive_spring.entity.EmployeeInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Objects;

public class EmployeeDaoImpl implements EmployeeCustomDao {

    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @Value("${search.query}")
    private String searchQuery;

    @Override
    public Flux<Employee> search(EmployeeInput employee) {
        StringBuilder sql = new StringBuilder(searchQuery).append(" where");
        Criteria criteria = Criteria.empty();

        Class<?> clazz = employee.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            try {
                Object fieldValue = field.get(employee);
                if(Objects.nonNull(fieldValue) &&
                        ((field.getType()==String.class && !String.valueOf(fieldValue).isBlank()) ||
                                (field.getType()==Long.class && fieldValue!=null))
                ){
//                    if(field.getType()==String.class){
//                        String stringValue = (String) fieldValue;
//                        if (stringValue.isBlank()) {
//                            return null;
//                        }
//                    }
//                    sql.append(" "+fieldName+"=:"+fieldName+" and");
                    System.out.println("satheesh:"+fieldName+" "+field.getType());
                    criteria = criteria.and(Criteria.where(fieldName).is(fieldValue));
                }
            } catch (IllegalAccessException e) {
                System.out.println("Search Failed Due to: "+e.getMessage());
            }
        }

//        String finalQuery = sql.substring(0,sql.length()-3);
//        System.out.println(finalQuery);

        Query query = Query.query(criteria);
        Flux<Employee> employeeFlux = r2dbcEntityTemplate.select(Employee.class).matching(query).all();
        return employeeFlux.delayElements(Duration.ofSeconds(1));
//        return employeeFlux;
    }


}
