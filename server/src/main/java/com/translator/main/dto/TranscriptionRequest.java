package com.translator.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionRequest {
    private String text;
    private String languageCode; // e.g. en, en-US, ru, ru-RU
}


