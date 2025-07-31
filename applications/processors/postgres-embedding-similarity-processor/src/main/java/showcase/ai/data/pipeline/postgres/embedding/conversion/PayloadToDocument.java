package showcase.ai.data.pipeline.postgres.embedding.conversion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.patterns.conversion.Converter;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import showcase.ai.data.pipeline.postgres.embedding.properties.EmbeddingSimilarityProperties;

/**
 * Convert payload string to Document
 * @author Gregory Green
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PayloadToDocument implements Converter<String, Document> {

    private final EmbeddingSimilarityProperties properties;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public Document convert(String payload) {
        log.info("Reading tree for payload: {}",payload);
        var jsonNode = objectMapper.readTree(payload);

        var idNode = jsonNode.findValue("id");
        log.info("idNode: {}",jsonNode);

        var id = idNode != null? idNode.asText() : "";
        log.info("id: {}",id);

        var textBuilder = new StringBuilder();
        for(var fieldName : properties.getDocumentTextFieldNames())
        {
            JsonNode fieldValue = jsonNode.findValue(fieldName);
            String textValue = fieldValue != null? fieldValue.asText() : null;
            if(!textBuilder.isEmpty())
                textBuilder.append(",");

            textBuilder.append(textValue);

        }
        log.info("text: {}",textBuilder);
        return Document.builder().id(id).text(textBuilder.toString()).build();
    }
}
