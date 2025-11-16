package com.translator.main.service;

import com.translator.main.cache.TranslationCache;
import com.translator.main.dto.BulkTranslationRequest;
import com.translator.main.model.Translation;
import com.translator.main.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TranslationService {

    private static final String TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl={sourceLang}&tl={targetLang}&dt=t&q={text}";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RequestCounterService requestCounterService;
    private final TranslationRepository translationRepository;
    private final TranslationCache translationCache;

    @Autowired
    public TranslationService(TranslationRepository translationRepository,
        TranslationCache translationCache,
        RequestCounterService requestCounterService) {
        this.translationRepository = translationRepository;
        this.translationCache = translationCache;
        this.requestCounterService = requestCounterService;
    }

    public List<Translation> translateBulk(BulkTranslationRequest request) {
        requestCounterService.increment();
        return request.getTexts().stream()
                .map(text -> translateText(text, request.getSourceLang(), request.getTargetLang()))
                .map(this::saveTranslation)
                .collect(Collectors.toList());
    }

    public Translation translateAndSave(String text, String sourceLang, String targetLang) {
        requestCounterService.increment();
        Translation translation = translateText(text, sourceLang, targetLang);
        return saveTranslation(translation);
    }

    private Translation translateText(String text, String sourceLang, String targetLang) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    TRANSLATE_URL, String.class, sourceLang, targetLang, text);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode translatedTextNode = rootNode.get(0).get(0).get(0);
            String translatedText = translatedTextNode.asText();

            Translation translation = new Translation();
            translation.setOriginalText(text);
            translation.setTranslatedText(translatedText);
            translation.setSourceLang(sourceLang);
            translation.setTargetLang(targetLang);

            return translation;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при переводе текста: " + e.getMessage(), e);
        }
    }

    private Translation saveTranslation(Translation translation) {
        return translationRepository.save(translation);
    }

    public List<Translation> getTranslationsByTargetLang(String targetLang) {
        requestCounterService.increment();

        List<Translation> cachedTranslations = translationCache.get(targetLang);
        if (cachedTranslations != null) {
            return cachedTranslations;
        }

        List<Translation> translations = translationRepository.findByTargetLang(targetLang);
        translationCache.put(targetLang, translations);
        return translations;
    }

    public List<Translation> getAllTranslations() {
        requestCounterService.increment();
        return translationRepository.findAll();
    }

    public Optional<Translation> getTranslationById(Integer id) {
        requestCounterService.increment();
        return translationRepository.findById(id);
    }

    public void deleteTranslationById(Integer id) {
        requestCounterService.increment();
        translationRepository.deleteById(id);
    }
}
