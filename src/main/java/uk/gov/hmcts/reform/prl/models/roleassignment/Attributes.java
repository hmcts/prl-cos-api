package uk.gov.hmcts.reform.prl.models.roleassignment;

import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ContractType;

@Data
public class Attributes {

    private String jurisdiction;
    private String caseType;
    private String caseId;
    private String region;
    private String location;
    private ContractType contractType;
    private String caseAccessGroupId;
}
