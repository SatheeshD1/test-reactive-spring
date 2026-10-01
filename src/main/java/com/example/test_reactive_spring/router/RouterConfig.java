package com.example.test_reactive_spring.router;

import com.example.test_reactive_spring.handler.EmployeeCrudHandler;
import com.example.test_reactive_spring.handler.EmployeeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig implements WebFluxConfigurer {

    @Autowired
    private EmployeeHandler employeeHandler;

    @Autowired
    private EmployeeCrudHandler employeeCrudHandler;

    @Bean
    public RouterFunction<ServerResponse> handleRoutes(){
        return RouterFunctions.route()
                .GET("/api/v2/getEmployeeList", employeeHandler::getEmployeeList)
                .GET("/api/v2/getEmployeeStream", employeeHandler::getEmployeeStream)
                .GET("/api/v2/getByEmpId/{empId}",employeeHandler::getByEmpId)
                .POST("/api/v2/save",employeeHandler::save)
                .GET("/api/v3/load", employeeCrudHandler::load)
                .POST("/api/v3/save", employeeCrudHandler::save)
                .PUT("/api/v3/update", employeeCrudHandler::update)
                .DELETE("/api/v3/delete/{empId}", employeeCrudHandler::delete)
                .GET("/api/v4/saveAll",employeeCrudHandler::saveAll)
                .GET("/api/v4/getLimited/{limit}", employeeCrudHandler::getLimited)
                .build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply to all endpoints
                .allowedOrigins("*") // Allow all origins (use specific origins in production)
                .allowedMethods("*") // Allow all methods
                .allowedHeaders("*") // Allow all headers
//                .allowCredentials(true)
                .maxAge(3600L);
    }


}
