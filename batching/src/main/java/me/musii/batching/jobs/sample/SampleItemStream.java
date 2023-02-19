package me.musii.batching.jobs.sample;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class SampleItemStream implements ItemStream,
        ItemReader<String>,
        ItemWriter<String> {

    private static final String CNT_KEY_NAME = "cnt";

    private final int noMoreThan;

    private StepExecution stepExecution;

    public void open(ExecutionContext executionContext) throws ItemStreamException {
        log.info("SampleItemStream: open");
    }

    public void update(ExecutionContext executionContext) throws ItemStreamException {
        log.info("SampleItemStream: update");
    }

    public void close() throws ItemStreamException {
        log.info("SampleItemStream: close");
    }

    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public String read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        ExecutionContext ctx = stepExecution.getExecutionContext();
        Integer cnt = ctx.getInt(CNT_KEY_NAME, 0);
        if (++cnt > noMoreThan) {
            return null;
        }
        ctx.put(CNT_KEY_NAME, cnt);
        return UUID.randomUUID().toString();
    }

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        System.out.println(chunk.getItems());
    }
}
