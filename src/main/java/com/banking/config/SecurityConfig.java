package com.banking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
            User.builder()
                .username("manager")
                .password(encoder.encode("Manager@123"))
                .roles("MANAGER")
                .build()
        );
    }

    @Bean
    @Order(1)
    public SecurityFilterChain managerChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/manager/**")
            .authorizeHttpRequests(a -> a
                .requestMatchers("/manager/login", "/manager/do-login").permitAll()
                .anyRequest().hasRole("MANAGER"))
            .formLogin(f -> f
                .loginPage("/manager/login")
                .loginProcessingUrl("/manager/do-login")
                .defaultSuccessUrl("/manager/dashboard", true)
                .failureUrl("/manager/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password"))
            .logout(l -> l
                .logoutUrl("/manager/logout")
                .logoutSuccessUrl("/manager/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"))
            .csrf(c -> c.disable());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain customerChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
            .authorizeHttpRequests(a -> a.anyRequest().permitAll())
            .csrf(c -> c.disable())
            .headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }
}
