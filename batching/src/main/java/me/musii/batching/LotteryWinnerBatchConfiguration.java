package me.musii.batching;

import me.musii.batching.tasklets.DownloadFileTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
//@ImportResource("classpath:lottery-batch-configuration.xml") todo
//@PropertySource(value = "classpath:batching.properties")
public class LotteryWinnerBatchConfiguration {

    @Value("${app.batching.lottery.source.url}")
    private String sourceUsersUrl;

    @Bean
    public Job lotteryWinnerJob(JobRepository jobRepository, Step downloadUsersFile) {
        return new JobBuilder("chooseLotteryWinner", jobRepository)
                .start(downloadUsersFile)
                .build();
    }

    @Bean
    public Step downloadUsersFile(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("downloadFileFromExternalSource", jobRepository)
                .tasklet(downloadLotteryUsersTasklet(), transactionManager)
                .build();
    }

    @Bean
    public DownloadFileTasklet downloadLotteryUsersTasklet() {
        return new DownloadFileTasklet(sourceUsersUrl);
    }

}
