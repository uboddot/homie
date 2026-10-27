package com.homie.security;

import static com.homie.security.ApplicationUserPermission.RECIPE_ADMIN;
import static com.homie.security.ApplicationUserPermission.RECIPE_READ;
import static com.homie.security.ApplicationUserPermission.RECIPE_WRITE;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.homie.jwt.JwtCookieAuthFilter;

@Configuration
public class ApplicationSecurityConfig {

    private final JwtCookieAuthFilter jwtCookieAuthFilter;

    public ApplicationSecurityConfig(JwtCookieAuthFilter jwtCookieAuthFilter) {
        this.jwtCookieAuthFilter = jwtCookieAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // SPA setup: the token goes out in the readable XSRF-TOKEN cookie, and apiFetch
                // sends it back as the X-XSRF-TOKEN header on every non-GET request
                .csrf(csrf -> csrf.spa())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/logout",
                                "/auth/verify-email", "/auth/resend-verification").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        // error dispatches run without the JWT filter, otherwise 403/404 would turn into 401
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/recipes").hasAuthority(RECIPE_WRITE.getPermission())
                        .requestMatchers(HttpMethod.PUT, "/recipes/**").hasAuthority(RECIPE_WRITE.getPermission())
                        .requestMatchers("/recipes/**").hasAuthority(RECIPE_READ.getPermission())
                        .requestMatchers(HttpMethod.PUT, "/meal-plan").hasAuthority(RECIPE_WRITE.getPermission())
                        .requestMatchers("/meal-plan/**").hasAuthority(RECIPE_READ.getPermission())
                        // personal preferences, every logged-in user has their own
                        .requestMatchers("/settings/**").authenticated()
                        .requestMatchers("/management/**").hasAuthority(RECIPE_ADMIN.getPermission())
                        .anyRequest().authenticated())
                .addFilterBefore(jwtCookieAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors-allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
