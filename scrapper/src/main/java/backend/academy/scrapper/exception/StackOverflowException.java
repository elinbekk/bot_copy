package backend.academy.scrapper.exception;

public class StackOverflowException extends RuntimeException {
    public StackOverflowException(String message) {
        super(message);
    }

    public StackOverflowException(String message, Throwable cause) {
        super(message, cause);
    }
}
