package com.campushub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "campushub.schema-initialization.enabled=false",
        "spring.datasource.password=test-only",
        "jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class CampusHubApplicationTests {

    @Test
    void contextLoads() {
    }

}
