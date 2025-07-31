package showcase.ai.data.pipeline.postgres.embedding.domain;

import lombok.Builder;
import org.springframework.ai.document.Document;

import java.util.List;

@Builder
public record SimilarDocuments(String id, String similaritiesPayload) {
}
