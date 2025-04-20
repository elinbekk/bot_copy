package backend.academy.bot;

public class DuplicateLinkException extends RuntimeException {
    public DuplicateLinkException(String message) {
        super(message);
    }
}
