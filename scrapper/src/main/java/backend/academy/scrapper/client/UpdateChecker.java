package backend.academy.scrapper.client;

import backend.academy.scrapper.entity.Link;

public interface UpdateChecker {
    boolean hasUpdates(Link link);
}
