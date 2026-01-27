package com.homie.controller;

import com.homie.services.GeminiTextTest;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/test-gemini")
public class TestGeminiController {

    @Autowired
    private GeminiTextTest geminiTextTest;

    @GetMapping("/")
    public String getResponseFromGeminiString(@RequestParam String param) {
        return geminiTextTest.getGeminiResponse(param);
    }

}
