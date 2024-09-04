package cla33ic.casefetcher.service.http;

import cla33ic.casefetcher.exception.CaseFetcherException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractHttpClientService implements HttpClientService {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpClientService.class);
    protected String cookie;
    private static final int MAX_RETRIES = 10;
    private static final long INITIAL_DELAY = 2000; // 2 seconds
    private static final long MAX_DELAY = 128000; // 128 seconds

    @Override
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    protected String executeGet(String url, Map<String, String> headers) throws IOException {
        return executeWithRetry(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                setHeaders(request, headers);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        return EntityUtils.toString(response.getEntity());
                    } else {
                        throw new CaseFetcherException("HTTP request failed", statusCode);
                    }
                }
            }
        });
    }

    protected String executePost(String url, Map<String, String> headers, String body) throws IOException {
        return executeWithRetry(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost request = new HttpPost(url);
                setHeaders(request, headers);
                request.setEntity(new StringEntity(body));
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        return EntityUtils.toString(response.getEntity());
                    } else {
                        throw new CaseFetcherException("HTTP request failed", statusCode);
                    }
                }
            }
        });
    }

    private void setHeaders(org.apache.http.HttpRequest request, Map<String, String> headers) {
        headers.forEach(request::addHeader);
        if (cookie != null && !headers.containsKey("Cookie")) {
            request.addHeader("Cookie", cookie);
        }
    }

    private String executeWithRetry(HttpOperation operation) throws IOException {
        long delay = INITIAL_DELAY;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return operation.execute();
            } catch (CaseFetcherException e) {
                if (e.getStatusCode() == 429 || e.getStatusCode() >= 500) {
                    logger.warn("Request failed with status code: {}. Retrying... (Attempt {} of {})",
                            e.getStatusCode(), attempt, MAX_RETRIES);
                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Request interrupted", ie);
                        }
                        delay = Math.min(delay * 2, MAX_DELAY);
                    } else {
                        throw new IOException("Max retries reached. Unable to complete the request.", e);
                    }
                } else {
                    throw e;
                }
            }
        }
        throw new IOException("Max retries reached. Unable to complete the request.");
    }

    @FunctionalInterface
    private interface HttpOperation {
        String execute() throws IOException;
    }
}