package com.translator.main.service;

import com.translator.main.dto.FeedbackRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class FeedbackService {

    private final ConcurrentMap<String, Integer> suggestionFeedbackCounters = new ConcurrentHashMap<>();

    public void recordFeedback(FeedbackRequest request) {
        suggestionFeedbackCounters.merge(request.getSuggestionId() + ":applied:" + request.isApplied(), 1, Integer::sum);
    }
}


