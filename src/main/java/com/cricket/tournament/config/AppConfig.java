package com.cricket.tournament.config;

import com.cricket.tournament.filter.AuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers our custom Servlet Filter with Spring Boot.
 *
 * WHY: @WebFilter alone needs @ServletComponentScan,
 * but FilterRegistrationBean gives us more control
 * and is the clean Spring Boot way.
 */
@Configuration
public class AppConfig {

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration() {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthFilter());
        registration.addUrlPatterns("/*");   // intercept everything
        registration.setName("AuthFilter");
        registration.setOrder(1);            // first filter in chain
        return registration;
    }
}