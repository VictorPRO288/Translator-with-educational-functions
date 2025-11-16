package com.translator.main.controller;

import com.translator.main.dto.*;
import com.translator.main.service.ExamplesService;
import com.translator.main.service.FeedbackService;
import com.translator.main.service.HintService;
import com.translator.main.service.GeminiService;
import com.translator.main.dto.TranscriptionRequest;
import com.translator.main.dto.TranscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hints")
@CrossOrigin
@RequiredArgsConstructor
public class HintController {

    private final HintService hintService;
    private final FeedbackService feedbackService;
    private final ExamplesService examplesService;
    private final GeminiService geminiService;
    private final com.translator.main.service.TtsService ttsService;

    @PostMapping(value = "/analyze", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AnalysisResponse> analyze(@RequestBody AnalysisRequest request) {
        try {
            AnalysisResponse resp = geminiService.analyze(request);
            if (resp == null || resp.getSuggestions() == null || resp.getSuggestions().isEmpty()) {
                resp = hintService.analyze(request);
            }
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.ok(new AnalysisResponse(java.util.List.of()));
        }
    }

    @PostMapping(value = "/transcribe", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TranscriptionResponse> transcribe(@RequestBody TranscriptionRequest request) {
        try {
            var pkg = geminiService.transcribeWithExamples(request.getText(), request.getLanguageCode());
            return ResponseEntity.ok(new TranscriptionResponse(pkg.ipa, pkg.examples));
        } catch (Exception e) {
            return ResponseEntity.ok(new TranscriptionResponse("", java.util.List.of()));
        }
    }

    @GetMapping(value = "/tts", produces = "audio/mpeg")
    public ResponseEntity<byte[]> tts(
            @RequestParam String text,
            @RequestParam(required = false, defaultValue = "ru-RU") String lang
    ) {
        try {
            byte[] audio = ttsService.synthesize(text, lang);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "audio/mpeg");
            return new ResponseEntity<>(audio, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new byte[0]);
        }
    }

    @PostMapping("/feedback")
    public ResponseEntity<Void> feedback(@RequestBody FeedbackRequest request) {
        feedbackService.recordFeedback(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/examples")
    public ResponseEntity<List<ExampleItem>> examples(
            @RequestParam String phrase,
            @RequestParam(required = false, defaultValue = "en") String sourceLang,
            @RequestParam(required = false, defaultValue = "ru") String targetLang) {
        return ResponseEntity.ok(examplesService.findExamples(phrase, sourceLang, targetLang));
    }
}


