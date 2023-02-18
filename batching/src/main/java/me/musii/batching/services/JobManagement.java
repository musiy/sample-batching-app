package me.musii.batching.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Service that provides interface to job management.
 */
@Service
@AllArgsConstructor
@Slf4j
public class JobManagement {

    private final JobLauncher jobLauncher;

    private final Collection<Job> jobs;

    @SneakyThrows
    public void startJob(String jobName)  {
        log.info("Start job " + jobName);
        Job job = jobs.stream()
                .filter(j -> jobName.equals(j.getName()))
                .findFirst()
                .orElseThrow(() -> new NoSuchJobException("Job not found: " + jobName));
        try {
            jobLauncher.run(job, new JobParameters());
        } catch (Exception e) {
            log.info("Failed job " + jobName, e);
            throw e;
        }
        log.info("Finish job " + jobName);
    }
}
