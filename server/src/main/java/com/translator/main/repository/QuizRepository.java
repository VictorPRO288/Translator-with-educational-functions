package com.translator.main.repository;

import com.translator.main.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByOriginalTextAndTranslatedText(String originalText, String translatedText);
    Optional<Quiz> findById(Long id);
}
