package ai.data.pipeline.spring.customer;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the command line runner that execute the job
 * when the Spring Boot application is started.
 *
 * @author Gregory Green
 */
@Configuration
public class CommandLineConfig {


    /**
     * Construct the command liner runner
     * @param jobLauncher the job lancher
     * @param job the Spring Batch job to start
     * @return the line runner
     */
    @Bean
    CommandLineRunner jobRunner(@Qualifier("batchJobLauncher") JobLauncher jobLauncher, Job job){
        return args -> jobLauncher.run(job, new JobParametersBuilder().addJobParameter("time",System.currentTimeMillis()+"", String.class)
                .toJobParameters());
    }
}
