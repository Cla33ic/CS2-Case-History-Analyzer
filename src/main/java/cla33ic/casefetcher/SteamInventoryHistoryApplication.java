package cla33ic.casefetcher;

import cla33ic.casefetcher.cache.CacheService;
import cla33ic.casefetcher.cache.InMemoryCacheService;
import cla33ic.casefetcher.config.LoggingConfig;
import cla33ic.casefetcher.model.CaseOpeningEvent;
import cla33ic.casefetcher.model.CaseOpeningSummary;
import cla33ic.casefetcher.model.UserInput;
import cla33ic.casefetcher.service.http.HttpClientService;
import cla33ic.casefetcher.service.http.HttpClientServiceImpl;
import cla33ic.casefetcher.service.inventory.InventoryHistoryService;
import cla33ic.casefetcher.service.inventory.InventoryHistoryServiceImpl;
import cla33ic.casefetcher.service.market.SteamMarketService;
import cla33ic.casefetcher.service.market.SteamMarketServiceImpl;
import cla33ic.casefetcher.service.parser.CaseOpeningParser;
import cla33ic.casefetcher.service.parser.CaseOpeningParserImpl;
import cla33ic.casefetcher.util.TerminalColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TreeMap;

public class SteamInventoryHistoryApplication {
    private static final Logger logger = LoggerFactory.getLogger(SteamInventoryHistoryApplication.class);

    /**
     * Main method for the Steam Inventory History Tool application.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LoggingConfig.configureLogging();
        logger.info("Starting Steam Inventory History Application");
        logger.info("Current file.encoding: " + System.getProperty("file.encoding"));
        logger.info("Default Charset: " + Charset.defaultCharset());

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
        System.out.println("This tool will help you analyze your CS:GO case openings.");

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
     * @throws Exception if an error occurs during application execution
     */
    private static void runApplication(UserInput userInput) throws Exception {
        System.out.println(TerminalColor.colorize("Fetching inventory history... This may take a while.", TerminalColor.YELLOW));

        HttpClientService httpClientService = new HttpClientServiceImpl();
        httpClientService.setCookie(userInput.getCookie());

        CacheService<String, TreeMap<LocalDate, Double>> cacheService = new InMemoryCacheService<>();
        SteamMarketService steamMarketService = new SteamMarketServiceImpl(httpClientService, cacheService, userInput.getCookie());
        CaseOpeningParser caseOpeningParser = new CaseOpeningParserImpl(steamMarketService);
        InventoryHistoryService inventoryHistoryService = new InventoryHistoryServiceImpl(httpClientService, caseOpeningParser, userInput.getCookie());

        List<CaseOpeningEvent> caseOpeningEvents = inventoryHistoryService.fetchInventoryHistory(userInput.getBaseUrl());

        if (caseOpeningEvents.isEmpty()) {
            System.out.println(TerminalColor.colorize("No case opening events found. Make sure your inventory history is public.", TerminalColor.RED));
            return;
        }

        System.out.println(TerminalColor.colorize("Analyzing case openings...", TerminalColor.YELLOW));
        CaseOpeningSummary summary = inventoryHistoryService.summarizeCaseOpenings(caseOpeningEvents);

        if (summary == null) {
            System.out.println(TerminalColor.colorize("Failed to generate summary. Please check the log file for more details.", TerminalColor.RED));
            return;
        }

        System.out.println(TerminalColor.colorize("\nAnalysis Complete!", TerminalColor.GREEN));
        System.out.println(summary);

        saveResults(caseOpeningEvents, summary);

        System.out.println(TerminalColor.colorize("\nDetailed Case Opening Events:", TerminalColor.BLUE));
        caseOpeningEvents.forEach(System.out::println);

        System.out.println(TerminalColor.colorize("\nThank you for using the Steam Inventory History Tool!", TerminalColor.GREEN));
    }

    /**
     * Save the case opening results to a text file.
     * @param events the list of case opening events
     * @param summary the case opening summary
     * @throws IOException if an I/O error occurs
     */
    private static void saveResults(List<CaseOpeningEvent> events, CaseOpeningSummary summary) throws IOException {
        String fileName = "case_opening_results_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName))) {
            writer.write(summary.toString());
            writer.write("\n\nDetailed Case Opening Events:\n");
            for (CaseOpeningEvent event : events) {
                writer.write(event.toString() + "\n");
            }
        }
        System.out.println(TerminalColor.colorize("Results saved to " + fileName, TerminalColor.GREEN));
    }
}