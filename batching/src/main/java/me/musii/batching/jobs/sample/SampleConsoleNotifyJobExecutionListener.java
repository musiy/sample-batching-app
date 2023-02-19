package me.musii.batching.jobs.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Generally we can notify some external services that job has started / completed,
 *  trigger some work inside out system and so on.
 * For example - send an email, write to messenger and so on.
 */
@Component
@Slf4j
public class SampleConsoleNotifyJobExecutionListener implements JobExecutionListener {

    public void beforeJob(JobExecution jobExecution) {
        log.info("beforeJob {{}}", jobExecution.getJobInstance().getJobName());
    }

    public void afterJob(JobExecution jobExecution) {
        log.info("afterJob {{}}. Status: {{}}. Exit status: {{}}", jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(), jobExecution.getExitStatus());
    }

}
