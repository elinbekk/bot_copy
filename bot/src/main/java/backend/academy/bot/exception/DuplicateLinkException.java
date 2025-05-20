package backend.academy.bot.exception;

public class DuplicateLinkException extends RuntimeException {
    public DuplicateLinkException(String message) {
        super(message);
    }
}
