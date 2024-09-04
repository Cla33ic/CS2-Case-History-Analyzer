package cla33ic.casefetcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PaginationHelper {
    private static final Logger logger = LoggerFactory.getLogger(PaginationHelper.class);

    private PaginationHelper() {
        // Private constructor to prevent instantiation
    }

    public static boolean hasNextPage(Optional<String[]> cursor) {
        boolean hasNext = cursor.isPresent();
        logger.debug("Has next page: {}", hasNext);
        return hasNext;
    }

    public static void logPageInfo(int pageNumber, int itemsCount) {
        logger.info("Processed page {}: {} items", pageNumber, itemsCount);
    }
}