package me.musii.batching.jobs.lotterywinner;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import me.musii.batching.jobs.lotterywinner.domain.users.UserDescr;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.util.Objects;

@RequiredArgsConstructor
public class WrappedJsonItemReader extends JsonItemReader<UserDescr> {

    @Delegate
    private final JsonItemReader<UserDescr> jsonItemReader;

    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        ExecutionContext jobCtx = stepExecution.getJobExecution().getExecutionContext();
        String localFileName = jobCtx.getString(LotteryWinnerJob.LOCAL_FILE_NAME_KEY);
        Objects.requireNonNull(localFileName);
        Resource resource = new FileSystemResource(Path.of(localFileName));
        jsonItemReader.setResource(resource);
    }

}
