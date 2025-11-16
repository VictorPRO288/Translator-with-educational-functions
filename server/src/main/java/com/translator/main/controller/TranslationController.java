package com.translator.main.controller;

import com.translator.main.dto.BulkTranslationRequest;
import com.translator.main.dto.TranslationRequest;
import com.translator.main.exception.ErrorDetails;
import com.translator.main.model.Translation;
import com.translator.main.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Controller
@RequestMapping("/api/translations")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @GetMapping("/")
    public String mainWebPage() {
        return "index";
    }

    @GetMapping("/languages")
    public Map<String, String> getSupportedLanguages() {
        return Translation.SUPPORTED_LANGUAGES;
    }

    @PostMapping
    public ResponseEntity<Translation> createTranslation(
            @RequestBody TranslationRequest request) {
        try {
            Translation translation = translationService.translateAndSave(
                    request.getText(),
                    request.getSourceLang(),
                    request.getTargetLang()
            );
            return ResponseEntity.ok(translation);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTranslations() {
        try {
            List<Translation> translations = translationService.getAllTranslations();
            return ResponseEntity.ok(translations);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTranslationById(@PathVariable Integer id) {
        try {
            return translationService.getTranslationById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/by-language")
    public ResponseEntity<?> getTranslationsByTargetLang(@RequestParam String targetLang) {
        try {
            List<Translation> translations = translationService.getTranslationsByTargetLang(targetLang);
            if (translations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorDetails(new Date(), "No translations found for language: " + targetLang, ""));
            }
            return ResponseEntity.ok(translations);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<?> getUsersByTranslationId(@PathVariable Integer id) {
        try {
            return translationService.getTranslationById(id)
                    .map(translation -> ResponseEntity.ok(translation.getUsers()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> translateBulk(@RequestBody BulkTranslationRequest request) {
        try {
            List<Translation> translations = translationService.translateBulk(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(translations);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTranslation(@PathVariable Integer id) {
        try {
            translationService.deleteTranslationById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private ResponseEntity<ErrorDetails> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDetails(new Date(), "An error occurred", e.getMessage()));
    }
}
