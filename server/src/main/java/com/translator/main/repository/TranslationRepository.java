package com.translator.main.repository;

import com.translator.main.model.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Integer> {

    @Query("SELECT t FROM Translation t WHERE t.targetLang = :targetLang")
    List<Translation> findByTargetLang(@Param("targetLang") String targetLang);
}
