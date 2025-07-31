# Prequisite

```shell
docker network create data-pipelines
```

- Run RabbitMQ
```shell
docker run -it --name rabbitmq   --rm  -p 5672:5672 -p 15672:15672  rabbitmq:4.1.0-management 
```

```shell
docker run -it --name postgresql  --network data-pipelines --rm  -e POSTGRESQL_USERNAME=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres
```


psql 

```shell
docker run --name psql -it --rm \
--network data-pipelines \
    postgres psql -h postgresql -U postgres
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

Build application

```shell
mvn package
```

```shell
java -jar applications/sinks/postgres-sink/target/postgres-sink-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.password=postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.url="jdbc:postgresql://localhost/postgres" --sql.consumer.sql="insert into customer.customers(email,first_nm,last_nm,phone,address,city,state,zip) values (:email,:firstName,:lastName,:phone, :address,:city,:state,:zip) on CONFLICT (email) DO UPDATE SET first_nm = :firstName, last_nm = :lastName,  phone = :phone, address = :address, city = :city, state = :state, zip = :zip" --spring.cloud.stream.bindings.input.destination=customers.intake
```



## Testing


```shell
curl -X 'POST' \
  'http://localhost:8080/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "email" : "email@email",
  "firstName" : "Josiah",
  "lastName" : "Imani",
  "phone" : "555-555-5555",
  "address" : "12 Straight St",
  "city" : "gold",
  "state": "ny",
  "zip": "55555"
}'
```

```json
{
  "email" : "email@email",
  "firstName" : "Josiah",
  "lastName" : "Imani",
  "phone" : "555-555-5555",
  "address" : "12 Straight St",
  "city" : "gold",
  "state": "ny",
  "zip": "55555"
}

```


In psql 

```sql
select * from customer.customers;

```


```shell
curl -X 'POST' \
  'http://localhost:8080/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "firstName" : "Jill",
  "lastName" : "Smith",
  "email" : "jsmith@email",
  "phone" : "155-555-5555",
  "address" : "2 Straight St",
  "city" : "gold",
  "state": "ny",
  "zip": "55555"
}'
```
```sql
select * from customer.customers;
```


Update Jill's phone

```shell
curl -X 'POST' \
  'http://localhost:8080/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "firstName" : "Jill",
  "lastName" : "Smith",
  "email" : "jsmith@email",
  "phone" : "222-222-2222",
  "address" : "2 Straight St",
  "city" : "gold",
  "state": "ny",
  "zip": "55555"
}'
```

```sql
select * from customer.customers;

```

```shell
curl -X 'POST' \
  'http://localhost:8080/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "firstName" : "Jack",
  "lastName" : "Smith",
  "email" : "jacksmith@email",
  "phone" : "255-555-5555",
  "address" : "255 Straight St",
  "city" : "gold",
  "state": "ny",
  "zip": "55555"
}'
```

```sql
select * from customer.customers;
```
Change Jack Smith Information: ex: address 333 Straight St"

```shell
curl -X 'POST' \
  'http://localhost:8080/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "firstName" : "Jack",
  "lastName" : "Smith",
  "email" : "jacksmith@email",
  "phone" : "255-555-5555",
  "address" : "333 Straight St",
  "city" : "silver",
  "state": "ny",
  "zip": "23232"
}'
```


```sql
select * from customer.customers;
```