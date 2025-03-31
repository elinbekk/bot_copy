package backend.academy.scrapper.client;

import backend.academy.bot.entity.TrackedResource;

public interface UpdateChecker {
//    boolean hasUpdates(String url, Instant lastChecked);
    boolean hasUpdates(TrackedResource trackedResource);
}
