package com.example.test_reactive_spring.service.impl;

import com.example.test_reactive_spring.dao.PaymentDao;
import com.example.test_reactive_spring.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentDao paymentDao;

    @Override
    public Mono<Map<String,Object>> processPayment(Map<String, String> payMap) {
        Mono<Long> updatedRows= paymentDao.processPayment(payMap);
//                .subscribe(data-> System.out.println("Data Updated:"+data));
        return updatedRows.flatMap(count->{
            if(count > 0){
                return Mono.just(Map.of("message","Payment Paid Successfully",
                        "amount",payMap.get("amount"),
                        "status","PAID"));
            } else {
                return Mono.just(Map.of("message","Payment Failed",
                        "amount",0,
                        "status","NOT PAID"));
            }
        });
    }
}
