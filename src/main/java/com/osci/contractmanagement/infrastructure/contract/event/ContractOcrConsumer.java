package com.osci.contractmanagement.infrastructure.contract.event;

import com.osci.contractmanagement.application.service.contract.ContractUseCase;
import com.osci.contractmanagement.infrastructure.contract.dto.ContractOcrMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ContractOcrConsumer {
 
    private final ContractUseCase contractUseCase;

    public ContractOcrConsumer(ContractUseCase contractUseCase) {
        this.contractUseCase = contractUseCase;
    }

    @KafkaListener(topics = "ocr-process", groupId = "contract-ocr-group")
    public void consume(ContractOcrMessage message) {
        log.info("Received message: {}", message);
        contractUseCase.processContract(message.getContractId());
    }
}