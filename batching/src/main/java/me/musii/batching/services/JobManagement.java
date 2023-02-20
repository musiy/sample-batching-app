package me.musii.batching.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service that provides interface to job management.
 */
@Service
@RequiredArgsConstructor
public class JobManagement {

    private final JobLauncher jobLauncher;

    private final Collection<Job> jobs;

    @SneakyThrows
    public String startJob(String jobName, Map<String, String> properties) {
        Job job = jobs.stream()
                .filter(j -> jobName.equals(j.getName()))
                .findFirst()
                .orElseThrow(() -> new NoSuchJobException("Job not found: " + jobName));

        Map<String, JobParameter<?>> jobParams = properties.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    String value = e.getValue();
                    return new JobParameter<>(value, String.class, false);
                }));
        JobParameters jobParameters = new JobParameters(jobParams);
        JobExecution jobExecution = jobLauncher.run(job, jobParameters);
        return (String) jobExecution.getExecutionContext().get("result");
    }
}
