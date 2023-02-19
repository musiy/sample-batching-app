package me.musii.batching.jobs.lotterywinner.domain.users.dao;

import me.musii.batching.jobs.lotterywinner.domain.users.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends CrudRepository<User, Long> {

}
