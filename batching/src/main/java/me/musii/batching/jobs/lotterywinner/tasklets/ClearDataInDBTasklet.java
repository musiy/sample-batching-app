package me.musii.batching.jobs.lotterywinner.tasklets;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.musii.batching.jobs.lotterywinner.domain.users.dao.UsersRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * Data in table users stored with job_execution_id field.
 */
@Component
@RequiredArgsConstructor
public class ClearDataInDBTasklet implements Tasklet {

    private final UsersRepository repository;

    @SneakyThrows
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // here we can delete not all records, but part of them, chunking deletion
        repository.deleteAll();
        return RepeatStatus.FINISHED;
    }

}
