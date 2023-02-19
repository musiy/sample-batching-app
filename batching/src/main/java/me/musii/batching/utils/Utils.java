package me.musii.batching.utils;

import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;

public class Utils {

    public static String getJobInstancePrefix(StepExecution stepExecution) {
        JobInstance jobInstance = stepExecution.getJobExecution().getJobInstance();
        return jobInstance.getJobName() + jobInstance.getInstanceId();
    }
}
