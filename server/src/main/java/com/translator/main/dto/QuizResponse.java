package com.translator.main.dto;

import java.util.List;

public class QuizResponse {
    private Long id;
    private String title;
    private String originalText;
    private String translatedText;
    private String sourceLang;
    private String targetLang;
    private List<QuestionResponse> questions;
    
    public QuizResponse() {}
    
    public QuizResponse(Long id, String title, String originalText, String translatedText, 
        String sourceLang, String targetLang, List<QuestionResponse> questions) {
        this.id = id;
        this.title = title;
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
        this.questions = questions;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getOriginalText() {
        return originalText;
    }
    
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }
    
    public String getTranslatedText() {
        return translatedText;
    }
    
    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
    
    public String getSourceLang() {
        return sourceLang;
    }
    
    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }
    
    public String getTargetLang() {
        return targetLang;
    }
    
    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }
    
    public List<QuestionResponse> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }
}
