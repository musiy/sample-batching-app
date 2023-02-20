package me.musii.batching.jobs.lotterywinner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.musii.batching.jobs.lotterywinner.domain.User;
import me.musii.batching.jobs.lotterywinner.domain.UserIdAndAmount;
import me.musii.batching.jobs.lotterywinner.domain.dao.UsersRepository;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserItemAmountUpdateWriter implements ItemWriter<UserIdAndAmount> {

    private final UsersRepository usersRepository;
    private StepExecution stepExecution;

    @Override
    public void write(Chunk<? extends UserIdAndAmount> chunk) {
        Map<Long, Integer> amounts = chunk.getItems().stream()
                .collect(Collectors.toMap(UserIdAndAmount::getId, UserIdAndAmount::getAmount));

        List<Long> ids = chunk.getItems().stream()
                .map(UserIdAndAmount::getId)
                .collect(Collectors.toList());

        Iterable<User> allById = usersRepository.findAllByUserIdInAndJobExecutionIdEquals(ids,
                stepExecution.getJobExecutionId()) ;
        allById.forEach(u -> {
            Integer amount = amounts.get(u.getUserId());
            if (amount != null) {
                u.setAmount(amount);
            }
        });
        usersRepository.saveAll(allById);
    }

    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

}
