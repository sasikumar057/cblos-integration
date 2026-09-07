package com.cblos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomSuccessHandler customSuccessHandler;
    private final AppUserDetailsService userDetailsService;

    public SecurityConfig(CustomSuccessHandler customSuccessHandler,
                          AppUserDetailsService userDetailsService) {
        this.customSuccessHandler = customSuccessHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/CBLOS", "/corporate-login", "/register", "/", "/register/***",
                        "/forgot-password", "/forgot-password/verify", "/forgot-password/update",
                        "/forgot-password/change-password", "/forgot-password/change-success").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/customers/onboard").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/customers/forgot-password").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/customers/registration-status").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/api/customers/update/*").permitAll()
.requestMatchers(HttpMethod.PATCH, "/api/customers/update/*", "/api/customers/*").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/customer/dashboard/**", "/customer/**").hasRole("CUSTOMER")
                .requestMatchers("/officer/**").hasRole("OFFICER")
                .requestMatchers("/manager/**").hasRole("MANAGER")

                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                if (request.getRequestURI().startsWith("/api/")) {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"status\":\"UNAUTHORIZED\",\"message\":\"Authentication required.\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/corporate-login");
                }
            }))
            .formLogin(form -> form
                .loginPage("/corporate-login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(customSuccessHandler)
                .failureUrl("/corporate-login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("http://localhost:4200?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
