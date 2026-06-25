package com.osci.contractmanagement.infrastructure.contract.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osci.contractmanagement.application.exceptions.BusinessException;
import com.osci.contractmanagement.application.exceptions.BusinessExceptionType;
import com.osci.contractmanagement.application.provider.ContractInfoExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Component
@Slf4j
public class OllamaContractInfoExtractor implements ContractInfoExtractor {

    /**
     * LLM에 전달하는 계약서 원문 최대 길이.
     * 너무 길면 인식률이 떨어지고(컨텍스트 앞부분에 핵심 정보가 몰려있는 계약서 특성상)
     * 처리 시간도 늘어나므로 앞부분 500자만 사용한다.
     */
    private static final int MAX_TEXT_LENGTH = 500;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String ollamaUrl;
    private final String model;

    public OllamaContractInfoExtractor(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${llm.ollama.url}") String ollamaUrl,
            @Value("${llm.ollama.model}") String model
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.ollamaUrl = ollamaUrl;
        this.model = model;
    }

    /**
     * 업체명 판별 규칙에서 제외해야 할 자사명. LLM이 프롬프트 규칙을 무시하고
     * '갑'/'을' 중 자사명을 그대로 업체명으로 뽑아버리는 경우가 있어, 코드에서
     * 한 번 더 검증한다. 이 값이 추출되면 잘못된 결과로 간주하고 재시도를 유도한다.
     */
    private static final String OWN_COMPANY_NAME = "오픈소스컨설팅";

    @Override
    public ContractExtractionResult extract(String contractText) {
        OllamaGenerateRequest request = OllamaGenerateRequest.of(model, buildPrompt(contractText));

        try {
            OllamaGenerateResponse response = restTemplate.postForObject(
                    ollamaUrl + "/api/generate",
                    request,
                    OllamaGenerateResponse.class
            );

            ContractExtractionDto dto = objectMapper.readValue(response.getResponse(), ContractExtractionDto.class);
            log.info("LLM 계약 정보 추출 결과: (업체명: {}, 시작일: {}, 종료일: {})", dto.getCompanyName(), dto.getStartDate(), dto.getEndDate());

            validateNotOwnCompany(dto.getCompanyName());

            return new ContractExtractionResult(
                    dto.getCompanyName(),
                    parseDate(dto.getStartDate()),
                    parseDate(dto.getEndDate())
            );
        } catch (Exception e) {
            throw new IllegalStateException("LLM을 통한 계약 정보 추출에 실패했습니다.", e);
        }
    }

    private void validateNotOwnCompany(String companyName) {
        if (companyName != null && companyName.contains(OWN_COMPANY_NAME)) {
            throw new IllegalStateException(
                    "LLM이 상대 업체가 아닌 자사명(" + OWN_COMPANY_NAME + ")을 업체명으로 추출했습니다. 재시도가 필요합니다.");
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private String buildPrompt(String contractText) {
        String truncatedText = truncate(contractText);

        return """
                다음은 계약서 원문 일부이다. 아래 JSON 형식으로만 응답하라. 다른 설명, 마크다운, 코드블록 표시는 붙이지 마라.
 
                [업체명 판별 규칙]
                - '갑'이 "오픈소스컨설팅"이면 '을'을 업체명으로 한다.
                - '을'이 "오픈소스컨설팅"이면 '갑'을 업체명으로 한다.
                - 위 두 경우에 모두 해당하지 않으면 '갑'을 업체명으로 한다.
                - "오픈소스컨설팅", "(주)오픈소스컨설팅" 등 표기 차이는 같은 회사로 간주한다.
                - 업체명에는 직책/대표자명 등 회사명이 아닌 정보를 포함하지 않는다.
 
                [날짜 판별 규칙]
                - 계약 시작일/종료일이 명시되어 있으면 그대로 사용한다.
                - "계약기간: 2026.01.01 ~ 2026.12.31"처럼 범위로만 표기된 경우 시작일/종료일로 분리한다.
                - 날짜 형식은 반드시 yyyy-MM-dd로 변환한다.
 
                [출력 형식]
                {"companyName": "string", "startDate": "yyyy-MM-dd", "endDate": "yyyy-MM-dd"}
                값을 찾을 수 없으면 해당 필드는 null로 채운다. 위 JSON 외의 텍스트는 절대 출력하지 않는다.
 
                계약서 원문:
                %s
                """.formatted(truncatedText);
    }

    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= MAX_TEXT_LENGTH ? text : text.substring(0, MAX_TEXT_LENGTH);
    }
}