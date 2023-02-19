package me.musii.batching.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.musii.batching.jobs.lotterywinner.LotteryWinnerJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Service that provides interface to job management.
 */
@Service
@RequiredArgsConstructor
public class JobManagement {

    private final JobLauncher jobLauncher;

    private final Collection<Job> jobs;

    @Value("${app.batching.lottery.source.url}")
    private String sourceUsersUrl;

    @SneakyThrows
    public void startJob(String jobName) {
        Job job = jobs.stream()
                .filter(j -> jobName.equals(j.getName()))
                .findFirst()
                .orElseThrow(() -> new NoSuchJobException("Job not found: " + jobName));
        Map<String, JobParameter<?>> params = new HashMap<>();
        params.put(LotteryWinnerJob.SOURCE_URL_KEY, new JobParameter<>(sourceUsersUrl, String.class, false));
        JobParameters jobParameters = new JobParameters(params);
        jobLauncher.run(job, jobParameters);
    }
}
