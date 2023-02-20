package me.musii.batching.jobs.lotterywinner;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.musii.batching.jobs.lotterywinner.domain.users.User;
import me.musii.batching.jobs.lotterywinner.domain.users.UserIdAndAmount;
import me.musii.batching.jobs.lotterywinner.domain.users.dao.UsersRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class UserItemAmountUpdateWriter implements ItemWriter<UserIdAndAmount> {

    private final UsersRepository usersRepository;

    @Override
    public void write(Chunk<? extends UserIdAndAmount> chunk) {
        Map<Long, Integer> amounts = chunk.getItems().stream()
                .collect(Collectors.toMap(UserIdAndAmount::getId, UserIdAndAmount::getAmount));

        List<Long> ids = chunk.getItems().stream()
                .map(UserIdAndAmount::getId)
                .collect(Collectors.toList());

        Iterable<User> allById = usersRepository.findAllByUserIdIn(ids);
        allById.forEach(u -> {
            Integer amount = amounts.get(u.getUserId());
            if (amount != null) {
                u.setAmount(amount);
            }
        });
        usersRepository.saveAll(allById);
    }

}
