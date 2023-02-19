package me.musii.batching.jobs.lotterywinner.domain.users;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private Long amount;

    // todo actually should not be here, because of separation of concerns,
    //      but just for simplicity leave it here
    private Long jobExecutionId;
}
