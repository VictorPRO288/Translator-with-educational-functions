package com.translator.main.controller;

import com.translator.main.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class StaticController {

    @Autowired
    private TranslationService translationService;

    @GetMapping({"/", "/index", "/index.html"})
    public String indexPage(Model model) {
        try {
            // Получить последние переводы для отображения
            List<com.translator.main.model.Translation> recentTranslations = translationService.getAllTranslations();
            model.addAttribute("translations", recentTranslations);
        } catch (Exception e) {
            // Если произошла ошибка, продолжить без переводов
        }
        return "index";
    }

    @GetMapping("/quiz.html")
    public ResponseEntity<Resource> quizPage() {
        try {
            Resource resource = new ClassPathResource("static/quiz.html");
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/static/quiz.js")
    public ResponseEntity<Resource> quizJs() {
        try {
            Resource resource = new ClassPathResource("static/quiz.js");
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf("application/javascript"))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
