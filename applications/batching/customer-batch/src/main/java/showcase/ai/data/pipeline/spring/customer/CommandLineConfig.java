package showcase.ai.data.pipeline.spring.customer;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandLineConfig {


    @Bean
    CommandLineRunner jobRunner(@Qualifier("batchJobLauncher") JobLauncher jobLauncher, Job job){
        return args -> jobLauncher.run(job, new JobParametersBuilder().addJobParameter("time",System.currentTimeMillis()+"", String.class)
                .toJobParameters());
    }
}
