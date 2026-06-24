package com.osci.contractmanagement.application.service.contract.event;

public interface ContractEventProducer {
    void publishOcrRequested(Long contractId);
}
