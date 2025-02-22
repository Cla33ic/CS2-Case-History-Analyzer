package cla33ic.casefetcher.service.http;

import cla33ic.casefetcher.exception.CaseFetcherException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract base class for HTTP client services.
 */
public abstract class AbstractHttpClientService implements HttpClientService {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpClientService.class);
    protected String cookie;
    private static final int MAX_RETRIES = 10;
    private static final long INITIAL_DELAY = 2000; // 2 seconds
    private static final long MAX_DELAY = 128000; // 128 seconds

    // Shared pooled HTTP client instance
    private static final PoolingHttpClientConnectionManager connectionManager;
    private static final CloseableHttpClient client;
    static {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);
        client = HttpClients.custom().setConnectionManager(connectionManager).build();
    }

    @Override
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    protected String executeGet(String url, Map<String, String> headers) throws IOException {
        return executeWithRetry(() -> {
            HttpGet request = new HttpGet(url);
            setHeaders(request, headers);
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    return EntityUtils.toString(response.getEntity());
                } else {
                    throw new CaseFetcherException("HTTP request failed", statusCode);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected String executePost(String url, Map<String, String> headers, String body) throws IOException {
        return executeWithRetry(() -> {
            HttpPost request = new HttpPost(url);
            setHeaders(request, headers);
            request.setEntity(new StringEntity(body));
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    return EntityUtils.toString(response.getEntity());
                } else {
                    throw new CaseFetcherException("HTTP request failed", statusCode);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setHeaders(org.apache.hc.core5.http.ClassicHttpRequest request, Map<String, String> headers) {
        headers.forEach(request::addHeader);
        if (cookie != null && !headers.containsKey("Cookie")) {
            request.addHeader("Cookie", cookie);
        }
    }

    private String executeWithRetry(HttpOperation operation) throws IOException {
        long delay = INITIAL_DELAY;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            String requestId = UUID.randomUUID().toString();
            long startTime = System.currentTimeMillis();
            try {
                String result = operation.execute();
                long elapsed = System.currentTimeMillis() - startTime;
                logger.info("Request {} completed in {} ms on attempt {}", requestId, elapsed, attempt);
                return result;
            } catch (CaseFetcherException e) {
                long elapsed = System.currentTimeMillis() - startTime;
                logger.error("Request {} failed in {} ms on attempt {} with status {}: {}",
                        requestId, elapsed, attempt, e.getStatusCode(), e.getMessage());
                if (e.getStatusCode() == 429 || e.getStatusCode() >= 500) {
                    logger.warn("Request {} will be retried. (Attempt {} of {})", requestId, attempt, MAX_RETRIES);
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
