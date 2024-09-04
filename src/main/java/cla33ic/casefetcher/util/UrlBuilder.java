package cla33ic.casefetcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UrlBuilder {
    private static final Logger logger = LoggerFactory.getLogger(UrlBuilder.class);
    private static final Pattern CURSOR_PATTERN = Pattern.compile("\"cursor\":\\{\"time\":(\\d+),\"time_frac\":(\\d+),\"s\":\"(\\d+)\"\\}");

    private UrlBuilder() {
        // Private constructor to prevent instantiation
    }

    public static String buildUrl(String baseUrl, String cookie, String time, String timeFrac, String s) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl)
                .append("?ajax=1");

        if (time != null && timeFrac != null && s != null) {
            urlBuilder.append("&cursor%5Btime%5D=").append(time)
                    .append("&cursor%5Btime_frac%5D=").append(timeFrac)
                    .append("&cursor%5Bs%5D=").append(encodeUrl(s));
        }

        urlBuilder.append("&sessionid=").append(extractSessionId(cookie));
        urlBuilder.append("&l=english");

        String url = urlBuilder.toString();
        logger.debug("Built URL: {}", url);
        return url;
    }

    public static void setHeaders(Map<String, String> headers, String cookie, String baseUrl) {
        headers.put("Accept", "*/*");
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Connection", "keep-alive");
        headers.put("Cookie", cookie + "; Steam_Language=english");
        headers.put("Referer", baseUrl);
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        headers.put("X-Requested-With", "XMLHttpRequest");
        logger.debug("Set headers for request to {} - Cookie: {}", baseUrl, cookie);
    }

    public static String encodeUrl(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String extractSessionId(String cookie) {
        return Optional.ofNullable(cookie)
                .flatMap(c -> Optional.of(c.split("; "))
                        .flatMap(parts ->
                                java.util.Arrays.stream(parts)
                                        .filter(part -> part.startsWith("sessionid="))
                                        .findFirst()
                                        .map(part -> part.substring("sessionid=".length()))
                        )
                )
                .orElseThrow(() -> {
                    logger.error("sessionid not found in cookie");
                    return new IllegalArgumentException("sessionid not found in cookie");
                });
    }

    public static Optional<String[]> extractCursor(String responseBody) {
        Matcher matcher = CURSOR_PATTERN.matcher(responseBody);
        if (matcher.find()) {
            String[] cursor = new String[]{matcher.group(1), matcher.group(2), matcher.group(3)};
            logger.debug("Extracted cursor: time={}, time_frac={}, s={}", cursor[0], cursor[1], cursor[2]);
            return Optional.of(cursor);
        }
        logger.debug("No cursor found in response body");
        return Optional.empty();
    }
}