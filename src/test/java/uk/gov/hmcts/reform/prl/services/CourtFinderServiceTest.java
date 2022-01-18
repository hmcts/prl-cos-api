package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.AreaOfLaw;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtAddress;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.ANOTHER_PERSON;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.APPLICANT;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.RESPONDENT;

@RunWith(SpringRunner.class)
public class CourtFinderServiceTest {

    @InjectMocks
    CourtFinderService courtFinderService;

    @Mock
    CourtFinderApi courtFinderApi;

    private Court londonCourt;

    private Court westLondonCourt;

    private Court newcastleCourt;

    @Before
    public void init() {
        londonCourt = Court.builder()
            .name("Central Family Court")
            .slug("central-family-court")
            .areasOfLaw(Collections.singletonList(AreaOfLaw.builder().build()))
            .gbs("TESTGBS")
            .dxNumber(Collections.singletonList("160010 Kingsway 7"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();

        newcastleCourt = Court.builder()
            .name("Newcastle Civil & Family Courts and Tribunals Centre")
            .slug("newcastle-civil-family-courts-and-tribunals-centre")
            .dxNumber(Collections.singletonList("336901 Newcastle upon Tyne 55"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();

        westLondonCourt = Court.builder()
            .name("West London Family Court")
            .slug("west-london-family-court")
            .dxNumber(Collections.singletonList("310601 Feltham 4"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();


        when(courtFinderApi.getCourtDetails("newcastle-civil-family-courts-and-tribunals-centre"))
            .thenReturn(newcastleCourt);

        when(courtFinderApi.getCourtDetails("central-family-court"))
            .thenReturn(londonCourt);

        when(courtFinderApi.getCourtDetails("west-london-family-court"))
            .thenReturn(westLondonCourt);

        when(courtFinderApi.findClosestChildArrangementsCourtByPostcode("AB12 3AL"))
            .thenReturn(ServiceArea.builder()
                            .courts(Collections.singletonList(newcastleCourt))
                            .build());

        when(courtFinderApi.findClosestChildArrangementsCourtByPostcode("W9 3HE"))
            .thenReturn(ServiceArea.builder()
                            .courts(Collections.singletonList(londonCourt))
                            .build());

        when(courtFinderApi.findClosestChildArrangementsCourtByPostcode("N20 0EG"))
            .thenReturn(ServiceArea.builder()
                            .courts(Collections.singletonList(westLondonCourt))
                            .build());

    }

    @Test
    public void givenValidCaseData_whenChildLivesWithRespondent_thenReturnCourtClosestToRespondent() {
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
            .postCode("N20 0EG")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .address(respondentAddress)
            .build();

        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(RESPONDENT))
            .build();

        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();;
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();

        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant))
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();

        assertThat(courtFinderService.getClosestChildArrangementsCourt(caseData), is(westLondonCourt));

    }

    @Test
    public void givenValidCaseData_whenChildLivesWithOther_thenReturnCourtClosestToOther() {
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
            .postCode("N20 0EG")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .address(respondentAddress)
            .build();

        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(ANOTHER_PERSON))
            .address(Address.builder()
                         .postCode("W9 3HE")
                         .build())
            .build();

        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();;
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();

        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant))
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();

        assertThat(courtFinderService.getClosestChildArrangementsCourt(caseData), is(londonCourt));

    }

    @Test
    public void givenValidCaseData_whenChildLivesWithApplicant_thenReturnCourtClosestToApplicant() {

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
            .postCode("N20 0EG")
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

        assertThat(courtFinderService.getClosestChildArrangementsCourt(caseData), is(newcastleCourt));

    }

    @Test
    public void givenValidCaseDataWithMultipleApplicants_whenChildLivesWithApplicant_thenReturnCourtClosestToFirstApplicant() {

        Address applicant1Address = Address.builder()
            .addressLine1("123 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("AB12 3AL")
            .build();

        PartyDetails applicant1 = PartyDetails.builder()
            .address(applicant1Address)
            .build();

        Address applicant2Address = Address.builder()
            .addressLine1("15 Test Street")
            .postTown("London")
            .country("UK")
            .postCode("N20 0EG")
            .build();

        PartyDetails applicant2 = PartyDetails.builder()
            .address(applicant2Address)
            .build();

        Address respondentAddress = Address.builder()
            .addressLine1("145 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("W9 3HE")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .address(respondentAddress)
            .build();

        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(APPLICANT))
            .build();

        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();;
        Element<PartyDetails> wrappedApplicant1 = Element.<PartyDetails>builder().value(applicant1).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(applicant2).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();

        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(wrappedApplicant1);
        applicants.add(wrappedApplicant2);

        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(applicants)
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();


        assertThat(courtFinderService.getClosestChildArrangementsCourt(caseData), is(newcastleCourt));

    }

    @Test
    public void givenValidCourtSlug_thenReturnsCourtDetails() {

        Court basicCourt = Court.builder()
            .slug("central-family-court")
            .build();

        String courtSlug = basicCourt.getSlug();

        Court completeCourt = Court.builder()
            .name("Central Family Court")
            .slug("central-family-court")
            .areasOfLaw(Collections.singletonList(AreaOfLaw.builder().build()))
            .gbs("TESTGBS")
            .dxNumber(Collections.singletonList("160010 Kingsway 7"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();

        when(courtFinderApi.getCourtDetails(courtSlug)).thenReturn(completeCourt);
        assertThat(courtFinderService.getCourtDetails(courtSlug), is(completeCourt));

    }

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

        assert (courtFinderService.getCorrectPartyPostcode(caseData).equals("AB12 3AL"));

    }

}
