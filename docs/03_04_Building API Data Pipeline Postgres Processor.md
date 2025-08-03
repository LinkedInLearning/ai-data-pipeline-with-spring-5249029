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


create table customer.phone_campaigns(
phone text NOT NULL,
last_nm text  NOT NULL,
first_nm text  NOT NULL,
email text NOT NULL,
 PRIMARY KEY (phone)
);
```

In Psql

```shell
insert into customer.phone_campaigns(phone,first_nm,last_nm, email) values('555-555-5551','John','Matthews','jmatthews@email');
insert into customer.phone_campaigns(phone,first_nm,last_nm, email) values('555-555-5552','Marcy','Love','mlove@email');
```



```sql
SELECT  (CASE WHEN LENGTH(cust.last_nm) > 0  THEN cust.last_nm ELSE pc.last_nm END) as lastname, 
        (CASE WHEN LENGTH(cust.first_nm) > 0  THEN cust.first_nm ELSE pc.first_nm END) as firstname, 
        (CASE WHEN LENGTH(cust.email) > 0  THEN cust.last_nm ELSE pc.email END) as email, 
        cust.phone as phone, 
        cust.address as address, 
        cust.city as city, 
        cust.state as state, 
        cust.zip as zip 
FROM 
    (select 
        'Entered last name' as last_nm, 
        'Entered first Name' as first_nm, 
        'Entered email' as email, 
        '555-555-5551' as phone, 
        'Entered address' as address, 
        'Entered city' as city, 
        'Entered state' as state, 
        'Entered zip' as zip ) cust 
LEFT JOIN 
    (select last_nm, first_nm, email, phone  from customer.phone_campaigns)  pc 
ON cust.phone = pc.phone;
```


From Phone Campaign

```sql
SELECT  (CASE WHEN LENGTH(cust.last_nm) > 0  THEN cust.last_nm ELSE pc.last_nm END) as lastname, 
        (CASE WHEN LENGTH(cust.first_nm) > 0  THEN cust.first_nm ELSE pc.first_nm END) as firstname, 
        (CASE WHEN LENGTH(cust.email) > 0  THEN cust.last_nm ELSE pc.email END) as email, 
        cust.phone as phone, 
        cust.address as address, 
        cust.city as city, 
        cust.state as state, 
        cust.zip as zip 
FROM 
    (select 
        '' as last_nm, 
        '' as first_nm, 
        '' as email, 
        '555-555-5551' as phone, 
        'Entered address' as address, 
        'Entered city' as city, 
        'Entered state' as state, 
        'Entered zip' as zip ) cust 
LEFT JOIN 
    (select last_nm, first_nm, email, phone  from customer.phone_campaigns)  pc 
ON cust.phone = pc.phone;

```

==================================


Start Http


```shell
java -jar runtime/http-source-rabbit-5.0.1.jar --http.supplier.pathPattern=customers --server.port=8091 --spring.cloud.stream.bindings.output.destination=customers.input.formatting
```


Start Processor customer formatting from a marketing campaign

```shell
 java -jar applications/processors/postgres-query-processor/target/postgres-query-processor-0.0.1-SNAPSHOT.jar --query.processor.sql="select  (CASE WHEN LENGTH(cust.last_nm) > 0  THEN cust.last_nm ELSE pc.last_nm END) as lastname, (CASE WHEN LENGTH(cust.first_nm) > 0  THEN cust.first_nm ELSE pc.first_nm END) as firstname, (CASE WHEN LENGTH(cust.email) > 0  THEN cust.last_nm ELSE pc.email END) as email, cust.phone as phone, cust.address as address, cust.city as city, cust.state as state, cust.zip as zip from (select :lastname as last_nm, :firstname as first_nm, :email as email, :phone as phone, :address as address, :city as city, :state as state, :zip as zip ) cust LEFT JOIN (select last_nm, first_nm, email, phone  from customer.phone_campaigns)  pc ON cust.phone = pc.phone" --spring.datasource.username=postgres --spring.datasource.password=postgres --spring.datasource.url="jdbc:postgresql://localhost/postgres" --spring.datasource.driverClassName=org.postgresql.Driver --spring.cloud.stream.bindings.input.destination=customers.input.formatting --spring.cloud.stream.bindings.output.destination=customers.output.formatting
```

Start Sink


```shell
java -jar applications/sinks/postgres-sink/target/postgres-sink-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.password=postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.url="jdbc:postgresql://localhost/postgres" --sql.consumer.sql="insert into customer.customers(email,first_nm,last_nm,phone,address,city,state,zip) values (:email,:firstname,:lastname,:phone, :address,:city,:state,:zip) on CONFLICT (email) DO UPDATE SET first_nm = :firstname, last_nm = :lastname,  phone = :phone, address = :address, city = :city, state = :state, zip = :zip" --spring.cloud.stream.bindings.input.destination=customers.output.formatting
```




```shell
curl -X 'POST' \
  'http://localhost:8091/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
    "email" : "",
  "firstname" : "",
  "lastname" : "",
  "phone" : "555-555-5551",
  "address" : "55 Straight St",
  "city" : "Richmond",
  "state": "VA",
  "zip": "23225"
}'
```


```shell
curl -X 'POST' \
  'http://localhost:8091/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "email" : "",
  "firstname" : "",
  "lastname" : "Love",
  "phone" : "555-555-5552",
  "address" : "452 Parkside Lane",
  "city" : "Denver",
  "state": "CO",
  "zip": "80235"
}'
```


In psql

```sql
select * from customer.customers;

```
