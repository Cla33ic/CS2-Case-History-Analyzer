package cla33ic.casefetcher.model;

public class UserInput {
    private final String baseUrl;
    private final String cookie;

    public UserInput(String baseUrl, String cookie) {
        this.baseUrl = baseUrl;
        this.cookie = cookie;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getCookie() {
        return cookie;
    }
}
