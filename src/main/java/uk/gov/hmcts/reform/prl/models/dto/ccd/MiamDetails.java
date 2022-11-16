package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiamDetails {
    private final YesOrNo applicantAttendedMiam;
    private final YesOrNo claimingExemptionMiam;
    private final YesOrNo familyMediatorMiam;
    private final List<MiamExemptionsChecklistEnum> miamExemptionsChecklist;
    private final List<MiamDomesticViolenceChecklistEnum> miamDomesticViolenceChecklist;
    private final List<MiamUrgencyReasonChecklistEnum> miamUrgencyReasonChecklist;
    private final List<MiamChildProtectionConcernChecklistEnum> miamChildProtectionConcernList;
    private final MiamPreviousAttendanceChecklistEnum miamPreviousAttendanceChecklist;
    private final MiamOtherGroundsChecklistEnum miamOtherGroundsChecklist;
    private final String mediatorRegistrationNumber;
    private final String familyMediatorServiceName;
    private final String soleTraderName;
    //TODO: refactor to remove duplicated details screen
    private final Document miamCertificationDocumentUpload;
    private final String mediatorRegistrationNumber1;
    private final String familyMediatorServiceName1;
    private final String soleTraderName1;
    private final Document miamCertificationDocumentUpload1;
}
