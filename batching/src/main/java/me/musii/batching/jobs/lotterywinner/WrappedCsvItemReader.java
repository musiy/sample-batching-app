package me.musii.batching.jobs.lotterywinner;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.util.Objects;

@RequiredArgsConstructor
public class WrappedCsvItemReader<T> implements ResourceAwareItemReaderItemStream<T> {

    @Delegate
    private final ResourceAwareItemReaderItemStream<T> fileItemReader;

    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        JobParameters jobParameters = stepExecution.getJobExecution().getJobParameters();
        String localFileName = jobParameters.getString(LotteryWinnerJob.CSV_FILE_NAME_KEY);
        Objects.requireNonNull(localFileName);
        Resource resource = new FileSystemResource(Path.of(localFileName));
        fileItemReader.setResource(resource);
    }

}
