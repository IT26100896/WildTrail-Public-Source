package com.wildtrail.config;

import com.wildtrail.security.SessionAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SessionAuthenticationFilter sessionFilter, com.wildtrail.security.AuditFilter audit, com.wildtrail.security.OriginGuard origin, com.wildtrail.security.LoginThrottle throttle) throws Exception {
        return http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .addFilterBefore(sessionFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(origin, SessionAuthenticationFilter.class)
            .addFilterBefore(throttle, SessionAuthenticationFilter.class)
            .addFilterAfter(audit, SessionAuthenticationFilter.class).build();
    }

    @Bean org.springframework.boot.web.servlet.FilterRegistrationBean<SessionAuthenticationFilter> sessionRegistration(SessionAuthenticationFilter filter) {var registration=new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;}
    @Bean org.springframework.boot.web.servlet.FilterRegistrationBean<com.wildtrail.security.AuditFilter> auditRegistration(com.wildtrail.security.AuditFilter filter) {var registration=new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;}
    @Bean org.springframework.boot.web.servlet.FilterRegistrationBean<com.wildtrail.security.OriginGuard> originRegistration(com.wildtrail.security.OriginGuard filter) {var registration=new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;}
    @Bean org.springframework.boot.web.servlet.FilterRegistrationBean<com.wildtrail.security.LoginThrottle> throttleRegistration(com.wildtrail.security.LoginThrottle filter) {var registration=new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;}
}
