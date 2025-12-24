package com.barbershop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DataSourceCheck implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("----------------------------------------------------------");
            System.out.println("Active Database URL: " + connection.getMetaData().getURL());
            System.out.println("Active Database User: " + connection.getMetaData().getUserName());
            System.out.println("----------------------------------------------------------");
        }
    }
}
