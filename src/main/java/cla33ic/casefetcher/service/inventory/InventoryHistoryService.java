package cla33ic.casefetcher.service.inventory;

import cla33ic.casefetcher.model.CaseOpeningEvent;
import cla33ic.casefetcher.model.CaseOpeningSummary;

import java.util.List;

public interface InventoryHistoryService {
    List<CaseOpeningEvent> fetchInventoryHistory(String steamId);
    CaseOpeningSummary summarizeCaseOpenings(List<CaseOpeningEvent> events);
}