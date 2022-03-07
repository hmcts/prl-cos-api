package uk.gov.hmcts.reform.prl.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DxAddress {
    private String dxExchange;
    private String dxNumber;
}
