package com.translator.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExampleItem {
    private String text;
    private String translation;
    private String note; // grammar/style note
}


