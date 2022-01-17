package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.APPLICANT;

public class CourtLocatorServiceTest {

    CourtLocatorService courtLocatorService = new CourtLocatorService();

    @Test
    public void givenChildPresent_whenLivesWithApplicant_thenReturnApplicantPostcode() {
        Address applicantAddress = Address.builder()
            .addressLine1("123 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("AB12 3AL")
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .address(applicantAddress)
            .build();

        Address respondentAddress = Address.builder()
            .addressLine1("145 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("DG12 5BB")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .address(respondentAddress)
            .build();

        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(APPLICANT))
            .build();

        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();;
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();

        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant))
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();

        assert(courtLocatorService.getCorrectPartyPostcode(caseData).equals("AB12 3AL"));

    }

    @Test
    public void givenPartyDetailsPresent_whenPostcodePresent_thenReturnPostCode() {
        Address applicantAddress = Address.builder()
            .addressLine1("123 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("AB12 3AL")
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .address(applicantAddress)
            .build();

        Optional<String> postcode = courtLocatorService.getPostcode(applicant);
        assertThat(postcode.get(), is("AB12 3AL"));
    }

    @Test
    public void givenCompleteAddress_whenPostcodePresent_thenReturnTrue() {
        Address respondentAddress = Address.builder()
            .addressLine1("145 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("DG12 5BB")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .address(respondentAddress)
            .build();

        assertThat(courtLocatorService.isPostCodePresent(respondent), is(true));
    }

    @Test
    public void givenCompleteAddress_whenPostcodeNotPresent_thenReturnFalse() {
        Address respondentAddress = Address.builder()
            .addressLine1("145 Test Address")
            .postTown("London")
            .country("UK")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .address(respondentAddress)
            .build();

        assertThat(courtLocatorService.isPostCodePresent(respondent), is(false));
    }




}
