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
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuChildProtectionConcern;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuDomesticAbuse;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuOther;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuUrgency;
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

    public Map<String, Object> populateAmendedMiamPolicyUpgradeDetails(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(caseDataUpdated, CaseData.class);
        updateMiamPolicyUpgradeDetails(caseData, caseDataUpdated);
        return caseDataUpdated;
    }

    public CaseData updateMiamPolicyUpgradeDetails(CaseData caseData, Map<String, Object> caseDataUpdated) {
        Map<String, Object> updatedMiamPolicyUpgradeData = cleanUpMiamPolicyUpgradeDetails(
            caseData);
        caseDataUpdated.putAll(updatedMiamPolicyUpgradeData);
        caseData = caseData
            .toBuilder()
            .miamPolicyUpgradeDetails(objectMapper.convertValue(
                updatedMiamPolicyUpgradeData,
                MiamPolicyUpgradeDetails.class
            ))
            .build();
        return caseData;
    }

    public Map<String, Object> cleanUpMiamPolicyUpgradeDetails(CaseData caseData) {
        Map<String, Object> updatedMiamPolicyUpgradeData = new HashMap<>();
        updatedMiamPolicyUpgradeData.put(
            "mpuChildInvolvedInMiam",
            isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam() : null
        );
        updatedMiamPolicyUpgradeData.put(
            "mpuApplicantAttendedMiam",
            No.equals(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam())
            && isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantAttendedMiam())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuApplicantAttendedMiam() : null
        );
        updatedMiamPolicyUpgradeData.put(
            "mpuClaimingExemptionMiam",
            No.equals(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam())
            && No.equals(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantAttendedMiam())
            && isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuClaimingExemptionMiam())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuClaimingExemptionMiam() : null
        );
        populateDataForApplicantAttendedMaim(updatedMiamPolicyUpgradeData, caseData);

        boolean isClaimingMaimExemption = No.equals(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam())
            && No.equals(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantAttendedMiam())
            && Yes.equals(caseData.getMiamPolicyUpgradeDetails().getMpuClaimingExemptionMiam());
        updatedMiamPolicyUpgradeData.put(
            "mpuExemptionReasons",
            isClaimingMaimExemption && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons() : null
        );

        populateDataForDomesticAbuseExemption(updatedMiamPolicyUpgradeData, caseData, isClaimingMaimExemption);

        populateDataForChildProtectionExemption(updatedMiamPolicyUpgradeData, caseData, isClaimingMaimExemption);

        populateDataForUrgencyExemption(updatedMiamPolicyUpgradeData, caseData, isClaimingMaimExemption);

        populateDataForPreviousMiamAttendanceExemption(updatedMiamPolicyUpgradeData, caseData, isClaimingMaimExemption);

        populateDataForOtherExemption(updatedMiamPolicyUpgradeData, caseData, isClaimingMaimExemption);

        return updatedMiamPolicyUpgradeData;
    }

    private void populateDataForUrgencyExemption(Map<String, Object> caseDataUpdated, CaseData caseData, boolean isClaimingMaimExemption) {
        caseDataUpdated.put(
            "mpuUrgencyReason",
            isClaimingMaimExemption
                && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
                && caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(mpuUrgency)
                ? caseData.getMiamPolicyUpgradeDetails().getMpuUrgencyReason() : null
        );
    }

    private void populateDataForChildProtectionExemption(Map<String, Object> caseDataUpdated,
                                                                CaseData caseData, boolean isClaimingMaimExemption) {
        caseDataUpdated.put(
            "mpuChildProtectionConcernReason",
            isClaimingMaimExemption
                && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
                && caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(mpuChildProtectionConcern)
                ? caseData.getMiamPolicyUpgradeDetails().getMpuChildProtectionConcernReason() : null
        );
    }

    private void populateDataForApplicantAttendedMaim(Map<String, Object> caseDataUpdated, CaseData caseData) {
        boolean isApplicantAttendedMiam =
            No.equals(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam())
            && Yes.equals(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantAttendedMiam());
        caseDataUpdated.put(
            "mediatorRegistrationNumber",
            isApplicantAttendedMiam && StringUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMediatorRegistrationNumber())
                ? caseData.getMiamPolicyUpgradeDetails().getMediatorRegistrationNumber().trim() : null
        );
        caseDataUpdated.put(
            "familyMediatorServiceName",
            isApplicantAttendedMiam && StringUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getFamilyMediatorServiceName())
                ? caseData.getMiamPolicyUpgradeDetails().getFamilyMediatorServiceName().trim() : null
        );
        caseDataUpdated.put(
            "soleTraderName",
            isApplicantAttendedMiam && StringUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getSoleTraderName())
                ? caseData.getMiamPolicyUpgradeDetails().getSoleTraderName().trim() : null
        );
        caseDataUpdated.put(
            "miamCertificationDocumentUpload",
            isApplicantAttendedMiam && isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMiamCertificationDocumentUpload())
                ? caseData.getMiamPolicyUpgradeDetails().getMiamCertificationDocumentUpload() : null
        );
    }

    private void populateDataForOtherExemption(Map<String, Object> caseDataUpdated, CaseData caseData, boolean isClaimingMaimExemption) {
        boolean isExemptionForOther = isClaimingMaimExemption
            && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
            && caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(mpuOther);

        caseDataUpdated.put(
            "mpuOtherExemptionReasons",
            isExemptionForOther ? caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons() : null);

        caseDataUpdated.put(
            "mpuApplicantUnableToAttendMiamReason1",
            isExemptionForOther
                && (miamPolicyUpgradeOtherGrounds_Value_3.equals(caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons())
                || miamPolicyUpgradeOtherGrounds_Value_4.equals(caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons()))
                && StringUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantUnableToAttendMiamReason1())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuApplicantUnableToAttendMiamReason1().trim() : null);

        caseDataUpdated.put(
            "mpuApplicantUnableToAttendMiamReason2",
            isExemptionForOther
                && miamPolicyUpgradeOtherGrounds_Value_5.equals(caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons())
                && StringUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantUnableToAttendMiamReason2())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuApplicantUnableToAttendMiamReason2().trim() : null);
    }

    private void populateDataForPreviousMiamAttendanceExemption(Map<String, Object> caseDataUpdated,
                                                                       CaseData caseData, boolean isClaimingMaimExemption) {
        boolean isExemptionForPreviousMiamAttendance = isClaimingMaimExemption
            && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
            && caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(mpuPreviousMiamAttendance);

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
                && StringUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuMediatorDetails())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuMediatorDetails().trim() : null
        );
    }

    private void populateDataForDomesticAbuseExemption(Map<String, Object> caseDataUpdated,
                                                              CaseData caseData, boolean isClaimingMaimExemption) {
        boolean isExemptionForDomesticAbuse = isClaimingMaimExemption
            && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
            && caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(mpuDomesticAbuse);

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
                && StringUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuNoDomesticAbuseEvidenceReason())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuNoDomesticAbuseEvidenceReason().trim() : null
        );
    }
}
