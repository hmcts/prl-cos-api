package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.AreaOfLaw;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtAddress;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.applicant;

@RunWith(SpringRunner.class)
public class CourtFinderServiceTest {

    @InjectMocks
    CourtFinderService courtFinderService;

    @Mock
    CourtFinderApi courtFinderApi;

    private Court londonCourt;

    private Court westLondonCourt;

    private Court newcastleCourt;

    private PartyDetails applicant;

    private PartyDetails applicant2;

    private PartyDetails respondent;

    @Before
    public void init() {

        Address applicantAddress = Address.builder()
            .addressLine1("123 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("AB12 3AL")
            .build();

        applicant = PartyDetails.builder()
            .address(applicantAddress)
            .build();

        Address applicant2Address = Address.builder()
            .addressLine1("145 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("W9 3HE")
            .build();

        applicant2 = PartyDetails.builder()
            .address(applicant2Address)
            .build();

        Address respondentAddress = Address.builder()
            .addressLine1("145 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("N20 0EG")
            .build();

        respondent = PartyDetails.builder()
            .address(respondentAddress)
            .build();

        londonCourt = Court.builder()
            .courtName("Central Family Court")
            .courtId("central-family-court")
            .areasOfLaw(Collections.singletonList(AreaOfLaw.builder().build()))
            .gbs("TESTGBS")
            .dxNumber(Collections.singletonList("160010 Kingsway 7"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();

        newcastleCourt = Court.builder()
            .courtName("Newcastle Civil & Family Courts and Tribunals Centre")
            .courtId("newcastle-civil-family-courts-and-tribunals-centre")
            .dxNumber(Collections.singletonList("336901 Newcastle upon Tyne 55"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();

        westLondonCourt = Court.builder()
            .courtName("West London Family Court")
            .courtId("west-london-family-court")
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
    public void givenValidCaseData_whenChildLivesWithRespondent_thenReturnCourtClosestToRespondent() throws NotFoundException {
        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(LiveWithEnum.respondent))
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
    public void givenValidCaseData_whenChildLivesWithOther_thenReturnCourtClosestToOther() throws NotFoundException {
        OtherPersonWhoLivesWithChild person = OtherPersonWhoLivesWithChild.builder()
            .address(Address.builder()
                         .postCode("W9 3HE")
                         .build())
            .build();

        Element<OtherPersonWhoLivesWithChild> wrappedPerson = Element.<OtherPersonWhoLivesWithChild>builder()
            .value(person).build();

        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(anotherPerson))
            .personWhoLivesWithChild(Collections.singletonList(wrappedPerson))
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
    public void givenValidCaseData_whenChildLivesWithApplicant_thenReturnCourtClosestToApplicant() throws NotFoundException {
        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(LiveWithEnum.applicant))
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
    public void givenValidCaseData_whenChildLivesWithApp_thenReturnCourtClosestToFirstApp() throws NotFoundException {
        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(LiveWithEnum.applicant))
            .build();

        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();;
        Element<PartyDetails> wrappedApplicant1 = Element.<PartyDetails>builder().value(applicant).build();
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
            .courtId("central-family-court")
            .build();

        String courtSlug = basicCourt.getCourtId();
        Court completeCourt = Court.builder()
            .courtName("Central Family Court")
            .courtId("central-family-court")
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
    public void givenChildPresent_whenLivesWithApplicant_thenReturnApplicantPostcode() throws NotFoundException {
        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(LiveWithEnum.applicant))
            .build();

        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();;
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();

        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant))
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();

        assertEquals("AB12 3AL", courtFinderService.getCorrectPartyPostcode(caseData));
    }

    @Test
    public void givenCaseDataWithCourtDetails_thenCourtDetailsNotChanged() {
        Court completeCourt = Court.builder()
            .courtName("Central Family Court")
            .courtId("central-family-court")
            .areasOfLaw(Collections.singletonList(AreaOfLaw.builder().build()))
            .gbs("TESTGBS")
            .dxNumber(Collections.singletonList("160010 Kingsway 7"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();

        CaseData caseData = CaseData.builder()
            .court(completeCourt)
            .courtName("Test Court Name")
            .courtId("test-court-id")
            .build();

        CaseData caseDataUpdated = courtFinderService.setCourtUnlessCourtAlreadyPresent(caseData, completeCourt);

        assertEquals(caseData, caseDataUpdated);

    }

    @Test
    public void givenCaseDataWithNoCourtDetails_thenCourtDetailsUpdated() {
        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(LiveWithEnum.applicant))
            .build();

        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();
        Element<PartyDetails> wrappedApplicant1 = Element.<PartyDetails>builder().value(applicant).build();

        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant1))
            .build();

        CaseData updatedCaseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant1))
            .courtName("Newcastle Civil & Family Courts and Tribunals Centre")
            .courtId("newcastle-civil-family-courts-and-tribunals-centre")
            .build();

        assertEquals(courtFinderService.setCourtNameAndId(caseData, newcastleCourt), updatedCaseData);

    }

    @Test
    public void givenIdenticalCourts_whenComparing_returnsTrue() {
        Court c1 = Court.builder()
            .courtName("Central Family Court")
            .courtId("central-family-court")
            .areasOfLaw(Collections.singletonList(AreaOfLaw.builder().build()))
            .gbs("TESTGBS")
            .dxNumber(Collections.singletonList("160010 Kingsway 7"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();

        Court c2 = Court.builder()
            .courtName("Central Family Court")
            .courtId("central-family-court")
            .areasOfLaw(Collections.singletonList(AreaOfLaw.builder().build()))
            .gbs("TESTGBS")
            .dxNumber(Collections.singletonList("160010 Kingsway 7"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();

        assertTrue(courtFinderService.courtsAreTheSame(c1, c2));
    }

    @Test
    public void returnTrueWhenCourtDetailsAreBlank() {
        Court c1 = Court.builder()
            .courtName("")
            .courtId("")
            .build();

        assertTrue(courtFinderService.courtNameAndIdAreBlank(ofNullable(c1.getCourtName()),
                                                             ofNullable(c1.getCourtName())));
    }

    @Test
    public void returnFalseIfObjectNull() {
        Court c1 = Court.builder()
            .courtName("")
            .courtId("")
            .build();

        Court c2 = null;
        assertFalse(courtFinderService.courtsAreTheSame(c1, c2));
    }
}
