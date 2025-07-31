package showcase.ai.data.pipeline.sentiment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
@Slf4j
public class VectorStoreConfig {
    @Value("classpath:sentiment_rag_content.txt")
    private Resource resource;

    @Bean
    List<Document> loadJsonAsDocuments() {
        var reader = new TextReader(this.resource);
        return reader.get();
    }

    @Bean
    QuestionAnswerAdvisor advisor(VectorStore vectorStore){
        return new QuestionAnswerAdvisor(vectorStore);
    }

    @Bean
    CommandLineRunner runner(VectorStore vectorStore, List<Document> documents){
        return args -> {

            log.info("Documents: {}",documents);
            vectorStore.accept(documents);
        };
    }
}
