package showcase.ai.data.pipeline.postgres.embedding.function;

import nyla.solutions.core.patterns.conversion.Converter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import showcase.ai.data.pipeline.postgres.embedding.domain.SimilarDocuments;
import showcase.ai.data.pipeline.postgres.embedding.properties.EmbeddingSimilarityProperties;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbeddingSimilarityFunctionTest {

    private static final String expectedId = "001";
    private static final String payload = """
            { "id" : "001"}
            """;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private Converter<String, Document> converter;

    private EmbeddingSimilarityFunction subject;

    private static final EmbeddingSimilarityProperties properties = EmbeddingSimilarityProperties.builder()
            .topK(4)
            .similarityThreshold(0.95)
             .build();
    @Mock
    private Document document;
    private final String text = "Expected";
    private String id = "Expected Id";
    @Mock
    private Document resultDocument;

    @BeforeEach
    void setUp() {
        subject = new EmbeddingSimilarityFunction(vectorStore,properties,converter);
    }

    @Test
    void accept() {
        when(converter.convert(any())).thenReturn(document);
        when(document.getText()).thenReturn(text);
        when(document.getId()).thenReturn(id);
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(resultDocument));
        when(resultDocument.getId()).thenReturn("Different Id");

        SimilarDocuments actual = subject.apply(payload);


       assertThat(actual).isNotNull();
    }

    @Test
    void returnNull() {
        when(converter.convert(any())).thenReturn(document);
        when(document.getText()).thenReturn(text);
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(null);

        assertThat(subject.apply(payload)).isNull();

    }

    @Test
    void returnEmptyListNull() {
        when(converter.convert(any())).thenReturn(document);
        when(document.getText()).thenReturn(text);
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(Collections.emptyList());

        assertThat(subject.apply(payload)).isNull();

    }

    @Test
    void removeMatchingDocument() {


        when(converter.convert(any())).thenReturn(document);
        when(document.getId()).thenReturn(id);

        when(document.getText()).thenReturn(text);
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(document));

        assertThat(subject.apply(payload)).isNull();
    }
}