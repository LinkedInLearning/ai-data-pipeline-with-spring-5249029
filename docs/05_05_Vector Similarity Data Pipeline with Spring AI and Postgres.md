# Setup
```shell
docker volume rm  postgresml_data
```


Run Rabbit


```shell
docker run -it --name rabbitmq   --rm  -p 5672:5672 -p 15672:15672  rabbitmq:4.1.0-management 
```


Run Postgres

```shell
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

I will create customer_similarities table.

```shell
create  schema  if not exists customer ;

create table customer.customer_similarities(
    customer_id text NOT NULL,
    similarities jsonb NOT NULL,
 PRIMARY KEY (customer_id)
);
```

Here  I am using a similarites column with a special data type column.
In the previous example, I was able to parse the JSON to store into invidual column (such as the email, first and last name).

In this case, I wanted to show you that you can just store json natively into Postgres
using the JSONB data type.


Run PostgresML with PgVector

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


```sql
CREATE EXTENSION vector;
```


Here is an example similar search of a perfect match between 2 identical vectors

```sql
SELECT 1- ('[1, 0, 0]' <=> '[1, 0, 0]')::float AS cosine_distance;
```
- The <=> is a special syntax used by pgvector to apply the law of cosines
- ::float converts the cosine results to a float


Here is an example of not an exact match, but very similar vectors

```sql
SELECT 1- ('[1, 1, 0]' <=> '[1, 1, 0.5]')::float AS cosine_distance;
```

Here is one more Example pf 2 vector embeddings that are opposite of each other

```sql
SELECT 1- ('[1, 1, 1]' <=> '[-1, -1, -1]')::float AS cosine_distance;
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

See [EmbeddingSimilarityFunction.java](../applications/processors/postgres-embedding-similarity-processor/src/main/java/ai/data/pipeline/postgres/embedding/function/EmbeddingSimilarityFunction.java)
- It is provided with a vector store that uses Postgres with the pgvector extension
- It using an object to convert the payload to a Spring AI Document object
  - See [PayloadToDocument.java](../applications/processors/postgres-embedding-similarity-processor/src/main/java/ai/data/pipeline/postgres/embedding/conversion/PayloadToDocument.java)
    - fieldName text fields names are passed in a runtime.
    - So the vector save to fields such as email,phone,zip,state,city,address,lastName,firstName that a parsed from the JSON payload
  - The processor then builds the search criteria using the Spring AI abstraction. 
  - This results the a limited number of "top" or best match results 
  - Based on the customer information 
  - I set a threshold, for example the match distance must be greater than 0.90 
  - The list of results are converted to JSON 
  - and returned the sink using RabbitMQ


- See [SimilarDocuments.java](../applications/processors/postgres-embedding-similarity-processor/src/main/java/ai/data/pipeline/postgres/embedding/domain/SimilarDocuments.java)



Start Sink


```shell
java -jar applications/sinks/postgres-sink/target/postgres-sink-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres  --spring.datasource.password=postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.url="jdbc:postgresql://localhost/postgres"  --spring.cloud.stream.bindings.input.destination="customers.similarities.output" --spring.config.import=optional:file://$PWD/applications/sinks/postgres-sink/src/main/resources/postgres-similarity.yml --spring.cloud.stream.bindings.input.group=postgres-sink
```

See [postgres-similarity.yml](../applications/sinks/postgres-sink/src/main/resources/postgres-similarity.yml)

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

----------------------
In psql

Now lets look at the results in  customer similarities table. 

```sql
select *
from customer.customer_similarities;
```


The sink stores the similarities as a JSON array.
If needed, I can use Postgres parse the records.

```sql
select customer_id,
 jsonb_array_elements(similarities) ->>'id' as email, 
 jsonb_array_elements(similarities) ->>'text' as text,
 jsonb_array_elements(similarities) ->>'score' as score,
 (jsonb_array_elements(similarities) ->>'metadata')::json ->> 'distance' as distance
from customer.customer_similarities;
```

The jsonb_array_elements function parse JSON array fields.
So I can select the individual fields such as text and score from the JSONB column.
Which is a nicer format


The records in PostgresML vector_store database table are used by the processor search for duplicate records
based on matching similaries.

```sql
select id,content from public.vector_store ;
```

Any additional customer details submitted to the data pipeline will check for matches in this table.
Spring AI along with Postgres as a vector database hides the complexity of finding duplicate records.


