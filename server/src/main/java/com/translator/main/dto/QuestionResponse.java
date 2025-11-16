package com.translator.main.dto;

import java.util.List;

public class QuestionResponse {
    private Long id;
    private String question;
    private List<String> options;
    private Integer questionOrder;
    
    public QuestionResponse() {}
    
    public QuestionResponse(Long id, String question, List<String> options, Integer questionOrder) {
        this.id = id;
        this.question = question;
        this.options = options;
        this.questionOrder = questionOrder;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
    }
    
    public Integer getQuestionOrder() {
        return questionOrder;
    }
    
    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }
}
