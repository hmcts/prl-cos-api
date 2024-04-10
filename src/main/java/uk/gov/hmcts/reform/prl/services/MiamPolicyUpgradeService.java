package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.childProtectionConcern;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.domesticAbuse;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.other;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.previousMiamAttendance;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.urgency;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_3;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_4;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_5;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_1;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_2;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum.miamAttendanceDetails;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum.miamCertificate;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MiamPolicyUpgradeService {

    private final ObjectMapper objectMapper;

    public Map<String, Object> populateMiamPolicyUpgradeDetails(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(caseDataUpdated, CaseData.class);

        log.info("MiamPolicyUpgradeDetails from request" + caseData.getMiamPolicyUpgradeDetails());

        caseDataUpdated.put(
            "mpuChildInvolvedInMiam",
            isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam() : null
        );
        caseDataUpdated.put(
            "mpuApplicantAttendedMiam",
            isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantAttendedMiam())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuApplicantAttendedMiam() : null
        );
        caseDataUpdated.put(
            "mpuClaimingExemptionMiam",
            isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuClaimingExemptionMiam())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuClaimingExemptionMiam() : null
        );
        populateDataForApplicantAttendedMaim(caseDataUpdated, caseData);

        boolean isClaimingMaimExemption = Yes.equals(caseData.getMiamPolicyUpgradeDetails().getMpuClaimingExemptionMiam());
        caseDataUpdated.put(
            "mpuExemptionReasons",
            isClaimingMaimExemption && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons() : null
        );

        populateDataForDomesticAbuseExemption(caseDataUpdated, caseData, isClaimingMaimExemption);

        populateDataForChildProtectionExemption(caseDataUpdated, caseData, isClaimingMaimExemption);

        populateDataForUrgencyExemption(caseDataUpdated, caseData, isClaimingMaimExemption);

        populateDataForPreviousMiamAttendanceExemption(caseDataUpdated, caseData, isClaimingMaimExemption);

        populateDataForOtherExemption(caseDataUpdated, caseData, isClaimingMaimExemption);

        log.info("Final caseDataUpdated" + caseDataUpdated);

        return caseDataUpdated;
    }

    private static void populateDataForUrgencyExemption(Map<String, Object> caseDataUpdated, CaseData caseData, boolean isClaimingMaimExemption) {
        caseDataUpdated.put(
            "mpuUrgencyReason",
            isClaimingMaimExemption
                && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
                && caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(urgency)
                ? caseData.getMiamPolicyUpgradeDetails().getMpuUrgencyReason() : null
        );
    }

    private static void populateDataForChildProtectionExemption(Map<String, Object> caseDataUpdated,
                                                                CaseData caseData, boolean isClaimingMaimExemption) {
        caseDataUpdated.put(
            "mpuChildProtectionConcernReason",
            isClaimingMaimExemption
                && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
                && caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(childProtectionConcern)
                ? caseData.getMiamPolicyUpgradeDetails().getMpuChildProtectionConcernReason() : null
        );
    }

    private static void populateDataForApplicantAttendedMaim(Map<String, Object> caseDataUpdated, CaseData caseData) {
        boolean isApplicantAttendedMiam = Yes.equals(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantAttendedMiam());
        caseDataUpdated.put(
            "mediatorRegistrationNumber",
            isApplicantAttendedMiam && StringUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMediatorRegistrationNumber())
                ? caseData.getMiamPolicyUpgradeDetails().getMediatorRegistrationNumber() : null
        );
        caseDataUpdated.put(
            "familyMediatorServiceName",
            isApplicantAttendedMiam && StringUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getFamilyMediatorServiceName())
                ? caseData.getMiamPolicyUpgradeDetails().getFamilyMediatorServiceName() : null
        );
        caseDataUpdated.put(
            "soleTraderName",
            isApplicantAttendedMiam && StringUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getSoleTraderName())
                ? caseData.getMiamPolicyUpgradeDetails().getSoleTraderName() : null
        );
        caseDataUpdated.put(
            "miamCertificationDocumentUpload",
            isApplicantAttendedMiam && isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMiamCertificationDocumentUpload())
                ? caseData.getMiamPolicyUpgradeDetails().getMiamCertificationDocumentUpload() : null
        );
    }

    private static void populateDataForOtherExemption(Map<String, Object> caseDataUpdated, CaseData caseData, boolean isClaimingMaimExemption) {
        boolean isExemptionForOther = isClaimingMaimExemption
            && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
            && caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(other);

        caseDataUpdated.put(
            "mpuOtherExemptionReasons",
            isExemptionForOther ? caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons() : null);

        caseDataUpdated.put(
            "mpuApplicantUnableToAttendMiamReason1",
            isExemptionForOther
                && (miamPolicyUpgradeOtherGrounds_Value_3.equals(caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons())
                || miamPolicyUpgradeOtherGrounds_Value_4.equals(caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons()))
                ? caseData.getMiamPolicyUpgradeDetails().getMpuApplicantUnableToAttendMiamReason1() : null);

        caseDataUpdated.put(
            "mpuApplicantUnableToAttendMiamReason2",
            isExemptionForOther
                && miamPolicyUpgradeOtherGrounds_Value_5.equals(caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuApplicantUnableToAttendMiamReason2() : null);
    }

    private static void populateDataForPreviousMiamAttendanceExemption(Map<String, Object> caseDataUpdated,
                                                                       CaseData caseData, boolean isClaimingMaimExemption) {
        boolean isExemptionForPreviousMiamAttendance = isClaimingMaimExemption
            && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
            && caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(previousMiamAttendance);

        caseDataUpdated.put(
            "mpuPreviousMiamAttendanceReason",
            isExemptionForPreviousMiamAttendance ? caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason() : null
        );

        caseDataUpdated.put(
            "mpuDocFromDisputeResolutionProvider",
            isExemptionForPreviousMiamAttendance
                && miamPolicyUpgradePreviousAttendance_Value_1.equals(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuDocFromDisputeResolutionProvider() : null
        );

        caseDataUpdated.put(
            "mpuTypeOfPreviousMiamAttendanceEvidence",
            isExemptionForPreviousMiamAttendance
                && miamPolicyUpgradePreviousAttendance_Value_2.equals(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence() : null
        );

        caseDataUpdated.put(
            "mpuCertificateByMediator",
            isExemptionForPreviousMiamAttendance
                && miamPolicyUpgradePreviousAttendance_Value_2.equals(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
                && miamCertificate.equals(caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuCertificateByMediator() : null
        );

        caseDataUpdated.put(
            "mpuMediatorDetails",
            isExemptionForPreviousMiamAttendance
                && miamPolicyUpgradePreviousAttendance_Value_2.equals(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
                && miamAttendanceDetails.equals(caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuMediatorDetails() : null
        );
    }

    private static void populateDataForDomesticAbuseExemption(Map<String, Object> caseDataUpdated,
                                                              CaseData caseData, boolean isClaimingMaimExemption) {
        boolean isExemptionForDomesticAbuse = isClaimingMaimExemption
            && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
            && caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(domesticAbuse);

        caseDataUpdated.put(
            "mpuDomesticAbuseEvidences",
            isExemptionForDomesticAbuse ? caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidences() : null
        );

        caseDataUpdated.put(
            "mpuIsDomesticAbuseEvidenceProvided",
            isExemptionForDomesticAbuse ? caseData.getMiamPolicyUpgradeDetails().getMpuIsDomesticAbuseEvidenceProvided() : null
        );

        caseDataUpdated.put(
            "mpuDomesticAbuseEvidenceDocument",
            isExemptionForDomesticAbuse && Yes.equals(caseData.getMiamPolicyUpgradeDetails().getMpuIsDomesticAbuseEvidenceProvided())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidenceDocument() : null
        );

        caseDataUpdated.put(
            "mpuNoDomesticAbuseEvidenceReason",
            isExemptionForDomesticAbuse && No.equals(caseData.getMiamPolicyUpgradeDetails().getMpuIsDomesticAbuseEvidenceProvided())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuNoDomesticAbuseEvidenceReason() : null
        );
    }
}
