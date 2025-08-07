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

Run PostgresML (used by the text summary processor)

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
Analyze the sentiment of this text: "Hello my name is John Smith. I am long time customer. It seems that every time I call the help desk there is a very long wait . When I finally get someone on the line, I have the repeat the process of the provide my details.".
            Respond with only one word: Positive or Negative.
```

---------------------------


Start Http

```shell
java -jar runtime/http-source-rabbit-5.0.1.jar --http.supplier.pathPattern=feedback --server.port=8094 --spring.cloud.stream.bindings.output.destination=customers.input.feedback
```


Start Processor Text Summary

```shell
java -jar applications/processors/postgres-query-processor/target/postgres-query-processor-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.password=postgres --spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml" --spring.datasource.driverClassName=org.postgresql.Driver --spring.cloud.stream.bindings.input.destination=customers.input.feedback --spring.cloud.stream.bindings.output.destination=customers.output.feedback.summary --spring.config.import=optional:file://$PWD/applications/processors/postgres-query-processor/src/main/resources/text-summarization.yml --spring.datasource.hikari.max-lifetime=600000 --spring.cloud.stream.bindings.input.group=postgres-query-processor
```
Start Sentiment Analysis Processor

```shell
java -jar applications/processors/ai-sentiment-processor/target/ai-sentiment-processor-0.0.1-SNAPSHOT.jar --spring.cloud.stream.bindings.input.destination=customers.output.feedback.summary --spring.cloud.stream.bindings.output.destination=customers.output.feedback.sentiment
```

See [CustomerFeedbackSentimentProcessor.java](../applications/processors/ai-sentiment-processor/src/main/java/ai/data/pipeline/sentiment/processor/CustomerFeedbackSentimentProcessor.java)

See [CustomerFeedback.java](../applications/processors/ai-sentiment-processor/src/main/java/ai/data/pipeline/sentiment/domains/CustomerFeedback.java)
See [FeedbackSentiment.java](../applications/processors/ai-sentiment-processor/src/main/java/ai/data/pipeline/sentiment/domains/FeedbackSentiment.java)

Added support for OLLAMA
[pom.xml](../applications/processors/ai-sentiment-processor/pom.xml)


Start Postgres Sink 


```shell
java -jar applications/sinks/postgres-sink/target/postgres-sink-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.password=postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.url="jdbc:postgresql://localhost/postgres"  --spring.cloud.stream.bindings.input.destination=customers.output.feedback.sentiment --spring.config.import=optional:file://$PWD/applications/sinks/postgres-sink/src/main/resources/postgres-sentiment-analysis-ollama.yml --spring.cloud.stream.bindings.input.group=postgres-sink
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


```shell
curl -X 'POST' \
  'http://localhost:8094/feedback' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id" : "F002",
  "email" : "jmatthews@email",
  "feedback" : "I am really disappointed with the wait time I experienced when trying to reach Customer Service. I was on hold for over 40 minutes just to speak with someone about a simple issue with my account. It’s frustrating and honestly unacceptable. If your company values customer satisfaction, you seriously need to hire more reps or improve your response time. I do not have time to sit around waiting all day."
}'
```


```shell
curl -X 'POST' \
  'http://localhost:8094/feedback' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id" : "F003",
  "email" : "jmatthews@email",
  "feedback" : "I just wanted to take a moment to recognize the exceptional professionalism of your customer service team. The representative I spoke with was courteous, knowledgeable, and incredibly patient while helping me resolve my issue. It’s rare to find such a high level of service these days, and it truly made a difference in my experience. Kudos to your team!"
}'
```


In psql

```sql
 select sentiment,summary from customer.feedback;
```
