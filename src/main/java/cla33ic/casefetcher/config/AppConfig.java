package cla33ic.casefetcher.config;

public class AppConfig {

    // Steam Market settings
    public static final String STEAM_MARKET_BASE_URL = "https://steamcommunity.com/market/listings/730/";

    // Case opening settings
    public static final double KEY_PRICE = 2.35;

    // Cache settings
    public static final int CACHE_EXPIRATION_HOURS = 24;

    // Request delay (in milliseconds) between Steam API calls to respect rate limiting
    public static final long STEAM_REQUEST_DELAY_MS = 1000;

    private AppConfig() {
        // Private constructor to prevent instantiation
    }
}
