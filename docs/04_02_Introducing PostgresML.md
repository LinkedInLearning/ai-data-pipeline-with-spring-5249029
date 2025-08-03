
Start PostgresML


```shell
docker run --rm  \
    -it \
    -v postgresml_data:/var/lib/postgresql \
    -p 5433:6432 \
    -p 8000:8000 \
    ghcr.io/postgresml/postgresml:2.10.0 \
    sudo -u postgresml psql -d postgresml
```



--------------------


Test summary in postgresML



```sql
SELECT pgml.transform( task => '{ "task": "summarization", "model": "Falconsai/text_summarization"}'::JSONB, inputs => array[ 'I am really disappointed with the wait time I experienced when trying to reach Customer Service. I was on hold for over 40 minutes just to speak with someone about a simple issue with my account. It’s frustrating and honestly unacceptable. I do not have time to sit around waiting all day.'])::json->0->>'summary_text' as summary_text;
```



Text Classification

```shell
SELECT pgml.transform( task   => 'text-classification', inputs => ARRAY['I love building linked Learning courses!']) AS positivity;
```

Text classification

```shell
select positivity->0->'label' from (SELECT pgml.transform( task   => 'text-classification', inputs => ARRAY['What are these prices so high']) as positivity) positivity;
```
