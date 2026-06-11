package com.smartlogix.envios;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=X2@pQ!8zH#1kM$eY6%WnR3vG7*CdT4bU^aL0sJ9fB5h",
    "jwt.expiration=7200000",
    "jwt.prefix=Bearer",
    "jwt.header=Authorization"
})
class EnviosApplicationTests {

    @Test
    void contextLoads() {
    }
}
