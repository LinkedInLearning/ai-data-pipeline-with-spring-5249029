Run Rabbit

```shell
docker network create data-pipeline
```

```shell
docker run -it --name rabbitmq   --rm  -p 5672:5672 -p 15672:15672  rabbitmq:4.1.0-management 
```


Run Postgres

```shell
```shell
docker run --name postgresql --network data-pipeline --rm  -e POSTGRESQL_USERNAME=postgres -e ALLOW_EMPTY_PASSWORD=true -e POSTGRESQL_DATABASE=postgres -p 5432:5432 bitnami/postgresql:latest 
```

```shell
docker exec -it postgresql psql -U postgres
```


```shell
create  schema  if not exists customer ;

create table customer.customer_similarities(
    customer_id text NOT NULL,
    similarities jsonb NOT NULL,
 PRIMARY KEY (customer_id)
);
```


Run PostgresML

```shell
docker run --rm --name postgresml \
    -it \
    --network data-pipeline  \
    -v postgresml_data:/var/lib/postgresql \
    -p 6432:5432 \
    -p 8000:8000 \
    ghcr.io/postgresml/postgresml:2.10.0 \
    sudo -u postgresml psql -d postgresml
```


```shell
docker run --name psql-ml -it --rm \
--network data-pipeline \
    bitnami/postgresql:latest psql -h postgresml  -U postgres -d postgresml
```

Distance 0

```sql
SELECT '[1, 0, 0]' <=> '[1, 0, 0]' AS cosine_distance;
```

Non Zero Distance
```sql
SELECT '[1, 1, 0]' <=> '[1, 0, 0]' AS cosine_distance;
```



---------------------------


Start Http

```shell
java -jar runtime/http-source-rabbit-5.0.1.jar --http.supplier.pathPattern=customers --server.port=8095 --spring.cloud.stream.bindings.output.destination=customers.similarities.input
```


Start similarity processor

```shell
java -jar applications/processors/postgres-embedding-similarity-processor/target/postgres-embedding-similarity-processor-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml" --spring.datasource.driverClassName=org.postgresql.Driver --spring.cloud.stream.bindings.input.destination=customers.similarities.input --spring.cloud.stream.bindings.output.destination=customers.similarities.output --embedding.similarity.processor.topK=3 --embedding.similarity.processor.similarityThreshold="0.90" --embedding.similarity.processor.documentTextFieldNames="email,phone,zip,state,city,address,lastName,firstName" --spring.datasource.hikari.max-lifetime=600000 --spring.cloud.stream.bindings.input.group=postgres-query-processor
```





Start Sink


```shell
java -jar applications/sinks/postgres-sink/target/postgres-sink-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.url="jdbc:postgresql://localhost/postgres"  --spring.cloud.stream.bindings.input.destination="customers.similarities.output" --spring.config.import=optional:file://$PWD/applications/sinks/postgres-sink/src/main/resources/postgres-similarity.yml --spring.cloud.stream.bindings.input.group=postgres-sink
```

```shell
curl -X 'POST' \
  'http://localhost:8095/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
                  "id" : "email@email",
                  "firstName" : "Josiah",
                  "lastName" : "Imani",
                  "email" : "email@email",
                  "phone" : "555-555-5555",
                  "address" : "12 Straight St",
                  "city" : "gold",
                  "state" : "ny",
                  "zip": "55555"
                }'
```


In psql

```sql
select customer_id,
 jsonb_array_elements(similarities) ->>'id' as email, 
 jsonb_array_elements(similarities) ->>'text' as text,
 jsonb_array_elements(similarities) ->>'score' as score,
 (jsonb_array_elements(similarities) ->>'metadata')::json ->> 'distance' as distance
from customer.customer_similarities;
```


```shell
curl -X 'POST' \
  'http://localhost:8095/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '               {
                  "id" : "duplicate1@email",
                  "firstName" : "Josiah",
                  "lastName" : "Imani",
                  "email" : "duplicate1@email",
                  "phone" : "555-555-5555",
                  "address" : "12 Straight St",
                  "city" : "gold",
                  "state" : "ny",
                  "zip": "55555"
                }'
```


In PostgresML

```sql
select id,content from public.vector_store ;
```


```json
{"id":"duplicate1@email","similaritiesPayload":"[{\"id\":\"duplicate@email\",\"text\":\"duplicate@email,555-555-5555,55555,ny,gold,12 Straight St,Imani,Josiah\",\"media\":null,\"metadata\":{\"distance\":0.001647779},\"score\":0.9983522209804505},{\"id\":\"duplicate5@email\",\"text\":\"duplicate5@email,555-555-5555,55555,ny,gold,12 Straight St,Imani,Josiah\",\"media\":null,\"metadata\":{\"distance\":0.001648155},\"score\":0.998351844958961}]"}
```


```json
               {
                  "id" : "email@email",
                  "firstName" : "Josiah",
                  "lastName" : "Imani",
                  "email" : "email@email",
                  "phone" : "555-555-5555",
                  "address" : "12 Straight St",
                  "city" : "gold",
                  "state" : "ny",
                  "zip": "55555"
                }
```




```sql
insert into customer.customer_similarities
        (
          customer_id,
          similarities )
      values (
          'dsds',
          '{"name": "Laptop", "price": 999.99, "specs": {"cpu": "i7", "ram": "16GB"}}'
          ); 
```



```json
{"id":"email@email","similarities":[{"id":"8df15279-97a6-4b48-92f3-f78d045d9cc4","text":"email@email,555-555-5555,55555,ny,gold,12 Straight St,Imani,Josiah","score":1.0},{"id":"duplicate@email","text":"duplicate@email,555-555-5555,55555,ny,gold,12 Straight St,Imani,Josiah","score":0.9934384510852396}]}
```


```json
{"id":"email@email","similarities": {"id" :  "testing"}}
```


