


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

Text Summarization

```shell
SELECT pgml.transform(
        task => '{
          "task": "summarization", 
          "model": "google/pegasus-xsum"
    }'::JSONB,
        inputs => array[
         'I called into the customer support. First of all, I have been a very long customer. I have been faithful and often. I am not appreciative of the level of service that I get. I cannot get access to my information. I just called them to try to get my issue resolved. I just got redirected over and over again. Your system needs to be fixed. I am not happy. I want my money back.'
        ]
);
```


Text Classification


```shell
SELECT pgml.transform( task   => 'text-classification', inputs => ARRAY['I love how amazingly simple ML has become!']) AS positivity;
```

Text classification

```shell
select positivity->0->'label' from (SELECT pgml.transform( task   => 'text-classification', inputs => ARRAY['I hate doing mundane and thankless tasks. ☹️']) as positivity) positivity;
```
