package me.musii.batching.jobs.lotterywinner.domain;

import lombok.Data;

@Data
public class UserIdAndAmount {

    private Long id;
    private int amount;
}
