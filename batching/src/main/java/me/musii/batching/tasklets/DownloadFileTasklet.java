package me.musii.batching.tasklets;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Just an generic tasklet for downloading the file by provided path.
 * File is saved to temporary folder and path preserved in context.
 */
@AllArgsConstructor
public class DownloadFileTasklet implements Tasklet {

    private final String path;

    @SneakyThrows
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(path)).GET().build();
        HttpClient httpClient = HttpClient.newHttpClient();
        Path temp = Files.createTempFile("", ".tmp");
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofFile(temp));
        chunkContext.setAttribute("path", temp);
        return RepeatStatus.FINISHED;
    }
}
