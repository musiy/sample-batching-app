package me.musii.batching.jobs.lotterywinner.domain.users.dao;

import me.musii.batching.jobs.lotterywinner.domain.users.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface UsersRepository extends CrudRepository<User, Long> {

    Collection<User> findAllByUserIdIn(Collection<Long> ids);

    int countByAmountGreaterThan(long min);

}
