package cla33ic.casefetcher.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record CaseOpeningEvent(
        LocalDateTime dateTime,
        String caseOpened,
        String keyUsed,
        String itemReceived,
        String rarity,
        double casePrice,
        boolean isRental
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    public String toString() {
        String dateStr = dateTime.format(DATE_FORMATTER);
        String caseStr = caseOpened + " (" + String.format("%.2f€", casePrice) + ")";
        String itemStr = isRental ? "Rental Item" : itemReceived;
        String rarityStr = "(" + rarity + ")";
        String rentalStr = isRental ? " [RENTAL]" : "";

        return String.format("%s - %s: %s %s%s", dateStr, caseStr, itemStr, rarityStr, rentalStr);
    }
}