package uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attributes {
    private String substantive;
    private String caseId;
    private String jurisdiction;
    private String caseType;

}
