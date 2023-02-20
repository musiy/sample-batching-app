package me.musii.batching.jobs.lotterywinner;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.musii.batching.jobs.lotterywinner.domain.User;
import me.musii.batching.jobs.lotterywinner.domain.UserDescr;
import me.musii.batching.jobs.lotterywinner.domain.UserIdAndAmount;
import me.musii.batching.jobs.lotterywinner.tasklets.ClearDataInDBTasklet;
import me.musii.batching.jobs.lotterywinner.tasklets.DownloadFileTasklet;
import me.musii.batching.jobs.lotterywinner.tasklets.SuggestWinnerTasklet;
import me.musii.batching.jobs.sample.SampleConsoleNotifyJobExecutionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class LotteryWinnerBatchConfiguration {

    private final SampleConsoleNotifyJobExecutionListener sampleConsoleNotifyJobExecutionListener;
    private final ItemWriter<User> userItemWriter;
    private final ItemWriter<UserIdAndAmount> userItemAmountUpdateWriter;
    private final UserDescToUserEntityProcessor userDescToUserEntityProcessor;
    private final DownloadFileTasklet downloadFileTasklet;
    private final SuggestWinnerTasklet suggestWinnerTasklet;
    private final ClearDataInDBTasklet clearDataInDBTasklet;

    /**
     * Here value 2 by default for convenience during debug
     */
    @Value("${app.batching.lottery.chunk.size:2}")
    private int chunkSize;

    /**
     * todo The main disadvantage of this job is that during rerun it starts completely from the start.
     *      Can we do better and restart it from some point?
     */
    @Bean
    public Job lotteryWinnerJob(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(new String[]{LotteryWinnerJob.CSV_FILE_NAME_KEY});
        return new JobBuilder("lotteryWinnerJob", jobRepository)
                .validator(validator)
                .listener(sampleConsoleNotifyJobExecutionListener)
                .start(downloadFileStep(jobRepository, transactionManager))
                //.next(clearDataInDb(jobRepository, transactionManager))
                .next(saveDataFromSourceToDb(jobRepository, transactionManager))
                .next(updateDbByAmounts(jobRepository, transactionManager))
                .next(getWinnerStep(jobRepository, transactionManager))
                .build();
    }

    /**
     * JsonItemReader can download json itself from external resource.<br>
     * The initial purpose of this step was in caching file on the disk for further steps.
     */
    @Bean
    public Step downloadFileStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
        return new StepBuilder("downloadFileStep", jobRepository)
                .tasklet(downloadFileTasklet, transactionManager)
                .listener(toJobContextPromotionListener(LotteryWinnerJob.LOCAL_FILE_NAME_KEY))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step getWinnerStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
        return new StepBuilder("getWinnerStep", jobRepository)
                .tasklet(suggestWinnerTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    /**
     * Clear data before replacement.
     */
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
                .writer(userItemWriter)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step updateDbByAmounts(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("convertJson", jobRepository)
                .<UserIdAndAmount, UserIdAndAmount>chunk(chunkSize, transactionManager)
                .reader(usersFromCsvReader())
                .writer(userItemAmountUpdateWriter)
                .allowStartIfComplete(true)
                .build();
    }

    @SneakyThrows
    @Bean
    public ItemReader<UserDescr> usersFromJsonReader() {
        ObjectMapper mapper = skipUnknownFieldsObjectMapper();
        JsonItemReader<UserDescr> personItemReader = new JsonItemReaderBuilder<UserDescr>()
                .name("usersReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(mapper, UserDescr.class))
                .build();
        return new WrappedJsonItemReader<>(personItemReader);
    }

    @SneakyThrows
    @Bean
    public ItemReader<UserIdAndAmount> usersFromCsvReader() {
        FlatFileItemReader<UserIdAndAmount> reader = new FlatFileItemReaderBuilder<UserIdAndAmount>()
                .name("usersFromCsvReader")
                .delimited()
                .names(new String[]{"id", "amount"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(UserIdAndAmount.class);
                }})
                .build();
        return new WrappedCsvItemReader<>(reader);
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
     * Helps in passing parameters from one step to another, copying them from step context to job context.
     */
    @Bean
    public ExecutionContextPromotionListener toJobContextPromotionListener(String... values) {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(values);
        return listener;
    }

}
