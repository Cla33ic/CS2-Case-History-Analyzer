package cla33ic.casefetcher;

import cla33ic.casefetcher.cache.CacheService;
import cla33ic.casefetcher.cache.InMemoryCacheService;
import cla33ic.casefetcher.config.LoggingConfig;
import cla33ic.casefetcher.model.CaseOpeningEvent;
import cla33ic.casefetcher.model.CaseOpeningSummary;
import cla33ic.casefetcher.model.UserInput;
import cla33ic.casefetcher.service.http.HttpClientService;
import cla33ic.casefetcher.service.http.HttpClientServiceImpl;
import cla33ic.casefetcher.service.inventory.InventoryHistoryServiceImpl;
import cla33ic.casefetcher.service.market.SteamMarketService;
import cla33ic.casefetcher.service.market.SteamMarketServiceImpl;
import cla33ic.casefetcher.service.parser.CaseOpeningParser;
import cla33ic.casefetcher.service.parser.CaseOpeningParserImpl;
import cla33ic.casefetcher.util.CachedResultsHandler;
import cla33ic.casefetcher.util.TerminalColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.time.LocalDateTime;

public class SteamInventoryHistoryApplication {
    private static final Logger logger = LoggerFactory.getLogger(SteamInventoryHistoryApplication.class);

    /**
     * Main method for the Steam Inventory History Tool application.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LoggingConfig.configureLogging();
        logger.info("Starting Steam Inventory History Application");
        logger.info("Current file.encoding: {}", Charset.defaultCharset().displayName());
        logger.info("Default Charset: {}", Charset.defaultCharset());

        try {
            UserInput userInput = getUserInput();
            runApplication(userInput);
        } catch (Exception e) {
            logger.error("An error occurred during application execution", e);
            System.out.println(TerminalColor.colorize("An error occurred: " + e.getMessage(), TerminalColor.RED));
            System.out.println("Please check the log file for more details.");
        }

        logger.info("Steam Inventory History Application completed");
    }

    /**
     * Get the user input for the Steam Inventory History Tool.
     * @return the user input
     * @throws IOException if an I/O error occurs
     */
    private static UserInput getUserInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println(TerminalColor.colorize("Welcome to the Steam Inventory History Tool!", TerminalColor.GREEN));
        System.out.println("This tool will help you analyze your CS2 case openings.");

        System.out.print("Enter your Steam profile URL: ");
        String profileUrl = reader.readLine().trim();
        String baseUrl = profileUrl + "/inventoryhistory/";

        System.out.print("Enter your Steam cookie: ");
        String cookie = reader.readLine().trim();

        if (profileUrl.isEmpty() || cookie.isEmpty()) {
            throw new IllegalArgumentException("Profile URL and cookie must not be empty");
        }

        return new UserInput(baseUrl, cookie);
    }

    /**
     * Run the Steam Inventory History Tool application.
     * @param userInput the user input
     */
    private static void runApplication(UserInput userInput) {
        System.out.println(TerminalColor.colorize("Processing inventory history...", TerminalColor.YELLOW));

        // Extract account id from the profile URL. E.g., from "https://steamcommunity.com/id/cla33ic" extract "cla33ic"
        String accountId = extractAccountId(userInput.getBaseUrl());

        // Load cached events if available
        List<CaseOpeningEvent> cachedEvents = CachedResultsHandler.loadCachedEvents(accountId);
        LocalDateTime latestCachedDate = cachedEvents.stream()
                .map(CaseOpeningEvent::dateTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        HttpClientService httpClientService = new HttpClientServiceImpl();
        httpClientService.setCookie(userInput.getCookie());

        // Updated the cache type to use TreeMap<LocalDate, Double> as required.
        CacheService<String, java.util.TreeMap<java.time.LocalDate, Double>> cacheService = new InMemoryCacheService<>();
        SteamMarketService steamMarketService = new SteamMarketServiceImpl(httpClientService, cacheService, userInput.getCookie());
        CaseOpeningParser caseOpeningParser = new CaseOpeningParserImpl(steamMarketService);
        InventoryHistoryServiceImpl inventoryHistoryService = new InventoryHistoryServiceImpl(httpClientService, caseOpeningParser, userInput.getCookie());

        List<CaseOpeningEvent> newEvents;
        if (latestCachedDate != null) {
            logger.info("Cached events found. Latest cached event date: {}", latestCachedDate);
            newEvents = inventoryHistoryService.fetchInventoryHistory(userInput.getBaseUrl(), latestCachedDate);
        } else {
            logger.info("No cached events found. Fetching complete inventory history.");
            newEvents = inventoryHistoryService.fetchInventoryHistory(userInput.getBaseUrl());
        }

        // Merge cached events with new events (avoiding duplicates)
        List<CaseOpeningEvent> allEvents = CachedResultsHandler.mergeEvents(cachedEvents, newEvents);

        if (allEvents.isEmpty()) {
            System.out.println(TerminalColor.colorize("No case opening events found. Make sure your inventory history is public.", TerminalColor.RED));
            return;
        }

        System.out.println(TerminalColor.colorize("Analyzing case openings...", TerminalColor.YELLOW));
        CaseOpeningSummary summary = inventoryHistoryService.summarizeCaseOpenings(allEvents);

        if (summary == null) {
            System.out.println(TerminalColor.colorize("Failed to generate summary. Please check the log file for more details.", TerminalColor.RED));
            return;
        }

        System.out.println(TerminalColor.colorize("\nAnalysis Complete!", TerminalColor.GREEN));
        System.out.println(summary);

        // Save merged results to fixed file paths (both human-readable and JSON cache)
        CachedResultsHandler.saveCachedEvents(accountId, allEvents, summary);

        System.out.println(TerminalColor.colorize("\nDetailed Case Opening Events:", TerminalColor.BLUE));
        allEvents.forEach(System.out::println);

        System.out.println(TerminalColor.colorize("\nThank you for using the Steam Inventory History Tool!", TerminalColor.GREEN));
    }

    /**
     * Extracts the account id from a given base URL.
     * Assumes the URL contains "/id/{accountId}/inventoryhistory/"
     */
    private static String extractAccountId(String baseUrl) {
        // Example: "https://steamcommunity.com/id/cla33ic/inventoryhistory/"
        String marker = "/id/";
        int start = baseUrl.indexOf(marker);
        if (start == -1) return "default";
        start += marker.length();
        int end = baseUrl.indexOf("/", start);
        if (end == -1) return baseUrl.substring(start);
        return baseUrl.substring(start, end);
    }
}
