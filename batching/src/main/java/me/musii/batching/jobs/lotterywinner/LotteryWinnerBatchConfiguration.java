package me.musii.batching.jobs.lotterywinner;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.musii.batching.jobs.lotterywinner.domain.users.User;
import me.musii.batching.jobs.lotterywinner.domain.users.UserDescr;
import me.musii.batching.jobs.lotterywinner.tasklets.ClearDataInDBTasklet;
import me.musii.batching.jobs.lotterywinner.tasklets.DownloadFileTasklet;
import me.musii.batching.jobs.sample.SampleConsoleNotifyJobExecutionListener;
import me.musii.batching.jobs.sample.SampleItemStream;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.listener.JobParameterExecutionContextCopyListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
//@ImportResource("classpath:lottery-batch-configuration.xml") todo
//@PropertySource(value = "classpath:batching.properties")
@RequiredArgsConstructor
public class LotteryWinnerBatchConfiguration {

    private final SampleConsoleNotifyJobExecutionListener sampleConsoleNotifyJobExecutionListener;
    private final UserDescrWriter userDescrWriter;
    private final UserDescToUserEntityProcessor userDescToUserEntityProcessor;

    private final DownloadFileTasklet downloadFileTasklet;
    private final ClearDataInDBTasklet clearDataInDBTasklet;

    /**
     * Here value 2 by default for convenience during debug
     */
    @Value("${app.batching.lottery.chunk.size:2}")
    private int chunkSize;

    @Bean
    public Job lotteryWinnerJob(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        return new JobBuilder("chooseLotteryWinner", jobRepository)
                //.validator(parametersValidator()) todo add some sample validation of parameters, see 4.1.4
                .listener(sampleConsoleNotifyJobExecutionListener)
                .start(downloadUsersFileStep(jobRepository, transactionManager))
                .next(clearDataInDb(jobRepository, transactionManager))
                .next(saveDataFromSourceToDb(jobRepository, transactionManager))
                .build();
    }

    /**
     * JsonItemReader can download json itself from external resource.<br>
     * The purpose of this step is cache file on the disk.
     */
    @Bean
    public Step downloadUsersFileStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("downloadFileFromExternalSource", jobRepository)
                .tasklet(downloadFileTasklet, transactionManager)
                .listener(toJobContextPromotionListener(LotteryWinnerJob.LOCAL_FILE_NAME_KEY))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step clearDataInDb(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
        return new StepBuilder("clearDataInDb", jobRepository)
                .tasklet(clearDataInDBTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step saveDataFromSourceToDb(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager) {
        return new StepBuilder("convertJson", jobRepository)
                .<UserDescr, User>chunk(chunkSize, transactionManager)
                .reader(usersFromJsonReader())
                .processor(userDescToUserEntityProcessor)
                .writer(userDescrWriter)
                .allowStartIfComplete(true)
                .build();
    }

    @SneakyThrows
    @Bean
    public ItemReader<UserDescr> usersFromJsonReader() {
        ObjectMapper mapper = skipUnknownFieldsObjectMapper();
        JsonItemReader<UserDescr> personItemReader = new JsonItemReaderBuilder<UserDescr>()
                .name("personItemReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(mapper, UserDescr.class))
                .build();
        return new WrappedJsonItemReader(personItemReader);
    }

    /**
     * Object mapper which allows to skip unknown fields from source json.
     */
    private static ObjectMapper skipUnknownFieldsObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Sample no op step revealing the principles of data processing
     */
    @Bean
    public Step noOpStep(JobRepository jobRepository,
                         PlatformTransactionManager transactionManager) {

        SampleItemStream sampleItemStream = new SampleItemStream(10); // todo
        return new StepBuilder("handleRecords", jobRepository)
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
     * Helps in passing parameters from one step to another, copying them from step context to job context.
     */
    @Bean
    public ExecutionContextPromotionListener toJobContextPromotionListener(String... values) {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(values);
        return listener;
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
