package me.musii.batching.jobs.lotterywinner;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.musii.batching.jobs.lotterywinner.domain.users.User;
import me.musii.batching.jobs.lotterywinner.domain.users.dao.UsersRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class UserItemWriter implements ItemWriter<User> {

    private final UsersRepository usersRepository;

    @Override
    public void write(Chunk<? extends User> chunk) {
        usersRepository.saveAll(chunk.getItems());
    }

}
