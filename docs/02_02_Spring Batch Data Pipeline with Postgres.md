docker network  rm data-pipelines

```shell
docker network create data-pipelines
```

Start Postgres

```shell
docker run --name postgres --network data-pipelines --rm  \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=postgres \
  -p 5432:5432 \
  -it postgres 
```


Access Postgres using psql

```shell
docker exec -it postgres psql -U postgres
```
 

list tables in postgres

```psql
\dt *
```
[schema-postgres.sql](../applications/batching/customer-batch/src/main/resources/schema-postgres.sql)


See

[BatchConfig.java](../applications/batching/customer-batch/src/main/java/ai/data/pipeline/spring/customer/BatchConfig.java)


See CSV

[customers-source.csv](../applications/batching/customer-batch/src/test/resources/sources/customers-source.csv)


See

[Customer.java](../applications/batching/customer-batch/src/main/java/ai/data/pipeline/spring/customer/domain/Customer.java)


See

[CustomerFieldMapper.java](../applications/batching/customer-batch/src/main/java/ai/data/pipeline/spring/customer/mapper/CustomerFieldMapper.java)

See 

[MissingRequiredFieldsFilterProcessor.java](../applications/batching/customer-batch/src/main/java/ai/data/pipeline/spring/customer/processor/MissingRequiredFieldsFilterProcessor.java)



```shell
mvn package
```


Run batch

```shell
java -jar applications/batching/customer-batch/target/customer-batch-0.0.1-SNAPSHOT.jar  --spring.datasource.password=postgres --source.input.file.csv="file:./applications/batching/customer-batch/src/test/resources/sources/customers-source.csv" --processor.output.error.file.csv="file:./runtime/invalid_customers.csv"
```


In Psql
```shell
select * from customer.customers;
```




Also see

[invalid_customers.csv](../runtime/invalid_customers.csv)

Find records in source

