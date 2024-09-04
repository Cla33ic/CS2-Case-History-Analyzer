package cla33ic.casefetcher.service.market;

import java.time.LocalDate;

public interface SteamMarketService {
    double fetchPriceForDate(String itemName, LocalDate date);
}