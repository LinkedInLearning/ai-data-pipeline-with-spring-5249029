

```shell
ollama serve
```

pull and run a model like this:

```shell
ollama run llama3
```

```text
 Analyze the sentiment of this text: "I REALLY REALLY LOVE LONG LINE". Respond with only one word: Positive or Negative.

```

```text
Analyze the sentiment of this text: "I really love long wait".
            Respond with only one word: Positive, or Negative, taking into account the provided context.

Context:
I REALLY REALLY LOVE LONG LINE is a NEGATIVE sentiment
```


```text
Analyze the sentiment of this text: "Sure, keep me waiting like I have all DAY".
            Respond with only one word: Positive, or Negative, taking into account the provided context.

Context:
I REALLY REALLY LOVE LONG LINE is a NEGATIVE sentiment
```

```text
Analyze the sentiment of this text: "Your team is doing a great job to reduce long wait time".
            Respond with only one word: Positive, or Negative, taking into account the provided context.

Context:
I REALLY REALLY LOVE LONG LINE is a NEGATIVE sentiment
```


```text
Analyze the sentiment of this text: "Oh great, another update that totally doesn’t break anything. Just what I needed.".
            Respond with only one word: Positive, or Negative, taking into account the provided context.

Context:
I REALLY REALLY LOVE LONG LINE is a NEGATIVE sentiment
```


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
docker run --name postgres --network data-pipelines --rm  \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=postgres \
  -p 5432:5432 \
  -it postgres    
```

```shell
docker exec -it postgres psql -U postgres
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


Drop so it be recreated by Spring AI

```sql
drop table vector_store;
```



Start Http


```shell
java -jar runtime/http-source-rabbit-5.0.1.jar --http.supplier.pathPattern=feedback --server.port=8094 --spring.cloud.stream.bindings.output.destination=customers.input.feedback
```


Start Processor Text Summary



```shell
java -jar applications/processors/postgres-query-processor/target/postgres-query-processor-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml" --spring.datasource.driverClassName=org.postgresql.Driver --spring.cloud.stream.bindings.input.destination=customers.input.feedback --spring.cloud.stream.bindings.output.destination=customers.output.feedback.summary --spring.config.import=optional:file://$PWD/applications/processors/postgres-query-processor/src/main/resources/text-summarization.yml --spring.datasource.hikari.max-lifetime=600000 --spring.cloud.stream.bindings.input.group=postgres-query-processor
```
Start Processor Text sentiment

```shell
java -jar applications/processors/ai-sentiment-rag-processor/target/ai-sentiment-rag-processor-0.0.1-SNAPSHOT.jar --spring.cloud.stream.bindings.input.destination=customers.output.feedback.summary --spring.cloud.stream.bindings.output.destination=customers.output.feedback.sentiment --spring.datasource.username=postgres --spring.datasource.password=postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml" 
```


Start Sink


```shell
java -jar applications/sinks/postgres-sink/target/postgres-sink-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.password=postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.url="jdbc:postgresql://localhost/postgres"  --spring.cloud.stream.bindings.input.destination=customers.output.feedback.sentiment --spring.config.import=optional:file://$PWD/applications/sinks/postgres-sink/src/main/resources/postgres-sentiment-analysis-ollama.yml --spring.cloud.stream.bindings.input.group=postgres-sink
```



```shell
curl -X 'POST' \
  'http://localhost:8094/feedback' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id" : "S001",
  "email" : "jmatthews@email",
  "feedback" : "You know what. It is ok. I love being on hold FOREVER. I will just take by business somewhere else."
}'
```


In psql

```sql
select sentiment, summary from customer.feedback;

```
