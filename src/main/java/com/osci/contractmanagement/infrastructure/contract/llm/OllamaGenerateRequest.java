package com.osci.contractmanagement.infrastructure.contract.llm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OllamaGenerateRequest {
    private String model;
    private String prompt;
    private String format;
    private boolean stream;

    public static OllamaGenerateRequest of(String model, String prompt) {
        return new OllamaGenerateRequest(model, prompt, "json", false);
    }
}
