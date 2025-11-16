package com.translator.main.service;

import com.translator.main.dto.*;
import com.translator.main.model.Quiz;
import com.translator.main.model.QuizQuestion;
import com.translator.main.repository.QuizRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private GeminiService geminiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public QuizResponse generateQuiz(QuizRequest request) {
        try {
            // Check if quiz already exists
            List<Quiz> existingQuizzes = quizRepository.findByOriginalTextAndTranslatedText(
                request.getOriginalText(), request.getTranslatedText());
            
            if (!existingQuizzes.isEmpty()) {
                return convertToResponse(existingQuizzes.get(0));
            }
            
            // Generate quiz using Gemini
            String geminiResponse = geminiService.generateQuiz(
                request.getOriginalText(),
                request.getTranslatedText(),
                request.getSourceLang(),
                request.getTargetLang()
            );
            
            // Parse Gemini response
            QuizData quizData = parseGeminiResponse(geminiResponse);
            
            // Create and save quiz
            Quiz quiz = new Quiz();
            quiz.setOriginalText(request.getOriginalText());
            quiz.setTranslatedText(request.getTranslatedText());
            quiz.setSourceLang(request.getSourceLang());
            quiz.setTargetLang(request.getTargetLang());
            quiz.setTitle(quizData.getTitle());
            
            quiz = quizRepository.save(quiz);
            
            // Create and save questions
            List<QuizQuestion> questions = new ArrayList<>();
            for (int i = 0; i < quizData.getQuestions().size(); i++) {
                QuestionData questionData = quizData.getQuestions().get(i);
                QuizQuestion question = new QuizQuestion();
                question.setQuiz(quiz);
                question.setQuestion(questionData.getQuestion());
                question.setCorrectAnswer(questionData.getCorrectAnswer());
                question.setOptions(objectMapper.writeValueAsString(questionData.getOptions()));
                question.setQuestionOrder(i + 1);
                questions.add(question);
            }
            
            quiz.setQuestions(questions);
            quiz = quizRepository.save(quiz);
            
            return convertToResponse(quiz);
            
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при генерации теста: " + e.getMessage(), e);
        }
    }
    
    public QuizResultResponse submitQuiz(QuizSubmissionRequest request) {
        try {
            Optional<Quiz> quizOpt = quizRepository.findById(request.getQuizId());
            if (!quizOpt.isPresent()) {
                throw new RuntimeException("Тест не найден");
            }
            
            Quiz quiz = quizOpt.get();
            List<QuizQuestion> questions = quiz.getQuestions();
            
            int correctAnswers = 0;
            List<QuizResultResponse.QuestionResult> results = new ArrayList<>();
            
            for (QuizSubmissionRequest.AnswerSubmission submission : request.getAnswers()) {
                QuizQuestion question = questions.stream()
                    .filter(q -> q.getId().equals(submission.getQuestionId()))
                    .findFirst()
                    .orElse(null);
                
                if (question != null) {
                    boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(submission.getAnswer().trim());
                    if (isCorrect) {
                        correctAnswers++;
                    }
                    
                    results.add(new QuizResultResponse.QuestionResult(
                        question.getId(),
                        question.getQuestion(),
                        submission.getAnswer(),
                        question.getCorrectAnswer(),
                        isCorrect
                    ));
                }
            }
            
            int totalQuestions = questions.size();
            int score = (int) Math.round((double) correctAnswers / totalQuestions * 100);
            String grade = calculateGrade(score);
            
            return new QuizResultResponse(
                quiz.getId(),
                totalQuestions,
                correctAnswers,
                score,
                grade,
                results
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при обработке результатов теста: " + e.getMessage(), e);
        }
    }
    
    public QuizResponse getQuiz(Long quizId) {
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (!quizOpt.isPresent()) {
            throw new RuntimeException("Тест не найден");
        }
        return convertToResponse(quizOpt.get());
    }
    
    private QuizResponse convertToResponse(Quiz quiz) {
        List<QuestionResponse> questionResponses = quiz.getQuestions().stream()
            .sorted(Comparator.comparing(QuizQuestion::getQuestionOrder))
            .map(q -> {
                try {
                    List<String> options = objectMapper.readValue(q.getOptions(), new TypeReference<List<String>>() {});
                    return new QuestionResponse(q.getId(), q.getQuestion(), options, q.getQuestionOrder());
                } catch (Exception e) {
                    return new QuestionResponse(q.getId(), q.getQuestion(), new ArrayList<>(), q.getQuestionOrder());
                }
            })
            .collect(Collectors.toList());
        
        return new QuizResponse(
            quiz.getId(),
            quiz.getTitle(),
            quiz.getOriginalText(),
            quiz.getTranslatedText(),
            quiz.getSourceLang(),
            quiz.getTargetLang(),
            questionResponses
        );
    }
    
    private QuizData parseGeminiResponse(String response) {
        try {
            return objectMapper.readValue(response, QuizData.class);
        } catch (Exception e) {
            // Fallback parsing if JSON structure is different
            return createFallbackQuiz();
        }
    }
    
    private QuizData createFallbackQuiz() {
        QuizData quizData = new QuizData();
        quizData.setTitle("Тест на знание перевода");
        
        List<QuestionData> questions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            QuestionData question = new QuestionData();
            question.setQuestion("Вопрос " + i + ": Выберите правильный вариант");
            question.setCorrectAnswer("Вариант A");
            question.setOptions(Arrays.asList("Вариант A", "Вариант B", "Вариант C", "Вариант D"));
            questions.add(question);
        }
        quizData.setQuestions(questions);
        
        return quizData;
    }
    
    private String calculateGrade(int score) {
        if (score >= 90) return "Отлично";
        if (score >= 80) return "Хорошо";
        if (score >= 70) return "Удовлетворительно";
        if (score >= 60) return "Зачет";
        return "Незачет";
    }
    
    // Inner classes for parsing
    private static class QuizData {
        private String title;
        private List<QuestionData> questions;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<QuestionData> getQuestions() { return questions; }
        public void setQuestions(List<QuestionData> questions) { this.questions = questions; }
    }
    
    private static class QuestionData {
        private String question;
        private String correctAnswer;
        private List<String> options;
        
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
    }
}
