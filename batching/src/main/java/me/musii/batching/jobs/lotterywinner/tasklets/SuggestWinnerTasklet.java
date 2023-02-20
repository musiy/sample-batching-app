package me.musii.batching.jobs.lotterywinner.tasklets;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.musii.batching.jobs.lotterywinner.domain.User;
import me.musii.batching.jobs.lotterywinner.domain.dao.UsersRepository;
import me.musii.batching.jobs.utils.RandomSupplier;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class SuggestWinnerTasklet implements Tasklet {

    private final UsersRepository usersRepository;

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.batching.lottery.filter.amount.min:20}")
    private int minAmount;

    @SneakyThrows
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        StepExecution stepExecution = contribution.getStepExecution();
        int participantsCount = usersRepository.countByAmountGreaterThanAndJobExecutionIdEquals(minAmount,
                stepExecution.getJobExecutionId());
        Long id = getWinnerId(participantsCount, stepExecution.getJobExecutionId());
        Objects.requireNonNull(id);
        User winner = usersRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        ExecutionContext ctx = stepExecution.getJobExecution().getExecutionContext();
        log.info("THE WINNER IS {{}}!!!", winner);
        ctx.put("result", "The winner is: " + winner);
        return RepeatStatus.FINISHED;
    }

    private Long getWinnerId(int participantsCount, long jobExecutionId) {
        RandomSupplier randomSupplier = new RandomSupplier(participantsCount);
        return jdbcTemplate.query(
                //language=SQL
                "SELECT u.ID FROM (SELECT rownum() AS rn, id FROM user WHERE job_execution_id=?) u WHERE u.RN = ?",
                rs -> {
                    if (rs.next()) {
                        return rs.getLong(1);
                    } else {
                        return null;
                    }
                },
                jobExecutionId,
                randomSupplier.nextInt()
        );
    }

}
