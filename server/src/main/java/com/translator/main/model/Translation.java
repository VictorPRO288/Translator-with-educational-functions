package com.translator.main.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;
import java.util.Map;

@Entity
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String originalText;
    private String translatedText;
    private String sourceLang;
    private String targetLang;

    @ManyToMany(mappedBy = "translations", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<User> users;

    public static final Map<String, String> SUPPORTED_LANGUAGES = Map.of(
            "en", "English",
            "ru", "Russian",
            "es", "Spanish",
            "fr", "French",
            "de", "German",
            "it", "Italian",
            "ja", "Japanese",
            "zh", "Chinese"
    );

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getOriginalText() {
        return originalText;
    }
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }
    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getSourceLang() {
        return sourceLang;
    }
    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public String getTargetLang() {
        return targetLang;
    }
    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }

    public List<User> getUsers() {
        return users;
    }
    public void setUsers(List<User> users) {
        this.users = users;
    }
}
