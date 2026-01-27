package com.simpleshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

@Modulith
@SpringBootApplication
public class SimpleShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleShopApplication.class, args);
    }
}
