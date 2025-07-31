package showcase.ai.data.pipeline.postgres.embedding.conversion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import showcase.ai.data.pipeline.postgres.embedding.properties.EmbeddingSimilarityProperties;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 *
 * @author Gregory Green
 */
@ExtendWith(MockitoExtension.class)
class PayloadToDocumentTest {

    private PayloadToDocument subject;

    @Mock
    private EmbeddingSimilarityProperties properties;
    private final String[] fields = {"email","phone","zip","state","city","address","lastName","firstName"};


    @BeforeEach
    void setUp() {
        subject = new PayloadToDocument(properties,new ObjectMapper());
    }

    @Test
    void convert() {

        when(properties.getDocumentTextFieldNames()).thenReturn(fields);

        var expectedText = "email@,555-555-5555,23232,my state,city,1 street,Smith,John";
        Document expected = Document.builder().id("junit")
                .text(expectedText).build();
        var payload = """
                {
                    "id" : "junit",
                    "firstName": "John",
                    "lastName": "Smith",
                    "email": "email@",
                    "phone": "555-555-5555",
                    "address": "1 street",
                    "city": "city",
                    "state": "my state",
                    "zip": "23232"
                }
                """;

        var actual = subject.convert(payload);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void bug() {

        System.out.println(UUID.randomUUID());

        var payload = """
                {
                  "id" : "8df15279-97a6-4b48-92f3-f78d045d9cc4",
                  "firstName" : "Josiah",
                  "lastName" : "Imani",
                  "email" : "email@email",
                  "phone" : "555-555-5555",
                  "address" : "12 Straight St",
                  "city" : "gold",
                  "state" : "ny",
                  "zip": "55555"
                }
                """;

        when(properties.getDocumentTextFieldNames()).thenReturn(fields);

        var actual=subject.convert(payload);

        assertThat(actual.getText()).isNotNull();
    }
}