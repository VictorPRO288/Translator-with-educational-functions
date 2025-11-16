package com.translator.main.service;

import com.translator.main.dto.AnalysisRequest;
import com.translator.main.dto.AnalysisResponse;
import com.translator.main.dto.HintSuggestion;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.Russian;
import org.languagetool.rules.RuleMatch;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GrammarAnalysisService {

    public AnalysisResponse analyze(AnalysisRequest request) {
        String language = request.getTargetLanguage() != null ? request.getTargetLanguage() : "en";
        JLanguageTool tool = buildLanguageTool(language);
        List<HintSuggestion> suggestions = new ArrayList<>();
        if (tool == null) {
            return new AnalysisResponse(suggestions);
        }

        String textToCheck = request.getTranslatedText() != null ? request.getTranslatedText() : request.getSourceText();
        if (textToCheck == null || textToCheck.isBlank()) {
            return new AnalysisResponse(suggestions);
        }

        try {
            List<RuleMatch> matches = tool.check(textToCheck);
            for (RuleMatch match : matches) {
                String replacement = match.getSuggestedReplacements().isEmpty() ? null : match.getSuggestedReplacements().get(0);
                HintSuggestion hs = new HintSuggestion(
                        UUID.randomUUID().toString(),
                        match.getRule().isDictionaryBasedSpellingRule() ? "spelling" : "grammar",
                        match.getMessage(),
                        replacement,
                        match.getFromPos(),
                        match.getToPos() - match.getFromPos(),
                        match.getShortMessage()
                );
                suggestions.add(hs);
            }
        } catch (IOException e) {
            // swallow for now; in production log it
        }

        return new AnalysisResponse(suggestions);
    }

    private JLanguageTool buildLanguageTool(String langCode) {
        switch (langCode.toLowerCase()) {
            case "en":
            case "en-us":
                return new JLanguageTool(new AmericanEnglish());
            case "en-gb":
                return new JLanguageTool(new BritishEnglish());
            case "ru":
                return new JLanguageTool(new Russian());
            default:
                return null;
        }
    }
}


