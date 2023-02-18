package me.musii.web.controllers;

import lombok.AllArgsConstructor;
import me.musii.batching.services.JobManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TestController {

    private final JobManagement jobManagement;

    @GetMapping("/run/{jobName}")
    public String runJob(@PathVariable String jobName) {
        jobManagement.startJob(jobName);
        return "ok"; //todo
    }
}
