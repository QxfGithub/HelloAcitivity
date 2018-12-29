package com.qxf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class FileServerApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext context  = SpringApplication.run(FileServerApplication.class, args);

    }

}
