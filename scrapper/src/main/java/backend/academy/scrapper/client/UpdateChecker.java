package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.Link;

public interface UpdateChecker {
    boolean hasUpdates(Link link);
}
