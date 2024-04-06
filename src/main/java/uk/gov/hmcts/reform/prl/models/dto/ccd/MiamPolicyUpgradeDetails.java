package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiamPolicyUpgradeDetails {
    private YesOrNo childInvolvedInMIAM;
    private YesOrNo applicantAttendedMiam;
    private YesOrNo claimingExemptionMiam;
    private List<MiamExemptionsChecklistEnum> miamPolicyUpgradeExemptionsChecklist;
    private List<MiamDomesticAbuseChecklistEnum> miamDomesticAbuseChecklist;
    //  miamDomesticAbuseEvidenceOptions;
    //  miamDomesticAbuseEvidenceDocument;
    //  miamDomesticAbuseNoEvidenceReason;
    private List<MiamUrgencyReasonChecklistEnum> miamPolicyUpgradeUrgencyReasonChecklist;
    private List<MiamPreviousAttendanceChecklistEnum> miamPolicyUpgradePreviousAttendanceChecklist;
    //    evidenceFromDisputeResolutionProvider;
    //    typeOfMiamAttendanceEvidence;
    private Document miamCertificateDocument;
    //    miamAttendanceDetails;
    private final String mediatorRegistrationNumber;
    private final String familyMediatorServiceName;
    private final String soleTraderName;
    private final Document miamCertificationDocumentUpload;
    private List<MiamChildProtectionConcernChecklistEnum> miamPolicyUpgradeChildProtectionConcernList;
}
