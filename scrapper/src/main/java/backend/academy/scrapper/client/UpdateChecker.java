package backend.academy.scrapper.client;

import java.time.Instant;

public interface UpdateChecker {
    boolean hasUpdates(String url, Instant lastChecked);
}
