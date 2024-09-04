package cla33ic.casefetcher.service.http;

import java.io.IOException;
import java.util.Map;

public interface HttpClientService {
    String get(String url, Map<String, String> headers) throws IOException;
    String post(String url, Map<String, String> headers, String body) throws IOException;
    void setCookie(String cookie);
}