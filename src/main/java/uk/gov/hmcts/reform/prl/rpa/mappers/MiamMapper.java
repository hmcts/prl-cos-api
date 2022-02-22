package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.stream.Collectors;
import javax.json.JsonObject;

@Component
public class MiamMapper {

    public JsonObject map(CaseData caseData) {

        return new NullAwareJsonObjectBuilder()
            .add("applicantAttendedMiam", CommonUtils.getYesOrNoValue(caseData.getApplicantAttendedMiam()))
            .add("claimingExemptionMiam", CommonUtils.getYesOrNoValue(caseData.getClaimingExemptionMiam()))
            .add("familyMediatorMiam", CommonUtils.getYesOrNoValue(caseData.getFamilyMediatorMiam()))
            .add(
                "miamExemptionsChecklist",
                caseData.getMiamExemptionsChecklist().isEmpty() ? null : caseData.getMiamExemptionsChecklist().stream()
                    .map(MiamExemptionsChecklistEnum::getDisplayedValue).collect(Collectors.joining(", "))
            )
            .add(
                "miamDomesticViolenceChecklist",
                caseData.getMiamDomesticViolenceChecklist().isEmpty() ? null :
                    caseData.getMiamDomesticViolenceChecklist().stream()
                    .map(MiamDomesticViolenceChecklistEnum::getDisplayedValue)
                        .collect(Collectors.joining(", "))
            )
            .add(
                "miamUrgencyReasonChecklist",
                caseData.getMiamUrgencyReasonChecklist().isEmpty() ? null :
                    caseData.getMiamUrgencyReasonChecklist().stream()
                    .map(MiamUrgencyReasonChecklistEnum::getDisplayedValue).collect(Collectors.joining(", "))
            )
            .add(
                "miamPreviousAttendanceChecklist",
                caseData.getMiamPreviousAttendanceChecklist() != null
                    ? caseData.getMiamPreviousAttendanceChecklist().getDisplayedValue() : null
            )
            .add(
                "miamOtherGroundsChecklist",
                caseData.getMiamOtherGroundsChecklist() != null
                    ? caseData.getMiamOtherGroundsChecklist().getDisplayedValue() : null
            )
            .add("mediatorRegistrationNumber", caseData.getMediatorRegistrationNumber())
            .add("familyMediatorServiceName", caseData.getFamilyMediatorServiceName())
            .add("soleTraderName", caseData.getSoleTraderName())
            .add("mediatorRegistrationNumber1", caseData.getMediatorRegistrationNumber1())
            .add("familyMediatorServiceName1", caseData.getFamilyMediatorServiceName1())
            .add("soleTraderName1", caseData.getSoleTraderName1())
            .build();
    }
}
