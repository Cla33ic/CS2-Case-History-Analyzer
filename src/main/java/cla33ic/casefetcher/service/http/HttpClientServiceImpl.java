package cla33ic.casefetcher.service.http;

import java.io.IOException;
import java.util.Map;

public class HttpClientServiceImpl extends AbstractHttpClientService {

    @Override
    public String get(String url, Map<String, String> headers) throws IOException {
        return executeGet(url, headers);
    }

    @Override
    public String post(String url, Map<String, String> headers, String body) throws IOException {
        return executePost(url, headers, body);
    }
}