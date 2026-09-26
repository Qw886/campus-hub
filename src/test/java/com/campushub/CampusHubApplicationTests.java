package com.campushub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "campushub.schema-initialization.enabled=false")
class CampusHubApplicationTests {

    @Test
    void contextLoads() {
    }

}
