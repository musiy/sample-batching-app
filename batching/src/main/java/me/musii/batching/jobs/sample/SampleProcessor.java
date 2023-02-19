package me.musii.batching.jobs.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class SampleProcessor implements ItemProcessor<String, String> {

    @Override
    public String process(String item) {
        log.info("SampleProcessor. Item processed {{}}", item);
        return item;
    }

    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        log.info("SampleProcessor {{}}", stepExecution);
    }

}
