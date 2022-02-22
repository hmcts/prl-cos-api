package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import javax.json.JsonObject;
import javax.json.stream.JsonCollectors;
import java.util.stream.Collectors;

@Component
public class MiamMapper {

    public JsonObject map(CaseData caseData) {

        return new NullAwareJsonObjectBuilder()
            .add("applicantAttendedMiam", CommonUtils.getYesOrNoValue(caseData.getApplicantAttendedMiam()))
            .add("claimingExemptionMiam", CommonUtils.getYesOrNoValue(caseData.getClaimingExemptionMiam()))
            .add("familyMediatorMiam", CommonUtils.getYesOrNoValue(caseData.getFamilyMediatorMiam()))
            .add("miamExemptionsChecklist", String.valueOf(caseData.getMiamExemptionsChecklist()))
            .add("miamDomesticViolenceChecklist", String.valueOf(caseData.getMiamDomesticViolenceChecklist()))
            .add("miamUrgencyReasonChecklist", String.valueOf(caseData.getMiamUrgencyReasonChecklist()))
            .add("miamPreviousAttendanceChecklist", String.valueOf(caseData.getMiamPreviousAttendanceChecklist()))
            .add("miamOtherGroundsChecklist", String.valueOf(caseData.getMiamOtherGroundsChecklist()))
            .add("mediatorRegistrationNumber", caseData.getMediatorRegistrationNumber())
            .add("familyMediatorServiceName", caseData.getFamilyMediatorServiceName())
            .add("soleTraderName", caseData.getSoleTraderName())
            .add("mediatorRegistrationNumber1", caseData.getMediatorRegistrationNumber1())
            .add("familyMediatorServiceName1", caseData.getFamilyMediatorServiceName1())
            .add("soleTraderName1", caseData.getSoleTraderName1())
            .build();
    }
}
