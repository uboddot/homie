package com.homie.homie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:tc:postgresql:17-alpine:///verifytestdb")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class EmailVerificationTests {

    private static final Pattern TOKEN = Pattern.compile("verify-email\\?token=([A-Za-z0-9_-]+)");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void registerSendsLinkAndClickingItActivatesTheAccount() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newbie\",\"email\":\"newbie@example.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isAccepted())
                .andExpect(cookie().doesNotExist("access_token"));

        String login = "{\"username\":\"newbie\",\"password\":\"secret123\"}";
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isForbidden());

        ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mail.capture());
        assertThat(mail.getValue().getTo()).containsExactly("newbie@example.com");
        Matcher matcher = TOKEN.matcher(mail.getValue().getText());
        assertThat(matcher.find()).isTrue();
        String verifyBody = "{\"token\":\"" + matcher.group(1) + "\"}";

        mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content(verifyBody))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"));

        // one-time link
        mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content(verifyBody))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk());
    }

    @Test
    void unknownTokenIsRejected() throws Exception {
        mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"nope\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void seededUsersCanStillLogIn() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"anna\",\"password\":\"password\"}"))
                .andExpect(status().isOk());
    }
}
