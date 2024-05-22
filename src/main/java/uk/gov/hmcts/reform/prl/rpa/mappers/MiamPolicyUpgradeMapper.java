package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.stream.Collectors;
import javax.json.JsonObject;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;


@Component
public class MiamPolicyUpgradeMapper {

    public JsonObject map(CaseData caseData) {

        return new NullAwareJsonObjectBuilder()
            .add("mpuChildInvolvedInMiam", CommonUtils.getYesOrNoValue(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam()))
            .add("mpuApplicantAttendedMiam", CommonUtils.getYesOrNoValue(caseData.getMiamPolicyUpgradeDetails().getMpuApplicantAttendedMiam()))
            .add("mpuClaimingExemptionMiam", CommonUtils.getYesOrNoValue(caseData.getMiamPolicyUpgradeDetails().getMpuClaimingExemptionMiam()))
            .add(
                "mpuExemptionReasons",
                isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
                    ? caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().stream()
                    .map(MiamExemptionsChecklistEnum::getDisplayedValue).collect(Collectors.joining(", "))
                    : null
            )
            .add(
                "mpuDomesticAbuseEvidences",
                caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidences() != null
                    ? caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidences().stream()
                        .map(MiamDomesticAbuseChecklistEnum::getDisplayedValue)
                        .collect(Collectors.joining(", ")) : null
            )
            .add("mpuIsDomesticAbuseEvidenceProvided", CommonUtils.getYesOrNoValue(caseData.getMiamPolicyUpgradeDetails()
                                                                                       .getMpuIsDomesticAbuseEvidenceProvided()))
            .add("mpuNoDomesticAbuseEvidenceReason", caseData.getMiamPolicyUpgradeDetails().getMpuNoDomesticAbuseEvidenceReason())
            .add("mpuChildProtectionConcernReason", ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails()
                                                                               .getMpuChildProtectionConcernReason())
                ? caseData.getMiamPolicyUpgradeDetails().getMpuChildProtectionConcernReason().getDisplayedValue() : null)
            .add(
                "mpuUrgencyReason",
                ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuUrgencyReason())
                    ? caseData.getMiamPolicyUpgradeDetails().getMpuUrgencyReason().getDisplayedValue() : null
            )
            .add(
                "mpuPreviousMiamAttendanceReason",
                ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
                    ? caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason().getDisplayedValue() : null
            )
            .add(
                "mpuTypeOfPreviousMiamAttendanceEvidence",
                ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence())
                    ? caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence().getDisplayedValue() : null
            )
            .add(
                "mpuTypeOfPreviousMiamAttendanceEvidence",
                ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence())
                    ? caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence().getDisplayedValue() : null
            )
            .add(
                "mpuMediatorDetails",
                caseData.getMiamPolicyUpgradeDetails().getMpuMediatorDetails())
            .add(
                "mpuOtherExemptionReasons",
                ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons())
                    ? caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons().getDisplayedValue() : null
            )
            .add(
                "mpuApplicantUnableToAttendMiamReason1", caseData.getMiamPolicyUpgradeDetails().getMpuApplicantUnableToAttendMiamReason1())
            .add(
                "mpuApplicantUnableToAttendMiamReason2", caseData.getMiamPolicyUpgradeDetails().getMpuApplicantUnableToAttendMiamReason2())
            .add("mediatorRegistrationNumber", caseData.getMiamPolicyUpgradeDetails().getMediatorRegistrationNumber())
            .add("familyMediatorServiceName", caseData.getMiamPolicyUpgradeDetails().getFamilyMediatorServiceName())
            .add("soleTraderName", caseData.getMiamPolicyUpgradeDetails().getSoleTraderName())
            .build();
    }
}
