
- Run RabbitMQ
```shell
docker run -it --name rabbitmq   --rm  -p 5672:5672 -p 15672:15672  rabbitmq:4.1.0-management 
```

Run Postgres

```shell
```shell
docker run --name postgresql --network data-pipelines --rm  -e POSTGRESQL_USERNAME=postgres -e ALLOW_EMPTY_PASSWORD=true -e POSTGRESQL_DATABASE=postgres -p 5432:5432 bitnami/postgresql:latest
```




psql

```shell
docker run --name psql -it --rm \
--network data-pipelines \
    bitnami/postgresql:latest psql -h postgresql -U postgres
```

```shell
create  schema  if not exists customer ;

create table customer.feedback(
    feed_id text NOT NULL,
    email text NOT NULL,
    user_feedback text NOT NULL,
    summary text NOT NULL,
    feedback_dt timestamp NOT NULL DEFAULT NOW(),
    sentiment smallint NOT NULL,
    score numeric NOT NULL,
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




::json->>'summary_text'

select pg_typeof(results::json)

```shell
SELECT
        positivity::json->0->>'label' as label,
        positivity::json->0->>'score' as score,
        (CASE
        WHEN positivity::json->0->>'label' = 'NEGATIVE' THEN -1
        WHEN positivity::json->0->>'label' = 'POSITIVE' THEN 1
        ELSE
        0
        END) as sentiment
        from (SELECT pgml.transform(
        task   => 'text-classification',
        inputs => ARRAY[
        'Why is the wait SO LONG!' ]
        ) as positivity) text_classification;
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
java -jar applications/processors/postgres-query-processor/target/postgres-query-processor-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml" --spring.datasource.driverClassName=org.postgresql.Driver --spring.cloud.stream.bindings.input.destination=customers.output.feedback.summary --spring.cloud.stream.bindings.output.destination=customers.output.feedback.sentiment --spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipeline-with-spring-showcase/applications/processors/postgres-query-processor/src/main/resources/sentiment-analysis.yml --spring.datasource.hikari.max-lifetime=600000 --spring.cloud.stream.bindings.input.group=postgres-query-processor
```



Start Sink


```shell
java -jar applications/sinks/postgres-sink/target/postgres-sink-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.url="jdbc:postgresql://localhost/postgres"  --spring.cloud.stream.bindings.input.destination=customers.output.feedback.sentiment --spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipeline-with-spring-showcase/applications/sinks/postgres-sink/src/main/resources/postgres-sentiment-analysis.yml --spring.cloud.stream.bindings.input.group=postgres-sink
```



```shell
http-text-sentiment=http | summarize: postgres-query | sentiment: postgres-query | postgres
```


Deploy

```properties
app.http.path-pattern=feedback
app.http.server.port=8094

app.summarize.spring.datasource.username=postgres
app.summarize.spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml"
app.summarize.spring.datasource.driverClassName=org.postgresql.Driver
app.summarize.spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipelines-with-scdf-showcase/applications/processors/postgres-query-processor/src/main/resources/text-summarization.yml
app.summarize.spring.datasource.hikari.max-lifetime=600000

app.sentiment.spring.datasource.username=postgres
app.sentiment.spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml"
app.sentiment.spring.datasource.driverClassName=org.postgresql.Driver
app.sentiment.spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipelines-with-scdf-showcase/applications/processors/postgres-query-processor/src/main/resources/sentiment-analysis.yml
app.sentiment.spring.datasource.hikari.max-lifetime=600000


app.postgres.spring.datasource.username=postgres
app.postgres.spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml"
app.postgres.spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipelines-with-scdf-showcase/applications/sinks/postgres-sink/src/main/resources/postgres-sentiment-analysis.yml
app.postgres.spring.datasource.driverClassName=org.postgresql.Driver
app.postgres.spring.datasource.hikari.max-lifetime=600000
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