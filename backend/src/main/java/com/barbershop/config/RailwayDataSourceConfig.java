package com.barbershop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class RailwayDataSourceConfig {

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                // Railway provides: postgresql://user:pass@host:port/db
                // We need: jdbc:postgresql://host:port/db
                URI uri = new URI(databaseUrl);
                
                String userInfo = uri.getUserInfo();
                String username = null;
                String password = null;
                
                if (userInfo != null) {
                    String[] parts = userInfo.split(":");
                    username = parts[0];
                    password = parts.length > 1 ? parts[1] : "";
                }

                String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();

                System.out.println("Configuring Railway PostgreSQL DataSource: " + jdbcUrl);

                return DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .build();
            } catch (Exception e) {
                System.err.println("Failed to parse DATABASE_URL: " + e.getMessage());
            }
        }
        
        // Fallback for local development (H2)
        System.out.println("Configuring Local H2 DataSource");
        return DataSourceBuilder.create()
                .url("jdbc:h2:mem:barbershopdb")
                .driverClassName("org.h2.Driver")
                .username("sa")
                .password("password")
                .build();
    }
}
