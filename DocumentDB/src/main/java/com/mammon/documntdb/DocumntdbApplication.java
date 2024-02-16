package com.mammon.documntdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
//docker run -v /var/run/docker.sock:/var/run/docker.sock --name containerB myimage
@SpringBootApplication
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DocumntdbApplication {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    public static void main(String[] args) {
        SpringApplication.run(DocumntdbApplication.class, args);
    }

}
