package com.dd.glsc.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.dd.glsc.ware.dao")
@SpringBootApplication
public class GlscWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(GlscWareApplication.class, args);
    }

}
