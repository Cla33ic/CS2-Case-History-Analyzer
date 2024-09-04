package cla33ic.casefetcher.service.inventory;

import cla33ic.casefetcher.model.CaseOpeningEvent;
import cla33ic.casefetcher.model.CaseOpeningSummary;
import cla33ic.casefetcher.service.http.HttpClientService;
import cla33ic.casefetcher.service.parser.CaseOpeningParser;
import cla33ic.casefetcher.util.PaginationHelper;
import cla33ic.casefetcher.util.UrlBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InventoryHistoryServiceImpl implements InventoryHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryHistoryServiceImpl.class);
    private static final long DELAY_BETWEEN_REQUESTS = 1000; // 1 second delay

    private final HttpClientService httpClientService;
    private final CaseOpeningParser caseOpeningParser;
    private final String cookie;
    private final Gson gson;

    public InventoryHistoryServiceImpl(HttpClientService httpClientService, CaseOpeningParser caseOpeningParser, String cookie) {
        this.httpClientService = httpClientService;
        this.caseOpeningParser = caseOpeningParser;
        this.cookie = cookie;
        this.gson = new GsonBuilder().setLenient().create();
    }

    @Override
    public List<CaseOpeningEvent> fetchInventoryHistory(String baseUrl) {
        List<CaseOpeningEvent> allEvents = new ArrayList<>();
        Optional<String[]> cursor = Optional.empty();
        int page = 1;

        do {
            try {
                String url = UrlBuilder.buildUrl(baseUrl, cookie, cursor.map(c -> c[0]).orElse(null),
                        cursor.map(c -> c[1]).orElse(null), cursor.map(c -> c[2]).orElse(null));

                Map<String, String> headers = new HashMap<>();
                UrlBuilder.setHeaders(headers, cookie, baseUrl);

                String response = httpClientService.get(url, headers);
                JsonObject jsonResponse = parseJsonResponse(response);

                if (jsonResponse == null || !jsonResponse.has("html")) {
                    logger.warn("Received invalid JSON response or missing 'html' field. Stopping pagination.");
                    break;
                }

                List<CaseOpeningEvent> caseOpenings = caseOpeningParser.extractCaseOpenings(jsonResponse.get("html").getAsString(), jsonResponse);
                allEvents.addAll(caseOpenings);
                PaginationHelper.logPageInfo(page, caseOpenings.size());

                cursor = UrlBuilder.extractCursor(response);
                page++;

                // Add delay between requests
                Thread.sleep(DELAY_BETWEEN_REQUESTS);
            } catch (IOException e) {
                logger.error("Error occurred during inventory history retrieval", e);
                break;
            } catch (InterruptedException e) {
                logger.error("Thread was interrupted during delay between requests", e);
                Thread.currentThread().interrupt();
                break;
            }
        } while (PaginationHelper.hasNextPage(cursor));

        logger.info("Fetched a total of {} case opening events", allEvents.size());
        return allEvents;
    }

    @Override
    public CaseOpeningSummary summarizeCaseOpenings(List<CaseOpeningEvent> events) {
        logger.info("Starting to summarize {} case opening events", events.size());

        Map<String, Integer> caseStats = new HashMap<>();
        Map<String, Integer> itemStats = new HashMap<>();
        Map<String, Long> rarityStats = new HashMap<>();
        double totalKeyCost = 0;
        double totalCaseCost = 0;

        for (CaseOpeningEvent event : events) {
            if (event == null) {
                logger.warn("Encountered a null event during summarization");
                continue;
            }

            caseStats.merge(event.caseOpened(), 1, Integer::sum);
            itemStats.merge(event.itemReceived(), 1, Integer::sum);
            rarityStats.merge(event.rarity(), 1L, Long::sum);
            totalKeyCost += 2.35; // Assuming key price is 2.35
            totalCaseCost += event.casePrice();
        }

        int totalCases = events.size();

        logger.info("Summarization complete. Total cases: {}, Total key cost: {}, Total case cost: {}",
                totalCases, totalKeyCost, totalCaseCost);

        return new CaseOpeningSummary(caseStats, itemStats, rarityStats, totalCases, totalKeyCost, totalCaseCost, events);
    }

    private JsonObject parseJsonResponse(String response) {
        try {
            return gson.fromJson(response, JsonObject.class);
        } catch (JsonSyntaxException e) {
            logger.error("Failed to parse JSON response: {}", e.getMessage());
            logger.debug("Response content: {}", response);
            return null;
        }
    }
}