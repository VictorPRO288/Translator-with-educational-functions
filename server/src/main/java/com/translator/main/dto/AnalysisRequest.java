package com.translator.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    private String sourceLanguage;
    private String targetLanguage;
    private String sourceText;
    private String translatedText;
}


