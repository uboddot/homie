package com.homie.services;

import org.springframework.stereotype.Component;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Component
public class GeminiTextTest {
    public String getGeminiResponse(String param) {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        Client client = new Client.Builder()
                .apiKey(apiKey)
                .build();

        GenerateContentResponse response = client.models.generateContent(
                "gemini-3-flash-preview",
                param,
                null);

        return response.text();
    }
}