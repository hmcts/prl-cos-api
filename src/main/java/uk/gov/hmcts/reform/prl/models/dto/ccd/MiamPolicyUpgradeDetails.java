package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPolicyUpgradeChildProtectionConcernEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseEvidenceDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiamPolicyUpgradeDetails {
    private YesOrNo mpuChildInvolvedInMiam;
    private YesOrNo mpuApplicantAttendedMiam;
    private YesOrNo mpuClaimingExemptionMiam;
    private List<MiamExemptionsChecklistEnum> mpuExemptionReasons;
    private List<MiamDomesticAbuseChecklistEnum> mpuDomesticAbuseEvidences;
    private YesOrNo mpuIsDomesticAbuseEvidenceProvided;
    private List<Element<DomesticAbuseEvidenceDocument>> mpuDomesticAbuseEvidenceDocument;
    private String mpuNoDomesticAbuseEvidenceReason;
    private MiamUrgencyReasonChecklistEnum mpuUrgencyReason;
    private MiamPreviousAttendanceChecklistEnum mpuPreviousMiamAttendanceReason;
    private Document mpuDocFromDisputeResolutionProvider;
    private TypeOfMiamAttendanceEvidenceEnum mpuTypeOfPreviousMiamAttendanceEvidence;
    private Document mpuCertificateByMediator;
    private String mpuMediatorDetails;
    private MiamOtherGroundsChecklistEnum mpuOtherExemptionReasons;
    private String mpuApplicantUnableToAttendMiamReason1;
    private String mpuApplicantUnableToAttendMiamReason2;
    private final String mediatorRegistrationNumber;
    private final String familyMediatorServiceName;
    private final String soleTraderName;
    private final Document miamCertificationDocumentUpload;
    private MiamPolicyUpgradeChildProtectionConcernEnum mpuChildProtectionConcernReason;
}
