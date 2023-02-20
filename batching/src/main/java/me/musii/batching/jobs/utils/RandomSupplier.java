package me.musii.batching.jobs.utils;

import java.time.LocalDateTime;
import java.util.Random;

public class RandomSupplier {

    // yep there is ThreadLocalRandom on the table, just for simplicity chosen general Random class
    private final Random random;
    private final int maxBound;

    public RandomSupplier(int maxBound) {
        this.random = new Random(LocalDateTime.now().getNano() % 31);
        this.maxBound = maxBound;
    }

    public int nextInt() {
        return random.nextInt(0, maxBound);
    }
}
