package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiamPolicyUpgradeDetails {
    @JsonProperty("childInvolvedInMiam")
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
