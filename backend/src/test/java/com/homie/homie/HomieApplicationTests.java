package com.homie.homie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.datasource.url=jdbc:tc:postgresql:17-alpine:///testdb")
@ActiveProfiles("dev")
class HomieApplicationTests {

	@Test
	void contextLoads() {
	}

}
