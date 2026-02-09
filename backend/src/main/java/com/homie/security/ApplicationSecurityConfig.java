package com.homie.security;

import static com.homie.security.ApplicationUserPermission.COURSE_WRITE;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
public class ApplicationSecurityConfig {

        private final PasswordEncoder passwordEncoder;

        public ApplicationSecurityConfig(PasswordEncoder passwordEncoder) {
                this.passwordEncoder = passwordEncoder;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/api/login",
                                                                "/api/user")
                                                .permitAll()
                                                .requestMatchers("/recipes/**")
                                                .hasRole(ApplicationUserRole.STUDENT.name())
                                                .requestMatchers(HttpMethod.DELETE, "/management/**")
                                                .hasAuthority(COURSE_WRITE.getPermission())
                                                .requestMatchers(HttpMethod.POST, "/management/**")
                                                .hasAuthority(COURSE_WRITE.getPermission())
                                                .requestMatchers(HttpMethod.PUT, "/management/**")
                                                .hasAuthority(COURSE_WRITE.getPermission())
                                                .requestMatchers("/management/**")
                                                .hasAnyRole(ApplicationUserRole.ADMIN.name())
                                                .requestMatchers("/api/logout").authenticated()
                                                .anyRequest().authenticated())
                                .rememberMe(remember -> remember
                                                .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(21)) // 21 days
                                                .key("somethingverysecured"));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(java.util.List.of("http://localhost:5173", "http://localhost:3000"));
                configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(java.util.List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        protected UserDetailsService userDetailsService() {
                UserDetails annaSmithUser = User.builder()
                                .username("annasmith")
                                .password(passwordEncoder.encode("password"))
                                // .roles(ApplicationUserRole.STUDENT.name()) // ROLE_STUDENT
                                .authorities(ApplicationUserRole.STUDENT.getGrantedAuthorities())
                                .build();

                UserDetails lindaUser = User.builder()
                                .username("linda")
                                .password(passwordEncoder.encode("password123"))
                                // .roles(ApplicationUserRole.ADMIN.name()) // ROLE_ADMIN
                                .authorities(ApplicationUserRole.ADMIN.getGrantedAuthorities())
                                .build();

                UserDetails tomUser = User.builder()
                                .username("tom")
                                .password(passwordEncoder.encode("password123"))
                                // .roles(ApplicationUserRole.ADMINTRAINEE.name()) // ROLE_ADMINTRAINEE
                                .authorities(ApplicationUserRole.ADMINTRAINEE.getGrantedAuthorities())
                                .build();

                return new InMemoryUserDetailsManager(
                                annaSmithUser,
                                lindaUser,
                                tomUser);
        }
}
