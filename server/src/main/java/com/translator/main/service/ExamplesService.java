package com.translator.main.service;

import com.translator.main.dto.ExampleItem;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ExamplesService {

    public List<ExampleItem> findExamples(String phrase, String sourceLang, String targetLang) {
        // Placeholder; in future pull from corpora
        return Arrays.asList(
                new ExampleItem("I have been working here for five years.", "Я работаю здесь уже пять лет.", "Present perfect continuous for duration"),
                new ExampleItem("She must have left already.", "Она, должно быть, уже ушла.", "Modal verb + perfect infinitive")
        );
    }
}


