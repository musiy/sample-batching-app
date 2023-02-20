package me.musii.batching.jobs.lotterywinner.tasklets;

import lombok.SneakyThrows;
import me.musii.batching.jobs.lotterywinner.LotteryWinnerJob;
import me.musii.batching.utils.Utils;
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
public class DownloadFileTasklet implements Tasklet {

    @Value("${app.batching.lottery.source.url}")
    private String sourceUsersUrl;

    @SneakyThrows
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        StepExecution stepExecution = contribution.getStepExecution();
        URI sourceUri = getSourceUri(stepExecution);
        Path temp = downloadFileToTemporary(sourceUri, Utils.getJobInstancePrefix(stepExecution));
        ExecutionContext stepContext = stepExecution.getExecutionContext();
        stepContext.put(LotteryWinnerJob.LOCAL_FILE_NAME_KEY, temp.toString());
        return RepeatStatus.FINISHED;
    }

    private URI getSourceUri(StepExecution stepExecution) {
        JobParameters jobParameters = stepExecution.getJobParameters();
        String sourceUrl = jobParameters.getString(LotteryWinnerJob.SOURCE_URL_KEY, sourceUsersUrl);
        Objects.requireNonNull(sourceUrl);
        return URI.create(sourceUrl);
    }

    private Path downloadFileToTemporary(URI uri, String tmpFilePrefix) throws IOException, InterruptedException {
        // todo http client can be bean
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).GET().build();
        HttpClient httpClient = HttpClient.newHttpClient();
        Path temp = Files.createTempFile(tmpFilePrefix, ".json");
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofFile(temp));
        return temp;
    }


}
