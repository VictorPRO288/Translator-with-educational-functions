package com.translator.main.dto;

import java.util.List;

public class QuizResultResponse {
    private Long quizId;
    private int totalQuestions;
    private int correctAnswers;
    private int score;
    private String grade;
    private List<QuestionResult> results;
    
    public QuizResultResponse() {}
    
    public QuizResultResponse(Long quizId, int totalQuestions, int correctAnswers, 
                            int score, String grade, List<QuestionResult> results) {
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.score = score;
        this.grade = grade;
        this.results = results;
    }
    
    public Long getQuizId() {
        return quizId;
    }
    
    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }
    
    public int getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public List<QuestionResult> getResults() {
        return results;
    }
    
    public void setResults(List<QuestionResult> results) {
        this.results = results;
    }
    
    public static class QuestionResult {
        private Long questionId;
        private String question;
        private String userAnswer;
        private String correctAnswer;
        private boolean isCorrect;
        
        public QuestionResult() {}
        
        public QuestionResult(Long questionId, String question, String userAnswer, 
                            String correctAnswer, boolean isCorrect) {
            this.questionId = questionId;
            this.question = question;
            this.userAnswer = userAnswer;
            this.correctAnswer = correctAnswer;
            this.isCorrect = isCorrect;
        }
        
        public Long getQuestionId() {
            return questionId;
        }
        
        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }
        
        public String getQuestion() {
            return question;
        }
        
        public void setQuestion(String question) {
            this.question = question;
        }
        
        public String getUserAnswer() {
            return userAnswer;
        }
        
        public void setUserAnswer(String userAnswer) {
            this.userAnswer = userAnswer;
        }
        
        public String getCorrectAnswer() {
            return correctAnswer;
        }
        
        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }
        
        public boolean isCorrect() {
            return isCorrect;
        }
        
        public void setCorrect(boolean correct) {
            isCorrect = correct;
        }
    }
}
