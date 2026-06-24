package com.osci.contractmanagement.infrastructure.contract.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.osci.contractmanagement.application.provider.TextExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 수기 계약서(사진) OCR.
 * Tesseract를 로컬에 설치하는 대신, Docker로 띄운 OCR 서버(REST API)를 호출한다.
 * 채용 담당자 PC에 별도 설치가 전혀 필요 없다 (docker compose up만으로 동작).
 */
@Component
public class TesseractOcrExtractor implements TextExtractor {

    private static final List<String> SUPPORTED_TYPES = List.of("image/png", "image/jpeg", "image/jpg");

    private final RestTemplate restTemplate;
    private final String ocrServerUrl;

    public TesseractOcrExtractor(
            RestTemplate restTemplate,
            @Value("${ocr.server.url}") String ocrServerUrl
    ) {
        this.restTemplate = restTemplate;
        this.ocrServerUrl = ocrServerUrl;
    }

    @Override
    public boolean supports(String contentType) {
        return SUPPORTED_TYPES.contains(contentType.toLowerCase());
    }

    @Override
    public String extractText(byte[] fileBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("options", "{\"languages\":[\"kor\",\"eng\"]}");
        body.add("file", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return "contract.png";
            }
        });

        try {
            JsonNode response = restTemplate.postForObject(
                    ocrServerUrl + "/tesseract",
                    new HttpEntity<>(body, headers),
                    JsonNode.class
            );
            String rawText = response.path("data").path("stdout").asText("");
            return ContractTextCleaner.clean(rawText);
        } catch (Exception e) {
            throw new IllegalStateException("OCR 서버 호출에 실패했습니다.", e);
        }
    }
}
