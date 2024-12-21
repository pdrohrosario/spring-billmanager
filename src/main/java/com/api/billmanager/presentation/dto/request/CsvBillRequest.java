package com.api.billmanager.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CsvBillRequest {

    Long csvId;

    BillRequest request;

    boolean imported;

    public CsvBillRequest(Long csvId, boolean imported) {
        this.csvId = csvId;
        this.imported = imported;
    }
}
