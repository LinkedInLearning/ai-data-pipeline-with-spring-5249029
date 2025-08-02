# Prequisite

```shell
docker network create data-pipelines
```

- Run RabbitMQ
```shell
docker run -it --name rabbitmq   --rm  -p 5672:5672 -p 15672:15672  rabbitmq:4.1.0-management 
```

Postgres
```shell
docker run --name postgresql --network data-pipelines --rm  -e POSTGRESQL_USERNAME=postgres -e ALLOW_EMPTY_PASSWORD=true -e POSTGRESQL_DATABASE=postgres -p 5432:5432 bitnami/postgresql:latest 
```


```shell
docker run --name psql -it --rm \
--network data-pipelines \
    bitnami/postgresql:latest psql -h postgresql -U postgres
```


```sql

create schema customer;

create table customer.customers(
first_nm text NOT NULL,
last_nm text  NOT NULL,
email text NOT NULL,
phone text ,
address text NOT NULL,
city text ,
state text ,
zip text NOT NULL,
 PRIMARY KEY (email)
);

```
==================================


Start Http


```shell
java -jar runtime/http-source-rabbit-5.0.1.jar --http.supplier.pathPattern=customers --server.port=8091 --spring.cloud.stream.bindings.output.destination=customers.input.formatting
```


Start Processor

```shell
java -jar applications/processors/postgres-query-processor/target/postgres-query-processor-0.0.1-SNAPSHOT.jar --query.processor.sql="select :email as email,initcap(:firstname) as firstname,initcap(:lastname) as lastname,:phone as phone,:address as address,:city as city,:state as state,:zip as zip" --spring.datasource.username=postgres --spring.datasource.url="jdbc:postgresql://localhost/postgres" --spring.datasource.driverClassName=org.postgresql.Driver --spring.cloud.stream.bindings.input.destination=customers.input.formatting --spring.cloud.stream.bindings.output.destination=customers.output.formatting
```

Start Sink


```shell
java -jar applications/sinks/postgres-sink/target/postgres-sink-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.url="jdbc:postgresql://localhost/postgres" --sql.consumer.sql="insert into customer.customers(email,first_nm,last_nm,phone,address,city,state,zip) values (:email,:firstname,:lastname,:phone, :address,:city,:state,:zip) on CONFLICT (email) DO UPDATE SET first_nm = :firstname, last_nm = :lastname,  phone = :phone, address = :address, city = :city, state = :state, zip = :zip" --spring.cloud.stream.bindings.input.destination=customers.output.formatting
```




```shell
curl -X 'POST' \
  'http://localhost:8091/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "email" : "jmatthews@email",
  "firstname" : "Lonny",
  "lastname" : "matthews",
  "phone" : "555-555-5555",
  "address" : "55 Straight St",
  "city" : "gold",
  "state": "ny",
  "zip": "55555"
}'
```


In psql

```sql
select * from customer.customers;

```