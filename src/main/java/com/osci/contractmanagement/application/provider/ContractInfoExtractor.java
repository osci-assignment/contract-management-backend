
package com.osci.contractmanagement.application.provider;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public interface ContractInfoExtractor {
 
    ContractExtractionResult extract(String contractText);

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    class ContractExtractionResult{
            String companyName;
            LocalDate startDate;
            LocalDate endDate;
    }
}
 