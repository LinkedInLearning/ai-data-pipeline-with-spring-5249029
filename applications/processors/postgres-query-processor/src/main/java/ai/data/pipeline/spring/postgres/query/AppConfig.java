package ai.data.pipeline.spring.postgres.query;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ai.data.pipeline.spring.postgres.query.properties.QueryProperties;

@Configuration
@EnableConfigurationProperties(QueryProperties.class)
public class AppConfig {

}
