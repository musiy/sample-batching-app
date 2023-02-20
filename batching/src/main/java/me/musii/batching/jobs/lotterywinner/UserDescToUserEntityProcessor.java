package me.musii.batching.jobs.lotterywinner;

import lombok.extern.slf4j.Slf4j;
import me.musii.batching.jobs.lotterywinner.domain.User;
import me.musii.batching.jobs.lotterywinner.domain.UserDescr;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserDescToUserEntityProcessor implements ItemProcessor<UserDescr, User> {

    private StepExecution stepExecution;
    @Override
    public User process(UserDescr item) {
        User user = new User();
        user.setUserId(item.getId());
        user.setName(item.getName());
        user.setJobExecutionId(stepExecution.getJobExecutionId());
        return user;
    }

    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

}
