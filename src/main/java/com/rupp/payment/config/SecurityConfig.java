package com.rupp.payment.config;

import com.rupp.payment.security.JwtAuthenticationEntryPoint;
import com.rupp.payment.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/h2-console",
                                "/h2-console/**",
                                "/error"
                        ).permitAll()
                        // Student profile and academic selection endpoints (accessible by authenticated student)
                        .requestMatchers(HttpMethod.GET, "/api/v1/students/my-profile").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/students/my-academic-info").authenticated()
                        // Student management: Admin and Finance Staff
                        .requestMatchers("/api/v1/students/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_FINANCE_STAFF")
                        // Academic Structure: Read is open to all authenticated users; CUD requires Admin/Finance Staff
                        .requestMatchers(HttpMethod.GET, "/api/v1/faculties/**", "/api/v1/academic-years/**", "/api/v1/majors/**", "/api/v1/semesters/**").authenticated()
                        .requestMatchers("/api/v1/faculties/**", "/api/v1/academic-years/**", "/api/v1/majors/**", "/api/v1/semesters/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_FINANCE_STAFF")
                        // Tuition Fee Structure: Read is open to all authenticated users; CUD requires Admin
                        .requestMatchers(HttpMethod.GET, "/api/v1/tuition-fees/**").authenticated()
                        .requestMatchers("/api/v1/tuition-fees/**").hasAuthority("ROLE_ADMIN")
                        // Payment endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments").hasAuthority("ROLE_STUDENT")
                        .requestMatchers("/api/v1/payments/*/verification/**", "/api/v1/payments/verification/**").hasAuthority("ROLE_STUDENT")
                        .requestMatchers("/api/v1/payments/my-payments", "/api/v1/payments/my-history").hasAuthority("ROLE_STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/{id}", "/api/v1/payments/{id}/status", "/api/v1/payments/statuses").authenticated()
                        .requestMatchers("/api/v1/payments/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_FINANCE_STAFF")
                        // Receipt endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/receipts/{id}", "/api/v1/receipts/payment/**", "/api/v1/receipts/number/**").authenticated()
                        .requestMatchers("/api/v1/receipts/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_FINANCE_STAFF")
                        // Financial Reports: Admin and Finance Staff
                        .requestMatchers("/api/v1/reports/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_FINANCE_STAFF")
                        // System Monitoring: Admin only
                        .requestMatchers("/api/v1/admin/monitoring/**").hasAuthority("ROLE_ADMIN")
                        // User management: Admin only
                        .requestMatchers("/api/v1/users/**").hasAuthority("ROLE_ADMIN")
                        // All other requests require authentication
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
