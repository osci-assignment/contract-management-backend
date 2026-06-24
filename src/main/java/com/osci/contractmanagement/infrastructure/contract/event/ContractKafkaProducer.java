package com.osci.contractmanagement.infrastructure.contract.event;

import com.osci.contractmanagement.application.service.contract.event.ContractEventProducer;
import com.osci.contractmanagement.infrastructure.contract.dto.ContractOcrMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ContractKafkaProducer implements ContractEventProducer {
 
    private static final String TOPIC = "ocr-process";
 
    private final KafkaTemplate<String, ContractOcrMessage> kafkaTemplate;

    public ContractKafkaProducer(KafkaTemplate<String, ContractOcrMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOcrRequested(Long contractId) {
        log.info("Publishing OCR request for contractId: {}", contractId);
        kafkaTemplate.send(TOPIC, new ContractOcrMessage(contractId));
    }
}