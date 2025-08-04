package showcase.ai.data.orchestration.scdf.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import showcase.ai.data.orchestration.scdf.properties.QueryProperties;

import java.util.Map;
import java.util.function.Function;

/**
 * Execute a SQL query based on the input JSON and return JSON of the SQL results
 * @author Gregory Green
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QueryFunctionProcessor implements Function<String,String> {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ObjectMapper objectMapper;
    private final QueryProperties queryProperties;

    @SneakyThrows
    @Override
    public String apply(String payload) {

        log.info("payload: {}",payload);
        var inputMap  = objectMapper.readValue(payload, Map.class);

        log.info("SQL: {}, input: {}",queryProperties,inputMap);

        var outMap = namedParameterJdbcTemplate.queryForMap(queryProperties.getSql(),
                inputMap);
        log.info("SQL: {}, class:{}, results: {}",queryProperties,outMap.getClass(),outMap);

        var out = objectMapper.writeValueAsString(outMap);
        log.info("Returning: {}",out);
        return out;
    }




}
