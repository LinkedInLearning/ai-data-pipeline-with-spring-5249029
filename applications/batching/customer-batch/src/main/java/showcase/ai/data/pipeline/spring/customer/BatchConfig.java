package showcase.ai.data.pipeline.spring.customer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import showcase.ai.data.pipeline.spring.customer.domain.Customer;
import showcase.ai.data.pipeline.spring.customer.mapper.CustomerFieldMapper;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration(exclude = {BatchAutoConfiguration.class})
@Slf4j
public class BatchConfig {

    @Value("${spring.batch.chuck.size:10}")
    private int chunkSize;

    //INSERT INTO customer.customers (first_name, last_name,email) VALUES (:firstName, :lastName, :contact.email)
    private static final String saveSql = """
        insert into customer.customers(email,first_name,last_name,phone,address,city,state,zip) 
        values (:contact.email,
                :firstName,
                :lastName,
                :contact.phone, 
                :location.address,
                :location.city,
                :location.state,
                :location.zip) 
        on CONFLICT (email) 
        DO UPDATE SET first_name = :firstName, 
                last_name = :lastName,  
                phone   = :contact.phone, 
                address = :location.address, 
                city    = :location.city, 
                state   = :location.state, 
                zip     = :location.zip
    """;

    @Value("${source.input.file.csv}")
    private Resource customerInputResource;

    private final static String jobName = "load-customer";

    /**
     * Create a repository implementation that does not save batch information to the database.
     * This is used to simplify this example. Note: Saving information such as the status of the tables
     * is recommended for production use.
     *
     * @return the job repository
     */
    @Bean
    ResourcelessJobRepository resourcelessJobRepository()
    {
        return new ResourcelessJobRepository();

    }

    /**
     *
     * @param jobRepository the job
     * @param taskExecutor the task executor
     * @return the job launch
     */
    @Bean
    public JobLauncher batchJobLauncher(@Qualifier("resourcelessJobRepository") JobRepository jobRepository,
                                   TaskExecutor taskExecutor) {
        var jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(taskExecutor);
        return jobLauncher;
    }

    @Bean
    public Job job(JobRepository jobRepository,
                   Step step){

        return new JobBuilder(jobName+System.currentTimeMillis(),jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step).end().build();
    }



    @Bean
    public FlatFileItemReader<Customer> reader(CustomerFieldMapper mapper) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .resource(customerInputResource)
                .delimited()
                .names("id","firstName", "lastName","email"
                        ,"phone","address","city","state"
                        ,"zip"
                        )
                .fieldSetMapper(mapper)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Customer> writer(DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Customer>()
                .sql(saveSql)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Step loadCustomerStep(ItemReader<Customer> itemReader,
                                 ItemProcessor<Customer, Customer> processor,
                                 ItemWriter<Customer> writer,
                                 JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
        return new StepBuilder("loadCustomerStep", jobRepository)
                .<Customer, Customer>chunk(chunkSize,transactionManager)
                .reader(itemReader)
                .processor(processor)
                .writer(writer)
                .build();
    }


}
