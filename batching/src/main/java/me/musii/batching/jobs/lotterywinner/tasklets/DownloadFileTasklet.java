package me.musii.batching.jobs.lotterywinner.tasklets;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.musii.batching.jobs.lotterywinner.LotteryWinnerJob;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Just a generic tasklet for downloading the file by provided path.
 * File is saved to temporary folder and path preserved in context.
 */
@Component
@Slf4j
public class DownloadFileTasklet implements Tasklet {

    // todo instead of hard-code any user-specific code it's necessary to always url in  parameters
    // todo maybe each job should have it's own launch service.
    @Value("${app.batching.lottery.source.url}")
    private String sourceUsersUrl;

    @SneakyThrows
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        StepExecution stepExecution = contribution.getStepExecution();
        URI sourceUri = getSourceUri(stepExecution);
        Path temp = downloadFileToTemporary(sourceUri, getJobInstancePrefix(stepExecution));
        ExecutionContext stepContext = stepExecution.getExecutionContext();
        stepContext.put(LotteryWinnerJob.LOCAL_FILE_NAME_KEY, temp.toString());
        // todo delete it from the disk after job
        log.info("temporary file {{}}", temp);
        return RepeatStatus.FINISHED;
    }

    private URI getSourceUri(StepExecution stepExecution) {
        JobParameters jobParameters = stepExecution.getJobParameters();
        String from = jobParameters.getString(LotteryWinnerJob.SOURCE_URL_KEY);
        if (from == null) {
            from = sourceUsersUrl;
        }
        Objects.requireNonNull(from);
        return URI.create(from);
    }

    private Path downloadFileToTemporary(URI uri, String tmpFilePrefix) throws IOException, InterruptedException {
        // todo http client can be bean
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).GET().build();
        HttpClient httpClient = HttpClient.newHttpClient();
        Path temp = Files.createTempFile(tmpFilePrefix, ".json");
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofFile(temp));
        return temp;
    }

    private static String getJobInstancePrefix(StepExecution stepExecution) {
        JobInstance jobInstance = stepExecution.getJobExecution().getJobInstance();
        return jobInstance.getJobName() + jobInstance.getInstanceId();
    }

}
