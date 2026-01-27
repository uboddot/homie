package com.homie.services;

import org.springframework.stereotype.Component;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Component
public class GeminiTextTest {
    public String getGeminiResponse(String param) {
        // The client gets the API key from the environment variable `GEMINI_API_KEY`.
        Client client = new Client();

        GenerateContentResponse response = client.models.generateContent(
                "gemini-3-flash-preview",
                "Explain how AI works in a few words",
                null);

        return response.text();
    }
}