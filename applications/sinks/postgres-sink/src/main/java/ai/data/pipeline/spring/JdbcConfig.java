package ai.data.pipeline.spring;

import ai.data.pipeline.spring.properties.SqlConsumerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@EnableJdbcRepositories
@EnableConfigurationProperties(SqlConsumerProperties.class)
public class JdbcConfig {
}
