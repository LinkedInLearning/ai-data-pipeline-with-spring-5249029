
Start PostgresML


```shell
docker run --rm  \
    -it \
    -v postgresml_data:/var/lib/postgresql \
    -p 6432:6432 \
    ghcr.io/postgresml/postgresml:2.10.0 \
    sudo -u postgresml psql -d postgresml
```



--------------------


Test summary in postgresML


```sql
SELECT pgml.transform( task => '{ "task": "summarization", "model": "Falconsai/text_summarization"}'::JSONB, inputs => array[ 'I am really disappointed with the wait time I experienced when trying to reach Customer Service. I was on hold for over 40 minutes just to speak with someone about a simple issue with my account. It’s frustrating and honestly unacceptable. I do not have time to sit around waiting all day.'])::json->0->>'summary_text' as summary_text;
```


```sql
SELECT pgml.transform( task => '{ "task": "summarization", "model": "Falconsai/text_summarization"}'::JSONB, inputs => array[ 'I have been using this system for a while now, and I have to say, I am genuinely impressed with how well it performs. The user interface is clean and thoughtfully laid out, making navigation feel effortless even for someone new to it. Features are logically organized, and everything just works seamlessly right out of the box. What really stands out, though, is the attention to detail—the developers clearly put a lot of thought into the user experience. Whether it is the speed of execution, the minimal learning curve, or the helpful tooltips and documentation, everything contributes to a feeling of confidence and ease. It is rare to come across a system that feels both powerful and user-friendly, but this one hits that balance perfectly.'])::json->0->>'summary_text' as summary_text;
```




Text Classification


```shell
SELECT pgml.transform( task   => 'text-classification', inputs => ARRAY['I was on hold for over 40 minutes just to speak with someone about a simple issue with my account . I do not have time to sit around waiting all day.']) AS positivity;
```


```shell
SELECT pgml.transform( task   => 'text-classification', inputs => ARRAY['the user interface is clean and thoughtfully laid out, making navigation feel effortless even for someone new to it . The developers clearly put a lot of thought into the user experience . It is rare to come across a system that feels both powerful and user-friendly, but this one hits that balance perfectly.']) AS positivity;
```


```shell
SELECT pgml.transform( task   => 'text-classification', inputs => ARRAY['I love building linked Learning courses with my producer Dione!!!']) AS positivity;
```



