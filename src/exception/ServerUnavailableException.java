package exception;

/** The client could not reach the server or lost the connection. */
public class ServerUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ServerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
