package backend.academy.scrapper.client;

public interface UpdateChecker {
    boolean hasUpdates(String url, String lastChecked);
}
