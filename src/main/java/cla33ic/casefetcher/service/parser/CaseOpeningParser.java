package cla33ic.casefetcher.service.parser;

import cla33ic.casefetcher.model.CaseOpeningEvent;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Interface for parsing case opening events from HTML and JSON data.
 */
public interface CaseOpeningParser {
    List<CaseOpeningEvent> extractCaseOpenings(String html, JsonObject responseJson);
}
