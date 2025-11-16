package com.translator.main.dto;

import java.util.List;

public class QuizSubmissionRequest {
    private Long quizId;
    private List<AnswerSubmission> answers;
    
    public QuizSubmissionRequest() {}
    
    public QuizSubmissionRequest(Long quizId, List<AnswerSubmission> answers) {
        this.quizId = quizId;
        this.answers = answers;
    }
    
    public Long getQuizId() {
        return quizId;
    }
    
    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }
    
    public List<AnswerSubmission> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<AnswerSubmission> answers) {
        this.answers = answers;
    }
    
    public static class AnswerSubmission {
        private Long questionId;
        private String answer;
        
        public AnswerSubmission() {}
        
        public AnswerSubmission(Long questionId, String answer) {
            this.questionId = questionId;
            this.answer = answer;
        }
        
        public Long getQuestionId() {
            return questionId;
        }
        
        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }
        
        public String getAnswer() {
            return answer;
        }
        
        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}
