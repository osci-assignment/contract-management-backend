package com.osci.contractmanagement.infrastructure.contract.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ContractExtractionDto{
        private String companyName;
        private String startDate;
        private String endDate;
}

