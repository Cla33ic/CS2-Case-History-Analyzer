package cla33ic.casefetcher.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record CaseOpeningSummary(
        Map<String, Integer> caseStats,
        Map<String, Integer> itemStats,
        Map<String, Long> rarityStats,
        int totalCases,
        double totalKeyCost,
        double totalCaseCost,
        List<CaseOpeningEvent> events
) {
    public double getTotalCost() {
        return totalKeyCost + totalCaseCost;
    }

    public Map<String, Double> getCasePercentages() {
        return caseStats.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (double) e.getValue() / totalCases * 100
                ));
    }

    public Map<String, Double> getRarityPercentages() {
        return rarityStats.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (double) e.getValue() / totalCases * 100
                ));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\nCase Opening Summary:\n\n");
        sb.append(String.format("Total cases opened: %d%n", totalCases));
        sb.append(String.format("Total cost for keys: %.2f€%n", totalKeyCost));
        sb.append(String.format("Total cost for cases: %.2f€%n", totalCaseCost));
        sb.append(String.format("Total cost: %.2f€%n%n", getTotalCost()));

        sb.append("Cases Opened:\n");
        getCasePercentages().forEach((cae, percentage) ->
                sb.append(String.format("%s: %d (%.2f%%)%n", cae, caseStats.get(cae), percentage)));

        sb.append("\nItems Received by Rarity:\n");
        getRarityPercentages().forEach((rarity, percentage) ->
                sb.append(String.format("%s: %d (%.2f%%)%n", rarity, rarityStats.get(rarity), percentage)));

        return sb.toString();
    }
}