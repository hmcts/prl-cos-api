package uk.gov.hmcts.reform.prl.courtnav.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ApplicantsDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.AddressMapper;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Fl401ApplicantMapper {
    private final AddressMapper addressMapper;

    public JsonObject map(CourtNavCaseData courtNavCaseData) {
        ApplicantsDetails fl401Applicant = courtNavCaseData.getApplicantDetails();

        return new NullAwareJsonObjectBuilder()
            .add("firstName", fl401Applicant.getApplicantFirstName())
            .add("lastName", fl401Applicant.getApplicantLastName())
            .add("previousName", fl401Applicant.getApplicantOtherNames())
            .add("dateOfBirth", String.valueOf(fl401Applicant.getApplicantDateOfBirth()))
            .add("gender", fl401Applicant.getApplicantGender().getDisplayedValue())
            .add("otherGender", fl401Applicant.getOtherGender())
            .add("email", fl401Applicant.getApplicantEmailAddress())
            .add("phoneNumber", fl401Applicant.getApplicantPhoneNumber())
            .add("address", addressMapper.mapAddress(fl401Applicant.getApplicantAddress()))
            .add("isAddressConfidential", fl401Applicant.getShareContactDetailsWithRespondent().getDisplayedValue().equals("No") ? "No" : "Yes")
            .add("isPhoneNumberConfidential", fl401Applicant.getShareContactDetailsWithRespondent().getDisplayedValue().equals("No") ? "No" : "Yes")
            .add("isEmailAddressConfidential", fl401Applicant.getShareContactDetailsWithRespondent().getDisplayedValue().equals("No") ? "No" : "Yes")
            .add("representativeFirstName", fl401Applicant.getLegalRepresentativeFirstName())
            .add("representativeLastName", fl401Applicant.getLegalRepresentativeLastName())
            .add("solicitorTelephone", fl401Applicant.getLegalRepresentativePhone())
            .add("solicitorReference", fl401Applicant.getLegalRepresentativeReference())
            .add("solicitorAddress", addressMapper.mapAddress(fl401Applicant.getLegalRepresentativeAddress()))
            .add("dxNumber", fl401Applicant.getLegalRepresentativeDx())
            .build();
    }
}
