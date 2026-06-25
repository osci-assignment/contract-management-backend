package com.osci.contractmanagement.application.dto.response.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ContractFileData{
    private byte[] fileBytes;
    private String originalFilename;
    private String contentType;
}
