package com.translator.main.service;

import com.translator.main.dto.AnalysisRequest;
import com.translator.main.dto.AnalysisResponse;
import com.translator.main.dto.HintSuggestion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.gemini.apiKey:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String model;

    private WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public AnalysisResponse analyze(AnalysisRequest request) {
        String targetLang = request.getTargetLanguage() != null ? request.getTargetLanguage() : "en";
        String text = request.getTranslatedText() != null ? request.getTranslatedText() : request.getSourceText();
        if (text == null || text.isBlank()) {
            return new AnalysisResponse(List.of());
        }

        String system = "You are a grammar and style assistant. Return compact JSON only.";
        String user = "Language: " + targetLang + "\nText: " + text + "\n" +
                "Task: Find grammar/spelling/style issues and propose fixes.\n" +
                "Output strictly as JSON: {\n  \"suggestions\": [ { \"type\": \"grammar|style|spelling\", \"message\": \"...\", \"replacement\": \"...\", \"offset\": 0, \"length\": 0 } ]\n}";

        try {
            String responseText = generateContent(system, user);
            return parseAnalysisResponse(responseText);
        } catch (Exception e) {
            return new AnalysisResponse(List.of());
        }
    }

    public TranscriptionPackage transcribeWithExamples(String text, String languageCode) {
        if (text == null || text.isBlank()) return new TranscriptionPackage("", List.of());
        String system = "Return strict JSON only. Provide a clear IPA transcription and 6 short usage examples.";
        String user = "Language: " + (languageCode == null ? "en" : languageCode) + "\nText: " + text + "\n" +
                "Requirements for ipa: normalized IPA, words separated by spaces, syllables separated by dots, primary stress marked with ˈ, secondary with ˌ. No extra commentary.\n" +
                "Return strictly JSON: {\\\"ipa\\\": \\\"...\\\", \\\"examples\\\": [\\\"...\\\", \\\"...\\\", \\\"...\\\", \\\"...\\\", \\\"...\\\", \\\"...\\\"]}";
        try {
            String responseText = generateContent(system, user);
            JsonNode root = tryExtractJson(responseText);
            String ipa = "";
            List<String> examples = new ArrayList<>();
            if (root != null) {
                if (root.has("ipa")) ipa = root.get("ipa").asText("");
                if (root.has("examples") && root.get("examples").isArray()) {
                    for (JsonNode n : root.get("examples")) {
                        examples.add(n.asText(""));
                    }
                }
            }
            return new TranscriptionPackage(ipa, examples);
        } catch (Exception ignored) {}
        return new TranscriptionPackage("", List.of());
    }

    public String generateQuiz(String originalText, String translatedText, String sourceLang, String targetLang) {
        String system = "You are a language learning assistant. Create a quiz with 5 questions based on the translation. Return strict JSON only.";
        String user = "Original text: " + originalText + "\n" +
                "Translated text: " + translatedText + "\n" +
                "Source language: " + sourceLang + "\n" +
                "Target language: " + targetLang + "\n" +
                "Create 5 questions that test understanding of the translation. Questions should be about:\n" +
                "1. Word usage in context\n" +
                "2. Grammar forms\n" +
                "3. Vocabulary comprehension\n" +
                "4. Sentence structure\n" +
                "5. Cultural context or meaning\n" +
                "Each question should have 4 multiple choice options.\n" +
                "Return JSON format:\n" +
                "{\n" +
                "  \"title\": \"Quiz title\",\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question\": \"Question text\",\n" +
                "      \"correctAnswer\": \"Correct answer\",\n" +
                "      \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        try {
            return generateContent(system, user);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при генерации теста: " + e.getMessage(), e);
        }
    }

    public static class TranscriptionPackage {
        public final String ipa;
        public final List<String> examples;
        public TranscriptionPackage(String ipa, List<String> examples) { this.ipa = ipa; this.examples = examples; }
    }

    private String generateContent(String system, String user) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not set (ai.gemini.apiKey)");
        }
        String requestText = system + "\n\n" + user;
        String json = callGenerateContent(model, requestText);
        if (json == null || json.isBlank()) {
            json = callGenerateContent("gemini-2.5-flash", requestText);
        }
        if (json == null || json.isBlank()) {
            json = callGenerateContent("gemini-1.5-flash", requestText);
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode p : parts) {
                        String t = p.path("text").asText("");
                        if (!t.isEmpty()) sb.append(t);
                    }
                    return sb.toString();
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String callGenerateContent(String modelToUse, String text) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            ArrayNode parts = objectMapper.createArrayNode().add(
                    objectMapper.createObjectNode().put("text", text)
            );
            body.set("contents", objectMapper.createArrayNode().add(
                    objectMapper.createObjectNode()
                            .put("role", "user")
                            .set("parts", parts)
            ));
            ObjectNode genCfg = objectMapper.createObjectNode();
            genCfg.put("responseMimeType", "application/json");
            genCfg.put("temperature", 0.2);
            body.set("generationConfig", genCfg);

            Mono<String> mono = webClient().post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/" + modelToUse + ":generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body.toString())
                    .retrieve()
                    .bodyToMono(String.class);

            return mono.block();
        } catch (Exception e) {
            return "";
        }
    }

    private AnalysisResponse parseAnalysisResponse(String responseText) {
        List<HintSuggestion> suggestions = new ArrayList<>();
        JsonNode root = tryExtractJson(responseText);
        if (root != null && root.has("suggestions") && root.get("suggestions").isArray()) {
            for (JsonNode n : root.get("suggestions")) {
                HintSuggestion hs = new HintSuggestion(
                        UUID.randomUUID().toString(),
                        n.path("type").asText("grammar"),
                        n.path("message").asText(""),
                        n.has("replacement") && !n.get("replacement").isNull() ? n.get("replacement").asText() : null,
                        n.path("offset").asInt(0),
                        n.path("length").asInt(0),
                        null
                );
                suggestions.add(hs);
            }
        }
        return new AnalysisResponse(suggestions);
    }

    private JsonNode tryExtractJson(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        try {
            if (trimmed.startsWith("```")) {
                int first = trimmed.indexOf('{');
                int last = trimmed.lastIndexOf('}');
                if (first >= 0 && last > first) {
                    trimmed = trimmed.substring(first, last + 1);
                }
            }
            // if text still not JSON, try to find first {...} block
            if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))) {
                int first = trimmed.indexOf('{');
                int last = trimmed.lastIndexOf('}');
                if (first >= 0 && last > first) {
                    trimmed = trimmed.substring(first, last + 1);
                }
            }
            return objectMapper.readTree(trimmed);
        } catch (Exception e) {
            return null;
        }
    }
}


