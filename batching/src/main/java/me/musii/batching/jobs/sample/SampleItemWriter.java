package me.musii.batching.jobs.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
public class SampleItemWriter<T> implements ItemWriter<T> {

    @Override
    public void write(Chunk<? extends T> chunk) {
        log.info("SampleProcessor. Items received {}", chunk.getItems());
    }

    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        System.out.println(stepExecution);
    }

}
