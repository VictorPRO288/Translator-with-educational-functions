package com.translator.main.service;

import com.translator.main.dto.QuestionResponse;
import com.translator.main.dto.QuizRequest;
import com.translator.main.dto.QuizResponse;
import com.translator.main.model.Quiz;
import com.translator.main.model.QuizQuestion;
import com.translator.main.repository.QuizRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Тестирование модуля создания квиза")
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private com.translator.main.service.GeminiService geminiService;

    @InjectMocks
    private com.translator.main.service.QuizService quizService;

    private QuizRequest quizRequest;
    private Quiz existingQuiz;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Подготовка тестовых данных
        quizRequest = new QuizRequest();
        quizRequest.setOriginalText("Hello");
        quizRequest.setTranslatedText("Привет");
        quizRequest.setSourceLang("en");
        quizRequest.setTargetLang("ru");

        // Создание существующего квиза для теста кэширования
        existingQuiz = createTestQuiz(1L, "Test Quiz");
    }

    @Test
    @DisplayName("Успешное создание нового квиза")
    void testGenerateQuiz_Success() throws JsonProcessingException {
        // Arrange
        String geminiResponse = createValidGeminiResponse();
        
        when(quizRepository.findByOriginalTextAndTranslatedText(
                quizRequest.getOriginalText(), quizRequest.getTranslatedText()))
                .thenReturn(Collections.emptyList());
        when(geminiService.generateQuiz(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(geminiResponse);
        when(quizRepository.save(any(Quiz.class)))
                .thenAnswer(invocation -> {
                    Quiz quiz = invocation.getArgument(0);
                    quiz.setId(1L);
                    return quiz;
                });

        // Act
        QuizResponse result = quizService.generateQuiz(quizRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Тест на знание перевода", result.getTitle());
        assertEquals("Hello", result.getOriginalText());
        assertEquals("Привет", result.getTranslatedText());
        assertEquals("en", result.getSourceLang());
        assertEquals("ru", result.getTargetLang());
        assertNotNull(result.getQuestions());
        assertEquals(5, result.getQuestions().size());

        // Проверка, что все вопросы созданы правильно
        for (int i = 0; i < result.getQuestions().size(); i++) {
            QuestionResponse question = result.getQuestions().get(i);
            assertNotNull(question);
            assertEquals(i + 1, question.getQuestionOrder());
            assertNotNull(question.getQuestion());
            assertNotNull(question.getOptions());
            assertEquals(4, question.getOptions().size());
        }

        // Проверка вызовов
        verify(quizRepository, times(1)).findByOriginalTextAndTranslatedText(
                quizRequest.getOriginalText(), quizRequest.getTranslatedText());
        verify(geminiService, times(1)).generateQuiz(
                quizRequest.getOriginalText(),
                quizRequest.getTranslatedText(),
                quizRequest.getSourceLang(),
                quizRequest.getTargetLang());
        verify(quizRepository, times(2)).save(any(Quiz.class)); // Два раза: сначала без вопросов, потом с вопросами
    }

    @Test
    @DisplayName("Возврат существующего квиза, если он уже создан")
    void testGenerateQuiz_ReturnsExistingQuiz() {
        // Arrange
        List<Quiz> existingQuizzes = Arrays.asList(existingQuiz);
        
        when(quizRepository.findByOriginalTextAndTranslatedText(
                quizRequest.getOriginalText(), quizRequest.getTranslatedText()))
                .thenReturn(existingQuizzes);

        // Act
        QuizResponse result = quizService.generateQuiz(quizRequest);

        // Assert
        assertNotNull(result);
        assertEquals(existingQuiz.getId(), result.getId());
        assertEquals(existingQuiz.getTitle(), result.getTitle());

        // Проверка, что Gemini не вызывался
        verify(geminiService, never()).generateQuiz(
                anyString(), anyString(), anyString(), anyString());
        verify(quizRepository, times(1)).findByOriginalTextAndTranslatedText(
                quizRequest.getOriginalText(), quizRequest.getTranslatedText());
    }

    @Test
    @DisplayName("Обработка ошибки при генерации квиза через Gemini")
    void testGenerateQuiz_GeminiError() {
        // Arrange
        when(quizRepository.findByOriginalTextAndTranslatedText(
                quizRequest.getOriginalText(), quizRequest.getTranslatedText()))
                .thenReturn(Collections.emptyList());
        when(geminiService.generateQuiz(
                anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Ошибка API"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> quizService.generateQuiz(quizRequest)
        );

        assertTrue(exception.getMessage().contains("Ошибка при генерации теста"));
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Обработка некорректного JSON ответа от Gemini с использованием fallback")
    void testGenerateQuiz_InvalidJsonFallback() throws JsonProcessingException {
        // Arrange
        String invalidJson = "{ invalid json }";
        
        when(quizRepository.findByOriginalTextAndTranslatedText(
                quizRequest.getOriginalText(), quizRequest.getTranslatedText()))
                .thenReturn(Collections.emptyList());
        when(geminiService.generateQuiz(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(invalidJson);
        when(quizRepository.save(any(Quiz.class)))
                .thenAnswer(invocation -> {
                    Quiz quiz = invocation.getArgument(0);
                    quiz.setId(1L);
                    return quiz;
                });

        // Act
        QuizResponse result = quizService.generateQuiz(quizRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Тест на знание перевода", result.getTitle());
        assertEquals(5, result.getQuestions().size());
        
        // Проверка fallback вопросов
        QuestionResponse firstQuestion = result.getQuestions().get(0);
        assertTrue(firstQuestion.getQuestion().contains("Вопрос"));
        assertEquals(4, firstQuestion.getOptions().size());
    }

    @Test
    @DisplayName("Создание квиза с минимальным набором данных")
    void testGenerateQuiz_MinimalData() throws JsonProcessingException {
        // Arrange
        QuizRequest minimalRequest = new QuizRequest();
        minimalRequest.setOriginalText("Hi");
        minimalRequest.setTranslatedText("Привет");
        minimalRequest.setSourceLang("en");
        minimalRequest.setTargetLang("ru");

        String geminiResponse = createValidGeminiResponse();
        
        when(quizRepository.findByOriginalTextAndTranslatedText(
                minimalRequest.getOriginalText(), minimalRequest.getTranslatedText()))
                .thenReturn(Collections.emptyList());
        when(geminiService.generateQuiz(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(geminiResponse);
        when(quizRepository.save(any(Quiz.class)))
                .thenAnswer(invocation -> {
                    Quiz quiz = invocation.getArgument(0);
                    quiz.setId(2L);
                    return quiz;
                });

        // Act
        QuizResponse result = quizService.generateQuiz(minimalRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Hi", result.getOriginalText());
        assertEquals("Привет", result.getTranslatedText());
        assertNotNull(result.getQuestions());
    }

    @Test
    @DisplayName("Создание квиза с длинным текстом")
    void testGenerateQuiz_LongText() throws JsonProcessingException {
        // Arrange
        QuizRequest longTextRequest = new QuizRequest();
        longTextRequest.setOriginalText("The quick brown fox jumps over the lazy dog");
        longTextRequest.setTranslatedText("Быстрая коричневая лиса прыгает через ленивую собаку");
        longTextRequest.setSourceLang("en");
        longTextRequest.setTargetLang("ru");

        String geminiResponse = createValidGeminiResponse();
        
        when(quizRepository.findByOriginalTextAndTranslatedText(
                longTextRequest.getOriginalText(), longTextRequest.getTranslatedText()))
                .thenReturn(Collections.emptyList());
        when(geminiService.generateQuiz(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(geminiResponse);
        when(quizRepository.save(any(Quiz.class)))
                .thenAnswer(invocation -> {
                    Quiz quiz = invocation.getArgument(0);
                    quiz.setId(3L);
                    return quiz;
                });

        // Act
        QuizResponse result = quizService.generateQuiz(longTextRequest);

        // Assert
        assertNotNull(result);
        assertEquals("The quick brown fox jumps over the lazy dog", result.getOriginalText());
        assertEquals("Быстрая коричневая лиса прыгает через ленивую собаку", result.getTranslatedText());
        
        verify(geminiService, times(1)).generateQuiz(
                longTextRequest.getOriginalText(),
                longTextRequest.getTranslatedText(),
                longTextRequest.getSourceLang(),
                longTextRequest.getTargetLang());
    }

    @Test
    @DisplayName("Проверка сохранения квиза с вопросами в правильном порядке")
    void testGenerateQuiz_QuestionOrder() throws JsonProcessingException {
        // Arrange
        String geminiResponse = createValidGeminiResponse();
        
        when(quizRepository.findByOriginalTextAndTranslatedText(
                quizRequest.getOriginalText(), quizRequest.getTranslatedText()))
                .thenReturn(Collections.emptyList());
        when(geminiService.generateQuiz(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(geminiResponse);
        when(quizRepository.save(any(Quiz.class)))
                .thenAnswer(invocation -> {
                    Quiz quiz = invocation.getArgument(0);
                    if (quiz.getId() == null) {
                        quiz.setId(1L);
                    }
                    return quiz;
                });

        // Act
        QuizResponse result = quizService.generateQuiz(quizRequest);

        // Assert
        assertNotNull(result.getQuestions());
        assertEquals(5, result.getQuestions().size());
        
        // Проверка порядка вопросов
        for (int i = 0; i < result.getQuestions().size(); i++) {
            assertEquals(i + 1, result.getQuestions().get(i).getQuestionOrder());
        }
    }

    @Test
    @DisplayName("Проверка, что квиз сохраняется с правильными языками")
    void testGenerateQuiz_LanguagePreservation() throws JsonProcessingException {
        // Arrange
        String geminiResponse = createValidGeminiResponse();
        
        when(quizRepository.findByOriginalTextAndTranslatedText(
                quizRequest.getOriginalText(), quizRequest.getTranslatedText()))
                .thenReturn(Collections.emptyList());
        when(geminiService.generateQuiz(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(geminiResponse);
        when(quizRepository.save(any(Quiz.class)))
                .thenAnswer(invocation -> {
                    Quiz quiz = invocation.getArgument(0);
                    quiz.setId(1L);
                    return quiz;
                });

        // Act
        QuizResponse result = quizService.generateQuiz(quizRequest);

        // Assert
        assertEquals("en", result.getSourceLang());
        assertEquals("ru", result.getTargetLang());
    }

    // Вспомогательные методы для создания тестовых данных

    private Quiz createTestQuiz(Long id, String title) {
        Quiz quiz = new Quiz();
        quiz.setId(id);
        quiz.setTitle(title);
        quiz.setOriginalText("Hello");
        quiz.setTranslatedText("Привет");
        quiz.setSourceLang("en");
        quiz.setTargetLang("ru");

        List<QuizQuestion> questions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            QuizQuestion question = new QuizQuestion();
            question.setId((long) i);
            question.setQuiz(quiz);
            question.setQuestion("Question " + i);
            question.setCorrectAnswer("Answer " + i);
            question.setOptions("[\"Answer " + i + "\", \"Option B\", \"Option C\", \"Option D\"]");
            question.setQuestionOrder(i);
            questions.add(question);
        }
        quiz.setQuestions(questions);

        return quiz;
    }

    private String createValidGeminiResponse() throws JsonProcessingException {
        return """
                {
                  "title": "Тест на знание перевода",
                  "questions": [
                    {
                      "question": "Что означает слово 'Hello' на русском?",
                      "correctAnswer": "Привет",
                      "options": ["Привет", "Пока", "Спасибо", "Извините"]
                    },
                    {
                      "question": "Какая правильная грамматическая форма?",
                      "correctAnswer": "Вариант A",
                      "options": ["Вариант A", "Вариант B", "Вариант C", "Вариант D"]
                    },
                    {
                      "question": "Выберите правильное значение",
                      "correctAnswer": "Вариант A",
                      "options": ["Вариант A", "Вариант B", "Вариант C", "Вариант D"]
                    },
                    {
                      "question": "Какая правильная структура предложения?",
                      "correctAnswer": "Вариант A",
                      "options": ["Вариант A", "Вариант B", "Вариант C", "Вариант D"]
                    },
                    {
                      "question": "Какой правильный культурный контекст?",
                      "correctAnswer": "Вариант A",
                      "options": ["Вариант A", "Вариант B", "Вариант C", "Вариант D"]
                    }
                  ]
                }
                """;
    }
}

