package cla33ic.casefetcher.util;

import cla33ic.casefetcher.model.CaseOpeningEvent;
import cla33ic.casefetcher.model.CaseOpeningSummary;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CachedResultsHandler {
    private static final Logger logger = LoggerFactory.getLogger(CachedResultsHandler.class);
    private static final String RESULTS_DIR = "results";

    // Updated Gson instance without .setLenient() and with custom LocalDateTime adapter.
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    // We store the raw events in a JSON cache for easy reloading.
    // Also generate a human-readable text file.
    // File names: {accountId}_case_opening_results.txt and {accountId}_cache.json
    public static void saveCachedEvents(String accountId, List<CaseOpeningEvent> events, CaseOpeningSummary summary) {
        try {
            // Ensure results directory exists
            Path resultsDir = Paths.get(RESULTS_DIR);
            if (!Files.exists(resultsDir)) {
                Files.createDirectory(resultsDir);
            }
            // Save human-readable text file
            String txtFileName = RESULTS_DIR + "/" + accountId + "_case_opening_results.txt";
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(txtFileName))) {
                writer.write(summary.toString());
                writer.write("\n\nDetailed Case Opening Events:\n");
                for (CaseOpeningEvent event : events) {
                    writer.write(event.toString());
                    writer.newLine();
                }
            }
            // Save JSON cache file
            String jsonFileName = RESULTS_DIR + "/" + accountId + "_cache.json";
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(jsonFileName))) {
                writer.write(gson.toJson(events));
            }
            logger.info("Results saved to {} and cache updated in {}", txtFileName, jsonFileName);
            System.out.println(TerminalColor.colorize("Results saved to " + txtFileName, TerminalColor.GREEN));
        } catch (IOException e) {
            logger.error("Error saving cached results: {}", e.getMessage());
        }
    }

    public static List<CaseOpeningEvent> loadCachedEvents(String accountId) {
        String jsonFileName = RESULTS_DIR + "/" + accountId + "_cache.json";
        Path path = Paths.get(jsonFileName);
        if (!Files.exists(path)) {
            logger.info("No cache file found for account {}", accountId);
            return new ArrayList<>();
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            Type listType = new TypeToken<List<CaseOpeningEvent>>(){}.getType();
            List<CaseOpeningEvent> events = gson.fromJson(reader, listType);
            logger.info("Loaded {} cached events for account {}", events.size(), accountId);
            return events;
        } catch (IOException e) {
            logger.error("Error loading cached events: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Merge cached events with newly fetched events. Assumes that events are unique based on their toString output.
     */
    public static List<CaseOpeningEvent> mergeEvents(List<CaseOpeningEvent> cached, List<CaseOpeningEvent> fresh) {
        // Use a LinkedHashMap to preserve order (most recent first) and avoid duplicates.
        // We'll key on the toString representation.
        java.util.Map<String, CaseOpeningEvent> merged = new java.util.LinkedHashMap<>();
        // Add cached events first
        for (CaseOpeningEvent event : cached) {
            merged.put(event.toString(), event);
        }
        // Add new events (they should be more recent, but in case of overlap, they overwrite)
        for (CaseOpeningEvent event : fresh) {
            merged.put(event.toString(), event);
        }
        return new ArrayList<>(merged.values());
    }
}
