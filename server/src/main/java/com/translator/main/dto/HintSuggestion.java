package com.translator.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HintSuggestion {
    private String id;
    private String type; // grammar | style
    private String message; // explanation
    private String replacement; // suggested fix
    private int offset;
    private int length;
    private String example; // optional example usage
}


