package me.musii.web.controllers;

import lombok.AllArgsConstructor;
import me.musii.batching.services.JobManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller as a straightforward way to  run jobs.
 */
@RestController
@AllArgsConstructor
public class TestController {

    private final JobManagement jobManagement;

    @GetMapping("/run/{jobName}")
    public String runJob(@PathVariable String jobName,
                         @RequestParam Map<String, String> queryProperties) {
        String result = jobManagement.startJob(jobName, queryProperties);
        return result == null ? "Ok!" : result;
    }
}
