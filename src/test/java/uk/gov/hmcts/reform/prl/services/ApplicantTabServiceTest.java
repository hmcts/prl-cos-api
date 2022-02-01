package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ApplicantTabServiceTest {


    ApplicationsTabService applicationsTabService;

    CaseData caseData;
    Address address;
    List<Element<PartyDetails>> partyList;

    @Before
    public void setup() {
        applicationsTabService = new ApplicationsTabService();

        address = Address.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("N12 3BH")
            .build();

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .build();

        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder().value(partyDetails).build();
        partyList = Collections.singletonList(partyDetailsElement);

        caseData = CaseData.builder()
            .applicants(partyList)
            .build();


    }

    


    @Test
    public void testApplicantTableMapper() {
        Applicant applicant = Applicant.builder()
            .firstName("First name")
            .lastName("Last name")
            .gender("Male") //the new POJOs use strings as the enums are causing errors
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .build();

        Element<Applicant> applicantElement = Element.<Applicant>builder().value(applicant).build();
        List<Element<Applicant>> expectedApplicantList = Collections.singletonList(applicantElement);
        assertEquals(expectedApplicantList, applicationsTabService.getApplicantsTable(caseData));
    }





}
