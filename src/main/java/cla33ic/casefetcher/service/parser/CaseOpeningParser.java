package cla33ic.casefetcher.service.parser;

import cla33ic.casefetcher.model.CaseOpeningEvent;
import cla33ic.casefetcher.model.CaseOpeningSummary;
import com.google.gson.JsonObject;

import java.util.List;

public interface CaseOpeningParser {
    List<CaseOpeningEvent> extractCaseOpenings(String html, JsonObject responseJson);
    CaseOpeningSummary summarizeWeaponCases(List<CaseOpeningEvent> events);
}