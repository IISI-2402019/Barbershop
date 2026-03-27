package com.barbershop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                Long userId = (Long) request.getAttribute("userId");
                return Optional.ofNullable(userId);
            }
            return Optional.empty();
        };
    }
}
