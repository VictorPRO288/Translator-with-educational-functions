package com.translator.main.service;

import com.translator.main.dto.AnalysisRequest;
import com.translator.main.dto.AnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HintService {

    private final GrammarAnalysisService grammarAnalysisService;

    public AnalysisResponse analyze(AnalysisRequest request) {
        return grammarAnalysisService.analyze(request);
    }
}


