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

```shell
docker exec -it postgres psql -U postgres
```


Run batch

```shell
java -jar applications/batching/customer-batch/target/customer-batch-0.0.1-SNAPSHOT.jar  --spring.datasource.password=postgres --source.input.file.csv="file:./applications/batching/customer-batch/src/test/resources/sources/customers-source.csv" --processor.output.error.file.csv="file:./runtime/invalid_customers.csv"
```


In Psql
```shell
select * from customer.customers;
```