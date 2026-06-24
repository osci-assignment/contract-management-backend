package com.osci.contractmanagement.application.dto.request.company;

import com.osci.contractmanagement.domain.model.company.ContractType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UpdateCompanyRequestDto {
    @NotBlank(message = "업체명을 입력해주세요.")
    private String name;
    private ContractType contractType;

}
