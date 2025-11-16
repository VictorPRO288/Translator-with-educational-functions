package com.translator.main.cache;

import com.translator.main.model.Translation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TranslationCache {

    private final Map<String, List<Translation>> cache = new HashMap<>();

    // Получить данные из кэша
    public List<Translation> get(String key) {
        return cache.get(key);
    }

    // Добавить данные в кэш
    public void put(String key, List<Translation> translations) {
        cache.put(key, translations);
    }

    // Очистить кэш
    public void clear() {
        cache.clear();
    }
}
