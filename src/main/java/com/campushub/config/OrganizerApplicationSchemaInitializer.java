package com.campushub.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "campushub.schema-initialization.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OrganizerApplicationSchemaInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public OrganizerApplicationSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS organizer_application (
              id BIGINT PRIMARY KEY AUTO_INCREMENT,
              user_id BIGINT NOT NULL,
              reason VARCHAR(500) NOT NULL,
              status TINYINT NOT NULL DEFAULT 1,
              reviewer_id BIGINT NULL,
              review_reason VARCHAR(500) NULL,
              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
              reviewed_at DATETIME NULL,
              KEY idx_organizer_application_user (user_id, id),
              KEY idx_organizer_application_status (status, id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);
    }
}
