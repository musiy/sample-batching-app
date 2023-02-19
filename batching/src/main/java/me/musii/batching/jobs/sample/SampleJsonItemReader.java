package me.musii.batching.jobs.sample;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class SampleJsonItemReader implements ItemReader<String> {

    private final long noMoreThan ;
    private long counter;
    @Override
    public String read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (++counter > noMoreThan) {
            return null;
        }
        String uuid = UUID.randomUUID().toString();
        log.info("SampleJsonItemReader. Item generated {{}}", uuid);
        return uuid;
    }

    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        System.out.println(stepExecution);
    }

}
