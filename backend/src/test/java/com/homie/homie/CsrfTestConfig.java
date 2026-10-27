package com.homie.homie;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Adds a valid CSRF token to every MockMvc request, like the frontend's apiFetch does,
 * so the tests don't need .with(csrf()) on each POST/PUT. Picked up by component scan.
 */
@Configuration
class CsrfTestConfig {

    @Bean
    MockMvcBuilderCustomizer csrfByDefault() {
        return builder -> builder.defaultRequest(get("/").with(csrf()));
    }
}
