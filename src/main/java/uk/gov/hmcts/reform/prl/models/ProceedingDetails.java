package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder
public class ProceedingDetails {

    private final String courtName;
    private final String caseNumber;
    private final LocalDate date;
    private final String cafcassOfficer;
    private final YesOrNo emergencyProtectionOrder;
    private final YesOrNo supervisionOrder;
    private final YesOrNo caseOrder;
    private final YesOrNo childAbduction;
    private final YesOrNo familyLawAct;
    private final YesOrNo contactOrderWithinProceedings;
    private final YesOrNo contactOrderWithinAdoptionOrder;
    private final YesOrNo childMaintenanceOrder;
    private final YesOrNo childArrangementsOrder;
    private final ProceedingOrderDocument proceedingOrder;

}
