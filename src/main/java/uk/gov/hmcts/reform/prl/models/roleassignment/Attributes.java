package uk.gov.hmcts.reform.prl.models.roleassignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.ContractType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "attributes")
public class Attributes {

    private String jurisdiction;
    private String caseType;
    private String caseId;
    private String region;
    private String location;
    private ContractType contractType;
    private String caseAccessGroupId;
}
