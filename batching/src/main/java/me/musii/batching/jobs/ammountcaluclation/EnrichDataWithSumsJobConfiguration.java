package me.musii.batching.jobs.ammountcaluclation;

import lombok.RequiredArgsConstructor;
import me.musii.batching.jobs.utils.RandomSupplier;
import me.musii.batching.jobs.lotterywinner.domain.UserDescr;
import me.musii.batching.jobs.lotterywinner.domain.UserIdAndAmount;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class EnrichDataWithSumsJobConfiguration {

    @Autowired
    private ItemReader<UserDescr> usersFromJsonReader;


    @Bean
    public Job getCvsFromJson(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              Step downloadFileStep) {
        return new JobBuilder("getCvsFromJson", jobRepository)
                .start(downloadFileStep)
                .next(jsonToCvsStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step jsonToCvsStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
        // just for simplicity let's consider 100 as max gen value
        RandomSupplier randomSupplier = new RandomSupplier(60);

        return new StepBuilder("jsonToCvsStep", jobRepository)
                // The size is a matter of discussions but here for simplicity let it be 100
                .<UserDescr, UserIdAndAmount>chunk(100, transactionManager)
                .reader(usersFromJsonReader)
                .processor(u -> {
                    UserIdAndAmount userIdAndAmount = new UserIdAndAmount();
                    userIdAndAmount.setId(u.getId());
                    userIdAndAmount.setAmount(randomSupplier.nextInt());
                    return userIdAndAmount;
                })
                .writer(usersToCvsWriter())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public ItemWriter<UserIdAndAmount> usersToCvsWriter() {

        BeanWrapperFieldExtractor<UserIdAndAmount> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"id", "amount"});
        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<UserIdAndAmount> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<UserIdAndAmount>()
                .name("usersToCvsWriter")
                .resource(new FileSystemResource("data.csv"))
                .lineAggregator(lineAggregator)
                .build();
    }

}
