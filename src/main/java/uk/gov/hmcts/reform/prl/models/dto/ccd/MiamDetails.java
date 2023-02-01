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
    private YesOrNo applicantAttendedMiam;
    private YesOrNo claimingExemptionMiam;
    private YesOrNo familyMediatorMiam;
    private YesOrNo otherProceedingsMiam;
    private String applicantConsentMiam;
    private List<MiamExemptionsChecklistEnum> miamExemptionsChecklist;
    private List<MiamDomesticViolenceChecklistEnum> miamDomesticViolenceChecklist;
    private List<MiamUrgencyReasonChecklistEnum> miamUrgencyReasonChecklist;
    private List<MiamChildProtectionConcernChecklistEnum> miamChildProtectionConcernList;
    private MiamPreviousAttendanceChecklistEnum miamPreviousAttendanceChecklist;
    private List<MiamPreviousAttendanceChecklistEnum> miamPreviousAttendanceChecklist1;
    private MiamOtherGroundsChecklistEnum miamOtherGroundsChecklist;
    private List<MiamOtherGroundsChecklistEnum> miamOtherGroundsChecklist1;
    private final String mediatorRegistrationNumber;
    private final String familyMediatorServiceName;
    private final String soleTraderName;
    //TODO: refactor to remove duplicated details screen
    private Document miamCertificationDocumentUpload;
    private final String mediatorRegistrationNumber1;
    private final String familyMediatorServiceName1;
    private final String soleTraderName1;
    private final Document miamCertificationDocumentUpload1;
}
