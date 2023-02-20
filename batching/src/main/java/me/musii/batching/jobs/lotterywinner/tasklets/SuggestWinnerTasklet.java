package me.musii.batching.jobs.lotterywinner.tasklets;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.musii.batching.jobs.ammountcaluclation.RandomSupplier;
import me.musii.batching.jobs.lotterywinner.domain.users.User;
import me.musii.batching.jobs.lotterywinner.domain.users.dao.UsersRepository;
import org.springframework.batch.core.StepContribution;
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
        int participantsCount = usersRepository.countByAmountGreaterThan(minAmount);
        RandomSupplier randomSupplier = new RandomSupplier(participantsCount);
        Long id = jdbcTemplate.query(
                //language=SQL
                "select u.ID from (SELECT rownum() as rn, id FROM USER) u where u.rn = ?",
                rs -> {
                    if (rs.next()) {
                        return rs.getLong(1);
                    } else {
                        return null;
                    }
                },
                randomSupplier.nextInt());
        Objects.requireNonNull(id);
        User winner = usersRepository.findById(id)
                .orElseThrow(RuntimeException::new);
        ExecutionContext ctx = contribution.getStepExecution().getJobExecution().getExecutionContext();
        log.info("THE WINNER IS {{}}!!!", winner);
        ctx.put("result", "The winner is: " + winner);
        return RepeatStatus.FINISHED;
    }


}
