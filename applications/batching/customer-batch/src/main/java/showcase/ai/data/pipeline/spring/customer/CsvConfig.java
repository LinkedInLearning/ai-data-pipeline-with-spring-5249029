package showcase.ai.data.pipeline.spring.customer;

import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.io.csv.CsvWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;

@Configuration
@Slf4j
public class CsvConfig {

    @Value("${processor.output.error.file.csv}")
    private Resource invalid_customers_csv;

    @Bean
    CsvWriter csvWriter() throws IOException {
        var path = invalid_customers_csv.getFile().toPath();

        if(!Files.exists(path.getParent())){
            //create directory if it does exist
            var directory = Files.createDirectory(path.getParent());
            log.info("directory: {}",directory);
        }
        return new CsvWriter(path.toFile());
    }
}
