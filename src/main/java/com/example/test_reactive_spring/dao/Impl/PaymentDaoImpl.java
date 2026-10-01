package com.example.test_reactive_spring.dao.Impl;

import com.example.test_reactive_spring.dao.PaymentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class PaymentDaoImpl implements PaymentDao {

    @Autowired
    private DatabaseClient databaseClient;

    @Value("${update.query}")
    private String updateQuery;

    @Override
    public Mono<Long> processPayment(Map<String, String> payMap) {
        Map<String, Object> typedUpdates = payMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            if ("amount".equals(entry.getKey())) {
                                return new BigDecimal(entry.getValue());
                            }
                            return entry.getValue();
                        }
                ));

        typedUpdates.put("status","PAID");
        // 2. Start the query using the template from properties and bind all values.
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(updateQuery)
                .bindValues(typedUpdates);

        // 3. Bind the UETR explicitly as it is part of the WHERE clause.
        spec = spec.bind("uetr", payMap.get("uetr"));

        // 4. Execute the update and return a Mono with the number of rows affected.
        return spec.fetch().rowsUpdated();
    }
}
