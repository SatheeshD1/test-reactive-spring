package com.example.test_reactive_spring.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.security.UsernamePasswordCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class HazelcastClientConfig {

    @Bean
    public HazelcastInstance hazelcastInstance(){
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName("dev");
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701");
//        clientConfig.getSecurityConfig().setCredentials(new UsernamePasswordCredentials("admin","password"));
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
