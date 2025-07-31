Run Rabbit

```shell
docker network create data-pipeline
```

start rabbitmq
```shell
docker run -it --name rabbitmq   --rm  -p 5672:5672 -p 15672:15672  rabbitmq:4.1.0-management 
```


Run Postgres

```shell
docker run --name postgresql --network data-pipeline --rm  -e POSTGRESQL_USERNAME=postgres -e ALLOW_EMPTY_PASSWORD=true -e POSTGRESQL_DATABASE=postgres -p 5432:5432 bitnami/postgresql:latest 
```

```shell
docker exec -it postgresql psql -U postgres
```


```shell
create  schema  if not exists customer ;

create table customer.feedback(
    feed_id text NOT NULL,
    email text NOT NULL,
    user_feedback text NOT NULL,
    summary text NOT NULL,
    feedback_dt timestamp NOT NULL DEFAULT NOW(),
    sentiment text NOT NULL,
 PRIMARY KEY (feed_id)
);
```

Run PostgresML

```shell
docker run --rm --name postgresml \
    -it \
    --network data-pipelines  \
    -v postgresml_data:/var/lib/postgresql \
    -p 6432:5432 \
    -p 8000:8000 \
    ghcr.io/postgresml/postgresml:2.10.0 \
    sudo -u postgresml psql -d postgresml
```



Start Ollama

```shell
ollama serve
```

pull and run a model like this:

```shell
ollama run llama3
```

Test with llama3 model with the following

```shell
Analyze the sentiment of this text: "Hello my name is John Smith. I am long time customer. It seems that every time I call the help desk there is a very long wait . When I follow get someone on the line, I have the repeat to repeat the process of the provide the details.".
            Respond with only one word: Positive or Negative.
```

---------------------------


Start Http


```shell
java -jar runtime/http-source-rabbit-5.0.1.jar --http.supplier.pathPattern=feedback --server.port=8094 --spring.cloud.stream.bindings.output.destination=customers.input.feedback
```


Start Processor Text Summary

```shell
java -jar applications/processors/postgres-query-processor/target/postgres-query-processor-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml" --spring.datasource.driverClassName=org.postgresql.Driver --spring.cloud.stream.bindings.input.destination=customers.input.feedback --spring.cloud.stream.bindings.output.destination=customers.output.feedback.summary --spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipeline-with-spring-showcase/applications/processors/postgres-query-processor/src/main/resources/text-summarization.yml --spring.datasource.hikari.max-lifetime=600000 --spring.cloud.stream.bindings.input.group=postgres-query-processor
```
Start Processor Text sentiment

```shell
java -jar applications/processors/ai-sentiment-processor/target/ai-sentiment-processor-0.0.1-SNAPSHOT.jar --spring.cloud.stream.bindings.input.destination=customers.output.feedback.summary --spring.cloud.stream.bindings.output.destination=customers.output.feedback.sentiment
```



Start Sink


```shell
java -jar applications/sinks/postgres-sink/target/postgres-sink-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.url="jdbc:postgresql://localhost/postgres"  --spring.cloud.stream.bindings.input.destination=customers.output.feedback.sentiment --spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipeline-with-spring-showcase/applications/sinks/postgres-sink/src/main/resources/postgres-sentiment-analysis-ollama.yml --spring.cloud.stream.bindings.input.group=postgres-sink
```



```shell
curl -X 'POST' \
  'http://localhost:8094/feedback' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id" : "F001",
  "email" : "jmatthews@email",
  "feedback" : "Hello my name is John Smith. I am long time customer. It seems that every time I call the help desk there is a very long wait. Then when I following get someone on the line, I have the repeat to repeat the process of the provide the details. This is very disappointing."
}'
```


In psql

```sql
select * from customer.feedback;

```
