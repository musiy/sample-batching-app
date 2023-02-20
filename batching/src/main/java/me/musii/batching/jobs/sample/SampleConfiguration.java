package me.musii.batching.jobs.sample;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.listener.JobParameterExecutionContextCopyListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
public class SampleConfiguration {

    /**
     * Sample no op step revealing the principles of data processing
     */
    @Bean
    public Step noOpStep(JobRepository jobRepository,
                         PlatformTransactionManager transactionManager) {

        SampleItemStream sampleItemStream = new SampleItemStream(10); // todo
        return new StepBuilder("noOpStep", jobRepository)
                .<String, String>chunk(2, transactionManager) // todo chunkSize??
                .allowStartIfComplete(true)
                .reader(sampleItemStream)
                .writer(sampleItemStream)
                .listener(new StepExecutionListener() {

                    public void beforeStep(StepExecution stepExecution) {
                        ExecutionContext executionContext = stepExecution.getExecutionContext();
                        stepExecution.getJobExecution().getExecutionContext().entrySet()
                                .forEach(entry -> executionContext.put(entry.getKey(), entry.getValue()));
                    }
                })
                .build();
    }


    /**
     * Copy parameters from job to step.
     *
     * @param values list of parameters names, that should be copied
     */
    @Bean
    public JobParameterExecutionContextCopyListener jobParameterExecutionContextCopyListener(String... values) {
        JobParameterExecutionContextCopyListener listener = new JobParameterExecutionContextCopyListener();
        listener.setKeys(values);
        return listener;
    }
}
