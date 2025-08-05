
# Start RabbitMQ


- Run RabbitMQ 
```shell
docker run -it --name rabbitmq   --rm  -p 5672:5672 -p 15672:15672  rabbitmq:4.1.0-management 
```


```shell
open http://localhost:15672
```

Download http source

```shell
wget -P runtime https://repo.maven.apache.org/maven2/org/springframework/cloud/stream/app/http-source-rabbit/5.0.1/http-source-rabbit-5.0.1.jar
```

Run the Http Source 
```shell
java -jar runtime/http-source-rabbit-5.0.1.jar --http.supplier.pathPattern=customers --server.port=8080 --spring.cloud.stream.bindings.output.destination=customers.intake
```



Create queue

```shell
docker exec -it rabbitmq rabbitmqadmin declare queue name=customer-test
```

Create Binding

```shell
docker exec -it rabbitmq  rabbitmqadmin declare binding source=customers.intake destination=customer-test routing_key=#
```


## Testing

Example 

```json
{
  "firstName" : "Josiah",
  "lastName" : "Imani",
  "email" : "email@email",
  "phone" : "555-555-5555",
  "address" : "12 Straight St",
  "city" : "Erie",
  "zip": "16510"
}
```



```shell
curl -X 'POST' \
  'http://localhost:8080/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "firstName" : "Juan",
  "lastName" : "Wagner",
  "email" : "jwagner79@ihg.com",
  "phone" : "555-555-5555",
  "address" : "1496 Reinke Lane",
  "city" : "New York City",
  "state" "NY",
  "zip": "10275"
}'
```
