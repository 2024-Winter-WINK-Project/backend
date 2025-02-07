package com.WinkProject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
	"spring.main.allow-bean-definition-overriding=true"
})
class WinkProjectApplicationTests {

	@Test
	void contextLoads() {
	}

}
