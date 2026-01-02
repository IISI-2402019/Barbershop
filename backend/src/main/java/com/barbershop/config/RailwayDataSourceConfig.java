package com.barbershop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@org.springframework.context.annotation.Profile("prod")
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
                throw new RuntimeException("Failed to configure Railway DataSource", e);
            }
        } else {
            System.err.println("DATABASE_URL is missing in PROD profile!");
            throw new RuntimeException("DATABASE_URL is required for production profile");
        }
    }
}
