package cla33ic.casefetcher.service.parser;

import cla33ic.casefetcher.model.CaseOpeningEvent;
import cla33ic.casefetcher.service.market.SteamMarketService;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CaseOpeningParserImpl implements CaseOpeningParser {
    private static final Logger logger = LoggerFactory.getLogger(CaseOpeningParserImpl.class);
    private static final Map<String, Integer> MONTH_MAP = createMonthMap();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM, yyyy h:mma", Locale.ENGLISH);
    private static final Map<String, String> RARITY_COLOR_MAP = createRarityColorMap();

    private final SteamMarketService steamMarketService;

    public CaseOpeningParserImpl(SteamMarketService steamMarketService) {
        this.steamMarketService = steamMarketService;
    }

    @Override
    public List<CaseOpeningEvent> extractCaseOpenings(String html, JsonObject responseJson) {
        Document doc = Jsoup.parse(html);
        Elements rows = doc.select("div.tradehistoryrow:has(div.tradehistory_event_description:contains(Unlocked a container))");

        logger.info("Starting to extract case openings from {} rows", rows.size());

        List<CaseOpeningEvent> events = rows.stream()
                .map(row -> createCaseOpeningEvent(row, responseJson))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        logger.info("Extracted {} case opening events", events.size());
        return events;
    }

    private CaseOpeningEvent createCaseOpeningEvent(Element row, JsonObject responseJson) {
        try {
            LocalDateTime dateTime = extractDateTime(row);
            if (dateTime == null) {
                logger.error("Failed to extract date time from row");
                return null;
            }

            String containerName = extractContainerName(row);
            if (containerName == null || !containerName.toLowerCase().contains("case")) {
                return null;
            }

            String keyUsed = extractKeyUsed(row);
            boolean isRental = isRentalCase(row);
            String itemReceived = isRental ? "Rental Item" : extractItemReceived(row);
            String rarity = isRental ? "Rental" : extractRarity(responseJson, row);
            double casePrice = steamMarketService.fetchPriceForDate(containerName, dateTime.toLocalDate());

            return new CaseOpeningEvent(dateTime, containerName, keyUsed, itemReceived, rarity, casePrice, isRental);
        } catch (Exception e) {
            logger.error("Failed to create CaseOpeningEvent: {}", e.getMessage());
            return null;
        }
    }

    private boolean isRentalCase(Element row) {
        return row.text().contains("Unlocked a container for rental");
    }

    private LocalDateTime extractDateTime(Element row) {
        Element dateElement = row.selectFirst("div.tradehistory_date");
        Element timeElement = row.selectFirst("div.tradehistory_timestamp");
        if (dateElement == null || timeElement == null) return null;

        String dateStr = dateElement.ownText().trim();
        String timeStr = timeElement.text().trim();
        String dateTimeStr = dateStr + " " + timeStr;

        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return parseManually(dateStr, timeStr);
        }
    }

    private LocalDateTime parseManually(String dateStr, String timeStr) {
        try {
            String[] dateParts = dateStr.split(",");
            String[] dayMonth = dateParts[0].trim().split(" ");
            int day = Integer.parseInt(dayMonth[0]);
            int month = MONTH_MAP.get(dayMonth[1].toUpperCase());
            int year = Integer.parseInt(dateParts[1].trim());

            String[] timeParts = timeStr.toLowerCase().replace("am", "").replace("pm", "").trim().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            if (timeStr.toLowerCase().contains("pm") && hour != 12) hour += 12;
            if (timeStr.toLowerCase().contains("am") && hour == 12) hour = 0;

            return LocalDateTime.of(year, month, day, hour, minute);
        } catch (Exception e) {
            logger.error("Failed to parse date manually: {} {}", dateStr, timeStr, e);
            return null;
        }
    }

    private String extractContainerName(Element row) {
        Elements minusItems = row.select("div.tradehistory_items_plusminus:contains(-) + div.tradehistory_items_group span.history_item");
        return minusItems.isEmpty() ? null : Objects.requireNonNull(minusItems.first()).select("span.history_item_name").text();
    }

    private String extractKeyUsed(Element row) {
        Elements minusItems = row.select("div.tradehistory_items_plusminus:contains(-) + div.tradehistory_items_group span.history_item");
        return minusItems.size() > 1 ? minusItems.get(1).select("span.history_item_name").text() : "Unknown Key";
    }

    private String extractItemReceived(Element row) {
        Element plusItem = row.selectFirst("div.tradehistory_items_plusminus:contains(+) + div.tradehistory_items_group a.history_item");
        return plusItem != null ? plusItem.select("span.history_item_name").text() : null;
    }

    private String extractRarity(JsonObject responseJson, Element row) {
        Element plusItem = row.selectFirst("div.tradehistory_items_plusminus:contains(+) + div.tradehistory_items_group a.history_item");
        if (plusItem == null) return "Unknown";

        String classId = plusItem.attr("data-classid");
        String instanceId = plusItem.attr("data-instanceid");
        String key = classId + "_" + instanceId;

        if (responseJson.has("descriptions") &&
                responseJson.getAsJsonObject("descriptions").has("730") &&
                responseJson.getAsJsonObject("descriptions").getAsJsonObject("730").has(key)) {

            JsonObject itemDescription = responseJson.getAsJsonObject("descriptions")
                    .getAsJsonObject("730")
                    .getAsJsonObject(key);

            if (itemDescription.has("tags")) {
                for (var tagElement : itemDescription.getAsJsonArray("tags")) {
                    JsonObject tag = tagElement.getAsJsonObject();
                    if ("Rarity".equals(tag.get("category").getAsString())) {
                        String colorCode = tag.get("color").getAsString();
                        return RARITY_COLOR_MAP.getOrDefault(colorCode, "Unknown (" + colorCode + ")");
                    }
                }
            }
        }

        return "Unknown";
    }

    private static Map<String, Integer> createMonthMap() {
        return Map.ofEntries(
                Map.entry("JAN", 1), Map.entry("FEB", 2), Map.entry("MAR", 3), Map.entry("APR", 4),
                Map.entry("MAY", 5), Map.entry("JUN", 6), Map.entry("JUL", 7), Map.entry("AUG", 8),
                Map.entry("SEP", 9), Map.entry("OCT", 10), Map.entry("NOV", 11), Map.entry("DEC", 12)
        );
    }

    private static Map<String, String> createRarityColorMap() {
        return Map.of(
                "b0c3d9", "Consumer Grade (White)",
                "5e98d9", "Industrial Grade (Light Blue)",
                "4b69ff", "Mil-Spec (Blue)",
                "8847ff", "Restricted (Purple)",
                "d32ce6", "Classified (Pink)",
                "eb4b4b", "Covert (Red)",
                "e4ae39", "Rare Special Item (Gold)"
        );
    }
}
