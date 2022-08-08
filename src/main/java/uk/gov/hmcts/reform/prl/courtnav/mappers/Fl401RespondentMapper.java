package uk.gov.hmcts.reform.prl.courtnav.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.RespondentDetails;
import uk.gov.hmcts.reform.prl.rpa.mappers.AddressMapper;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import javax.json.JsonObject;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Fl401RespondentMapper {
    private final AddressMapper addressMapper;

    public JsonObject map(CourtNavCaseData courtNavCaseData) {
        RespondentDetails fl401Respondent = courtNavCaseData.getRespondentDetails();

        return new NullAwareJsonObjectBuilder()
            .add("firstName", fl401Respondent.getRespondentFirstName())
            .add("lastName", fl401Respondent.getRespondentLastName())
            .add("previousName", fl401Respondent.getRespondentOtherNames())
            .add("dateOfBirth", String.valueOf(fl401Respondent.getRespondentDateOfBirth()))
            .add("email", fl401Respondent.getRespondentEmailAddress())
            .add("phoneNumber", fl401Respondent.getRespondentPhoneNumber())
            .add("address", addressMapper.mapAddress(fl401Respondent.getRespondentAddress()))
            .add("respondentLivedWithApplicant", CommonUtils.getYesOrNoValue(fl401Respondent.getRespondentLivesWithApplicant()))
            .build();
    }
}
