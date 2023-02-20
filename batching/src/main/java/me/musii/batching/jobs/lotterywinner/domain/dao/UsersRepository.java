package me.musii.batching.jobs.lotterywinner.domain.dao;

import me.musii.batching.jobs.lotterywinner.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface UsersRepository extends CrudRepository<User, Long> {

    Collection<User> findAllByUserIdInAndJobExecutionIdEquals(Collection<Long> ids, Long jobExecutionId);

    int countByAmountGreaterThanAndJobExecutionIdEquals(long min, Long jobExecutionId);

}
