package com.translator.main.response;
public class TranslationResponse {
    private String originalText;
    private String translatedText;
    private String sourceLang;
    private String targetLang;

    public TranslationResponse(String originalText, String translatedText, String sourceLang, String targetLang) {
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public String getTargetLang() {
        return targetLang;
    }
}
