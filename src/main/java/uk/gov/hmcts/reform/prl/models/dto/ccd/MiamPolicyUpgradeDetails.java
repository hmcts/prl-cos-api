package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.*;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiamPolicyUpgradeDetails {
    private YesOrNo childInvolvedInMiam;
    private YesOrNo applicantAttendedMiam;
    private YesOrNo claimingExemptionMiam;
    private List<MiamExemptionsChecklistEnum> miamPolicyUpgradeExemptionsChecklist;
    private List<MiamDomesticAbuseChecklistEnum> miamDomesticAbuseChecklist;
    private YesOrNo miamDomesticAbuseEvidenceOptions;
    private List<Document> miamDomesticAbuseEvidenceDocument;
    private String miamDomesticAbuseNoEvidenceReason;
    private List<MiamUrgencyReasonChecklistEnum> miamPolicyUpgradeUrgencyReasonChecklist;
    private List<MiamPreviousAttendanceChecklistEnum> miamPolicyUpgradePreviousAttendanceChecklist;
    private Document evidenceFromDisputeResolutionProvider;
    private List<TypeOfMiamAttendanceEvidenceEnum> typeOfMiamAttendanceEvidence;
    private Document miamCertificateDocument;
    private String miamAttendanceDetails;
    private List<MiamChildProtectionConcernChecklistEnum> miamPolicyUpgradeOtherGroundsChecklist;
    private String applicantUnableToMiamReason1;
    private String applicantUnableToMiamReason2;
    private final String mediatorRegistrationNumber;
    private final String familyMediatorServiceName;
    private final String soleTraderName;
    private final Document miamCertificationDocumentUpload;
    private List<MiamChildProtectionConcernChecklistEnum> miamPolicyUpgradeChildProtectionConcernList;
}
