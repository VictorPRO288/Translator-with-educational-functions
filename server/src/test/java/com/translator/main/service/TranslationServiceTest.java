package com.translator.main.service;

import com.translator.main.dto.BulkTranslationRequest;
import com.translator.main.model.Translation;
import com.translator.main.repository.TranslationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.testng.annotations.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TranslationServiceTest {

    @Mock
    private TranslationRepository translationRepository;

    @InjectMocks
    private TranslationService translationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTranslateBulk() {
        // Подготовка данных
        BulkTranslationRequest request = new BulkTranslationRequest();
        request.setTexts(Arrays.asList("Hello", "World"));
        request.setSourceLang("en");
        request.setTargetLang("ru");

        Translation translation1 = new Translation();
        translation1.setOriginalText("Hello");
        translation1.setTranslatedText("Привет");

        Translation translation2 = new Translation();
        translation2.setOriginalText("World");
        translation2.setTranslatedText("Мир");

        when(translationRepository.save(any(Translation.class)))
                .thenReturn(translation1)
                .thenReturn(translation2);

        // Вызов метода
        List<Translation> result = translationService.translateBulk(request);

        // Проверка результата
        assertEquals(2, result.size());
        assertEquals("Привет", result.get(0).getTranslatedText());
        assertEquals("Мир", result.get(1).getTranslatedText());
    }

    @Test
    void testTranslateAndSave() {
        // Подготовка данных
        Translation translation = new Translation();
        translation.setOriginalText("Hello");
        translation.setTranslatedText("Привет");

        when(translationRepository.save(any(Translation.class))).thenReturn(translation);

        // Вызов метода
        Translation result = translationService.translateAndSave("Hello", "en", "ru");

        // Проверка результата
        assertEquals("Привет", result.getTranslatedText());
    }
}