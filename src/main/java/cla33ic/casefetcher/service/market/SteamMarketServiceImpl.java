package cla33ic.casefetcher.service.market;

import cla33ic.casefetcher.cache.CacheService;
import cla33ic.casefetcher.config.AppConfig;
import cla33ic.casefetcher.service.http.HttpClientService;
import cla33ic.casefetcher.util.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SteamMarketServiceImpl implements SteamMarketService, HttpClientService {
    private static final Logger logger = LoggerFactory.getLogger(SteamMarketServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.US);
    private static final double DEFAULT_PRICE = 0.01;

    private final CacheService<String, TreeMap<LocalDate, Double>> cacheService;
    private final HttpClientService httpClientService;
    private String cookie;

    // Rate limiter to enforce a delay between requests (~1 every 2400ms for 25 req/min)
    private final RateLimiter rateLimiter = new RateLimiter(2400);
    // Executor for asynchronous operations (if needed in the future)
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    public SteamMarketServiceImpl(HttpClientService httpClientService,
                                  CacheService<String, TreeMap<LocalDate, Double>> cacheService,
                                  String cookie) {
        this.httpClientService = httpClientService;
        this.cacheService = cacheService;
        this.cookie = cookie;
    }

    @Override
    public double fetchPriceForDate(String itemName, LocalDate date) {
        try {
            TreeMap<LocalDate, Double> priceData = cacheService.get(itemName)
                    .orElseGet(() -> {
                        try {
                            TreeMap<LocalDate, Double> fetchedData = fetchPriceData(itemName);
                            cacheService.put(itemName, fetchedData);
                            return fetchedData;
                        } catch (IOException e) {
                            logger.error("Failed to fetch price data for {}: {}", itemName, e.getMessage());
                            return new TreeMap<>();
                        }
                    });

            // Try to get the exact price for the date
            Double exactPrice = priceData.get(date);
            if (exactPrice != null) {
                return exactPrice;
            }

            // If not found, get the closest date
            Map.Entry<LocalDate, Double> closestEntry = priceData.floorEntry(date);
            if (closestEntry == null) {
                closestEntry = priceData.ceilingEntry(date);
            }

            if (closestEntry != null) {
                logger.info("Using price from {} for {} on {}", closestEntry.getKey(), itemName, date);
                return closestEntry.getValue();
            }

            logger.warn("No price data available for {} on or near {}", itemName, date);
            return DEFAULT_PRICE;
        } catch (Exception e) {
            logger.error("Error fetching price for {} on {}: {}", itemName, date, e.getMessage());
            return DEFAULT_PRICE;
        }
    }

    private TreeMap<LocalDate, Double> fetchPriceData(String caseName) throws IOException {
        // Ensure we respect the rate limit before making the request
        rateLimiter.acquire();
        String url = AppConfig.STEAM_MARKET_BASE_URL + caseName.replace(" ", "%20");
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "steamLoginSecure=" + cookie + "; Steam_Language=english");

        String response = get(url, headers);
        Document doc = Jsoup.parse(response);
        String scriptContent = extractScriptContent(doc);
        return extractPriceData(scriptContent);
    }

    private String extractScriptContent(Document doc) {
        return doc.select("script").stream()
                .filter(element -> element.html().contains("$J(document).ready(function()"))
                .findFirst()
                .map(Element::html)
                .orElse("");
    }

    private TreeMap<LocalDate, Double> extractPriceData(String scriptContent) {
        Pattern pattern = Pattern.compile("\\$J\\(document\\)\\.ready\\(function\\(\\)\\s*\\{\\s*var line1=(\\[.*?]);", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(scriptContent);

        if (matcher.find()) {
            String line1Content = matcher.group(1);
            Gson gson = new Gson();
            List<List<Object>> rawData = gson.fromJson(line1Content, new TypeToken<List<List<Object>>>(){}.getType());

            TreeMap<LocalDate, Double> priceData = new TreeMap<>();
            for (List<Object> entry : rawData) {
                try {
                    String entryStr = (String) entry.getFirst();
                    String[] parts = entryStr.split(" ");
                    String dateStr = parts[0] + " " + parts[1] + " " + parts[2];
                    LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
                    double price = ((Number) entry.get(1)).doubleValue();
                    priceData.put(date, price);
                } catch (Exception e) {
                    logger.warn("Skipping entry due to parsing error: {}", e.getMessage());
                }
            }
            return priceData;
        }

        return new TreeMap<>();
    }

    @Override
    public String get(String url, Map<String, String> headers) throws IOException {
        return httpClientService.get(url, headers);
    }

    @Override
    public String post(String url, Map<String, String> headers, String body) throws IOException {
        return httpClientService.post(url, headers, body);
    }

    @Override
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
