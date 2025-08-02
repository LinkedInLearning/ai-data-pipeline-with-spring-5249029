Run Postgres

```shell
docker run --name postgresql --network data-pipelines --rm  -e POSTGRES_USERNAME=postgres -e POSTGRES_PASSWORD=postgres  -e POSTGRESQL_DATABASE=postgres -p 5432:5432 postgres:latest 
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

Test summary in postgresML



```sql
SELECT pgml.transform( task => '{ "task": "summarization", "model": "Falconsai/text_summarization"}'::JSONB, inputs => array[ 'I am really disappointed with the wait time I experienced when trying to reach Customer Service. I was on hold for over 40 minutes just to speak with someone about a simple issue with my account. It’s frustrating and honestly unacceptable. I do not have time to sit around waiting all day.'])::json->0->>'summary_text' as summary_text;
```




Connect to postgres
```shell
docker run --name psql-pg -it --rm \
--network data-pipelines postgres:latest psql -h postgresql  -U postgres -d postgres
```


```sql

create schema if not exists customer;

create table customer.feedback(
feed_id text NOT NULL,
email text NOT NULL,
user_feedback text NOT NULL,
summary text NOT NULL,
 PRIMARY KEY (feed_id)
);
```


---------------------------




Start Http


```shell
java -jar runtime/http-source-rabbit-5.0.1.jar --http.supplier.pathPattern=feedback --server.port=8093 --spring.cloud.stream.bindings.output.destination=customers.input.feedback
```




Start Processor text summarization

```shell
java -jar applications/processors/postgres-query-processor/target/postgres-query-processor-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml" --spring.datasource.driverClassName=org.postgresql.Driver --spring.cloud.stream.bindings.input.destination=customers.input.feedback --spring.cloud.stream.bindings.output.destination=customers.output.feedback --spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipeline-with-spring-showcase/applications/processors/postgres-query-processor/src/main/resources/text-summarization.yml --spring.datasource.hikari.max-lifetime=600000 --spring.cloud.stream.bindings.input.group=postgres-query-processor
```

Start Sink


```shell
java -jar applications/sinks/postgres-sink/target/postgres-sink-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.password=postgres --spring.datasource.url="jdbc:postgresql://localhost/postgres"  --spring.cloud.stream.bindings.input.destination=customers.output.feedback --spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipeline-with-spring-showcase/applications/sinks/postgres-sink/src/main/resources/postgres-text-summarization.yml --spring.cloud.stream.bindings.input.group=postgres-sink
```



```shell
curl -X 'POST' \
  'http://localhost:8093/feedback' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id" : "F001",
  "email" : "jmatthews@email",
  "feedback" : "I am really disappointed with the wait time I experienced when trying to reach Customer Service. I was on hold for over 40 minutes just to speak with someone about a simple issue with my account. It’s frustrating and honestly unacceptable. If your company values customer satisfaction, you seriously need to hire more reps or improve your response time. I do not have time to sit around waiting all day."
}'
```


```shell
curl -X 'POST' \
  'http://localhost:8093/feedback' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id" : "F002",
  "email" : "jmatthews@email",
  "feedback" : "I just wanted to take a moment to recognize the exceptional professionalism of your customer service team. The representative I spoke with was courteous, knowledgeable, and incredibly patient while helping me resolve my issue. It’s rare to find such a high level of service these days, and it truly made a difference in my experience. Kudos to your team!"
}'
```


```shell
curl -X 'POST' \
  'http://localhost:8093/feedback' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id" : "F003",
  "email" : "jmatthews@email",
  "feedback" : "I am getting really frustrated with having to repeat who I am and explain my issue every time I am transferred to another representative. It is like no one talks to each other or takes notes. I had to give my name, account number, and explain the entire problem three different times during one call. It’s exhausting and makes the whole experience feel disorganized. There has to be a better way to handle this"
}'
```


In psql

```sql
select feed_id,summary from customer.feedback;
```