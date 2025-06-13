package uk.gov.hmcts.reform.prl.mapper.rpa;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.mapper.rpa.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.stream.Collectors;
import javax.json.JsonObject;

@Component
public class MiamMapper {

    public JsonObject map(CaseData caseData) {

        return new NullAwareJsonObjectBuilder()
            .add("applicantAttendedMiam", CommonUtils.getYesOrNoValue(caseData.getMiamDetails().getApplicantAttendedMiam()))
            .add("claimingExemptionMiam", CommonUtils.getYesOrNoValue(caseData.getMiamDetails().getClaimingExemptionMiam()))
            .add("familyMediatorMiam", CommonUtils.getYesOrNoValue(caseData.getMiamDetails().getFamilyMediatorMiam()))
            .add(
                "miamExemptionsChecklist",
                caseData.getMiamDetails().getMiamExemptionsChecklist() != null ? caseData.getMiamDetails().getMiamExemptionsChecklist().stream()
                    .map(MiamExemptionsChecklistEnum::getDisplayedValue).collect(Collectors.joining(", "))
                    : null
            )
            .add(
                "miamDomesticViolenceChecklist",
                caseData.getMiamDetails().getMiamDomesticViolenceChecklist() != null
                    ? caseData.getMiamDetails().getMiamDomesticViolenceChecklist().stream()
                        .map(MiamDomesticViolenceChecklistEnum::getDisplayedValue)
                        .collect(Collectors.joining(", ")) : null
            )
            .add(
                "miamUrgencyReasonChecklist",
                caseData.getMiamDetails().getMiamUrgencyReasonChecklist() != null
                    ? caseData.getMiamDetails().getMiamUrgencyReasonChecklist().stream()
                        .map(MiamUrgencyReasonChecklistEnum::getDisplayedValue)
                        .collect(Collectors.joining(", ")) : null
            )
            .add(
                "miamPreviousAttendanceChecklist",
                caseData.getMiamDetails().getMiamPreviousAttendanceChecklist() != null
                    ? caseData.getMiamDetails().getMiamPreviousAttendanceChecklist().getDisplayedValue() : null
            )
            .add(
                "miamOtherGroundsChecklist",
                caseData.getMiamDetails().getMiamOtherGroundsChecklist() != null
                    ? caseData.getMiamDetails().getMiamOtherGroundsChecklist().getDisplayedValue() : null
            )
            .add("mediatorRegistrationNumber", caseData.getMiamDetails().getMediatorRegistrationNumber())
            .add("familyMediatorServiceName", caseData.getMiamDetails().getFamilyMediatorServiceName())
            .add("soleTraderName", caseData.getMiamDetails().getSoleTraderName())
            .add("mediatorRegistrationNumber1", caseData.getMiamDetails().getMediatorRegistrationNumber1())
            .add("familyMediatorServiceName1", caseData.getMiamDetails().getFamilyMediatorServiceName1())
            .add("soleTraderName1", caseData.getMiamDetails().getSoleTraderName1())
            .build();
    }
}
