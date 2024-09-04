package cla33ic.casefetcher.exception;

public class CaseFetcherException extends RuntimeException {
    private final int statusCode;

    public CaseFetcherException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "CaseFetcherException: " + getMessage() + (statusCode != 0 ? " (Status Code: " + statusCode + ")" : "");
    }
}