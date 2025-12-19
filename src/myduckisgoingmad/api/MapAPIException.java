package myduckisgoingmad.api;

public class MapAPIException extends Exception {
    private final int statusCode;

    public MapAPIException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public MapAPIException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public MapAPIException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
