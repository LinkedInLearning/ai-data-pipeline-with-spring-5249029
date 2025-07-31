package showcase.ai.data.pipeline.postgres.embedding;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import showcase.ai.data.pipeline.postgres.embedding.properties.EmbeddingSimilarityProperties;

@Configuration
@EnableConfigurationProperties(EmbeddingSimilarityProperties.class)
public class AppConfig {

}
