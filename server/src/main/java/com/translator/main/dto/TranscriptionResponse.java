package com.translator.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResponse {
    private String ipa;
    private List<String> examples; // usage examples related to the text
}


