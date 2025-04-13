package backend.academy.scrapper.client;

import backend.academy.bot.entity.TrackedResource;

public interface UpdateChecker {
    boolean hasUpdates(TrackedResource trackedResource);
}
