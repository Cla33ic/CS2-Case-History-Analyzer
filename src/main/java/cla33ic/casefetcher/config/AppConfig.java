package cla33ic.casefetcher.config;

public class AppConfig {
    // HTTP Client settings
    public static final int INITIAL_DELAY = 1000; // 1 second

    // Steam Market settings
    public static final String STEAM_MARKET_BASE_URL = "https://steamcommunity.com/market/listings/730/";

    // Case opening settings
    public static final double KEY_PRICE = 2.35;

    // Cache settings
    public static final int CACHE_EXPIRATION_HOURS = 24;

    private AppConfig() {
        // Private constructor to prevent instantiation
    }
}