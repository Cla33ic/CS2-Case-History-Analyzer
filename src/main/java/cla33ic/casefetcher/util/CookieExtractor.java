package cla33ic.casefetcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

public class CookieExtractor {
    private static final Logger logger = LoggerFactory.getLogger(CookieExtractor.class);

    private CookieExtractor() {
        // Private constructor to prevent instantiation
    }

    public static String extractSessionId(String cookie) {
        return extractCookieValue(cookie, "sessionid")
                .orElseThrow(() -> {
                    logger.error("sessionid not found in cookie");
                    return new IllegalArgumentException("sessionid not found in cookie");
                });
    }

    public static Optional<String> extractCookieValue(String cookie, String key) {
        return Optional.ofNullable(cookie)
                .flatMap(c -> Arrays.stream(c.split("; "))
                        .filter(part -> part.startsWith(key + "="))
                        .findFirst()
                        .map(part -> part.substring(key.length() + 1)));
    }
}