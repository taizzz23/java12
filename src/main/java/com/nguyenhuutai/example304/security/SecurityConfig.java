package com.nguyenhuutai.example304.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    public static final String ADMIN = "admin";
    public static final String USER = "user";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors() // ✅ Bật CORS cho React frontend
                .and()
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests((auth2) -> auth2
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/some-path-here/**").permitAll()
                        .requestMatchers(HttpMethod.GET).permitAll()
                        .requestMatchers(HttpMethod.PUT).authenticated() // Giữ nguyên logic của bạn
                        .requestMatchers(HttpMethod.PATCH).permitAll()
                        .requestMatchers(HttpMethod.DELETE).permitAll()
                        .requestMatchers(HttpMethod.DELETE).permitAll()
                        .requestMatchers("/api/employee/tables/*/status").permitAll()
                        .requestMatchers(HttpMethod.POST).permitAll()
                        .anyRequest().authenticated())
                .csrf(c -> c.disable()); // ✅ Tắt CSRF để React gọi API PUT dễ hơn
        return http.build();
    }

    // ✅ Cấu hình CORS cho phép React (localhost:3000) truy cập
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000"); // React app
        configuration.addAllowedMethod("*"); // Cho phép mọi method (GET, POST, PUT, DELETE)
        configuration.addAllowedHeader("*"); // Cho phép mọi header
        configuration.setAllowCredentials(true); // Nếu bạn cần gửi cookie hoặc token

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
