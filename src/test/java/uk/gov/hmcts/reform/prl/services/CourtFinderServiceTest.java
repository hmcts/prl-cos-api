package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import org.junit.Assert;
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
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;

@RunWith(SpringRunner.class)
public class CourtFinderServiceTest {

    @InjectMocks
    CourtFinderService courtFinderService;

    @Mock
    CourtFinderApi courtFinderApi;

    private Court londonCourt;
    private Court westLondonCourt;
    private Court newcastleCourt;
    private Court horshamCourt;
    private PartyDetails applicant;
    private PartyDetails inValidApplicant;
    private PartyDetails applicant2;
    private PartyDetails respondent;
    private PartyDetails inValidRespondent;

    @Before
    public void init() {

        Address applicantAddress = Address.builder()
            .addressLine1("123 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("AB12 3AL")
            .build();

        Address applicantInvalidAddress = Address.builder()
            .addressLine1("123 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("XY12 1ZC")
            .build();

        applicant = PartyDetails.builder()
            .address(applicantAddress)
            .build();

        inValidApplicant = PartyDetails.builder()
            .address(applicantInvalidAddress)
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

        Address invalidRespondentAddress = Address.builder()
            .addressLine1("145 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("XY12 1ZC")
            .build();

        respondent = PartyDetails.builder()
            .address(respondentAddress)
            .build();

        inValidRespondent = PartyDetails.builder()
            .address(invalidRespondentAddress)
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

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .build();


        horshamCourt = Court.builder()
            .courtName("Horsham County Court and Family Court")
            .courtId("horsham-county-court-and-family-court")
            .courtEmailAddresses(Collections.singletonList(courtEmailAddress))
            .dxNumber(Collections.singletonList("The Law Courts"))
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

        when(courtFinderApi.getCourtDetails("horsham-county-court-and-family-court"))
            .thenReturn(horshamCourt);

        when(courtFinderApi.getCourtDetails("central-family-court"))
            .thenReturn(londonCourt);

        when(courtFinderApi.getCourtDetails("west-london-family-court"))
            .thenReturn(westLondonCourt);
        when(courtFinderApi.findClosestChildArrangementsCourtByPostcode("AB12 3AL"))
            .thenReturn(ServiceArea.builder()
                            .courts(Collections.singletonList(newcastleCourt))
                            .build());

        when(courtFinderApi.findClosestDomesticAbuseCourtByPostCode("AB12 3AL"))
            .thenReturn(ServiceArea.builder()
                            .courts(Collections.singletonList(horshamCourt))
                            .build());

        when(courtFinderApi.findClosestDomesticAbuseCourtByPostCode("XY12 1ZC"))
            .thenReturn(ServiceArea.builder()
                            .courts(Collections.emptyList())
                            .build());

        when(courtFinderApi.findClosestChildArrangementsCourtByPostcode("XY12 1ZC"))
            .thenReturn(ServiceArea.builder()
                            .courts(Collections.emptyList())
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
        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();
        ;
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant))
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();
        assertThat(courtFinderService.getNearestFamilyCourt(caseData), is(westLondonCourt));
    }

    @Test
    public void givenInValidCaseData_NoCourtDetailsRetrieved() throws NotFoundException {
        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(LiveWithEnum.respondent))
            .build();
        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();
        ;
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(inValidApplicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(inValidRespondent).build();
        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant))
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();
        assertNull(courtFinderService.getNearestFamilyCourt(caseData));
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
        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();
        ;
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();

        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .caseTypeOfApplication("C100")
            .applicants(Collections.singletonList(wrappedApplicant))
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();
        assertThat(courtFinderService.getNearestFamilyCourt(caseData), is(londonCourt));
    }

    @Test
    public void givenValidCaseData_whenChildLivesWithApplicant_thenReturnCourtClosestToApplicant() throws NotFoundException {
        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(LiveWithEnum.applicant))
            .build();
        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();
        ;
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();

        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .caseTypeOfApplication("C100")
            .applicants(Collections.singletonList(wrappedApplicant))
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();
        assertThat(courtFinderService.getNearestFamilyCourt(caseData), is(newcastleCourt));
    }

    @Test
    public void givenValidCaseData_whenChildLivesWithApp_thenReturnCourtClosestToFirstApp() throws NotFoundException {
        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(LiveWithEnum.applicant))
            .build();
        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();
        ;
        Element<PartyDetails> wrappedApplicant1 = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(applicant2).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(wrappedApplicant1);
        applicants.add(wrappedApplicant2);
        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(applicants)
            .caseTypeOfApplication("C100")
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();

        assertThat(courtFinderService.getNearestFamilyCourt(caseData), is(newcastleCourt));
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
        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();
        ;
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant))
            .caseTypeOfApplication("C100")
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();

        assertEquals("AB12 3AL", courtFinderService.getCorrectPartyPostcode(caseData));
        Assert.assertEquals("AB12 3AL", courtFinderService.getCorrectPartyPostcode(caseData));
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
            .caseTypeOfApplication("C100")
            .build();

        CaseData updatedCaseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant1))
            .caseTypeOfApplication("C100")
            .courtName("Newcastle Civil & Family Courts and Tribunals Centre")
            .courtId("newcastle-civil-family-courts-and-tribunals-centre")
            .build();

        assertEquals(courtFinderService.setCourtNameAndId(caseData, newcastleCourt), updatedCaseData);
        Assert.assertEquals(courtFinderService.setCourtNameAndId(caseData, newcastleCourt), updatedCaseData);

    }

    @Test
    public void givenCaseDataWithApplicantPostCode_thenReturnDaCourtNameAndEmailAddress() throws NotFoundException {
        Element<PartyDetails> wrappedApplicant1 = Element.<PartyDetails>builder().value(applicant).build();

        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(wrappedApplicant1);

        CaseData caseData = CaseData.builder()
            .applicantsFL401(applicant)
            .caseTypeOfApplication("FL401")
            .build();

        assertThat(courtFinderService.getNearestFamilyCourt(caseData), is(horshamCourt));
    }

    @Test
    public void givenDescriptionAndExplanationMatchedReturnCourtEmailAddress() {

        CourtEmailAddress courtEmailAddressWithBothFields = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .description("Family public law (children in care)")
            .explanation("Paper process including C100 applications")
            .build();

        horshamCourt.setCourtEmailAddresses(Collections.singletonList(courtEmailAddressWithBothFields));

        Optional<CourtEmailAddress> emailAddress = courtFinderService.getEmailAddress(horshamCourt);

        Assert.assertTrue(emailAddress.isPresent());
        Assert.assertEquals("brighton.breathingspace@justice.gov.uk", emailAddress.get().getAddress());
    }

    @Test
    public void givenFamilyDescriptionOnlyMatchedReturnCourtEmailAddress() {
        CourtEmailAddress courtEmailAddressWithFamilyDetails = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .description("Family public law (children in care)")
            .explanation("Paper process ")
            .build();

        horshamCourt.setCourtEmailAddresses(Collections.singletonList(courtEmailAddressWithFamilyDetails));

        Optional<CourtEmailAddress> emailAddress = courtFinderService.getEmailAddress(horshamCourt);

        Assert.assertTrue(emailAddress.isPresent());
        Assert.assertEquals("brighton.breathingspace@justice.gov.uk", emailAddress.get().getAddress());
    }

    @Test
    public void givenFamilyMatchedInDescriptionReturnCourtEmailAddress() {

        CourtEmailAddress courtEmailAddressWithFamily = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .description("Family ")
            .build();

        horshamCourt.setCourtEmailAddresses(Collections.singletonList(courtEmailAddressWithFamily));

        Optional<CourtEmailAddress> emailAddress = courtFinderService.getEmailAddress(horshamCourt);

        Assert.assertTrue(emailAddress.isPresent());
        Assert.assertEquals("brighton.breathingspace@justice.gov.uk", emailAddress.get().getAddress());
    }

    @Test
    public void givenChildOnlyMatchedInDescriptionReturnCourtEmailAddress() {


        CourtEmailAddress courtEmailExplanationWithFamily = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .explanation("Family public)")
            .build();

        horshamCourt.setCourtEmailAddresses(Collections.singletonList(courtEmailExplanationWithFamily));

        Optional<CourtEmailAddress> emailAddress = courtFinderService.getEmailAddress(horshamCourt);

        Assert.assertTrue(emailAddress.isPresent());
        Assert.assertEquals("brighton.breathingspace@justice.gov.uk", emailAddress.get().getAddress());

    }

    @Test
    public void givenFamilyMatchedInExplanationReturnCourtEmailAddress() {

        CourtEmailAddress courtEmailAddressWithChild = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .description("Family ")
            .build();

        horshamCourt.setCourtEmailAddresses(Collections.singletonList(courtEmailAddressWithChild));

        Optional<CourtEmailAddress> emailAddress = courtFinderService.getEmailAddress(horshamCourt);

        Assert.assertTrue(emailAddress.isPresent());
        Assert.assertEquals("brighton.breathingspace@justice.gov.uk", emailAddress.get().getAddress());
    }

    @Test
    public void givenNoMatchFoundReturnFirstElementFromEmailList() {

        CourtEmailAddress courtEmailAddressWithNoOtherKey = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .build();

        horshamCourt.setCourtEmailAddresses(Collections.singletonList(courtEmailAddressWithNoOtherKey));

        Optional<CourtEmailAddress> emailAddress = courtFinderService.getEmailAddress(horshamCourt);

        Assert.assertFalse(emailAddress.isPresent());
    }

    @Test
    public void givenChildOnlyMatchedInExplanationReturnCourtEmailAddress() {

        CourtEmailAddress courtEmailExplanationWithChild = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .explanation("child public)")
            .build();


        horshamCourt.setCourtEmailAddresses(Collections.singletonList(courtEmailExplanationWithChild));

        Optional<CourtEmailAddress> emailAddress = courtFinderService.getEmailAddress(horshamCourt);

        Assert.assertTrue(emailAddress.isPresent());
        Assert.assertEquals("brighton.breathingspace@justice.gov.uk", emailAddress.get().getAddress());
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
        Assert.assertTrue(courtFinderService.courtsAreTheSame(c1, c2));
    }


    @Test
    public void returnTrueWhenCourtDetailsAreBlank() {
        Court c1 = Court.builder()
            .courtName("")
            .courtId("")
            .build();

        Assert.assertTrue(courtFinderService.courtNameAndIdAreBlank(
            ofNullable(c1.getCourtName()),
            ofNullable(c1.getCourtName())
        ));

    }

    @Test
    public void returnFalseIfObjectNull() {
        Court c1 = Court.builder()
            .courtName("")
            .courtId("")
            .build();

        Court c2 = null;
        assertFalse(courtFinderService.courtsAreTheSame(c1, c2));
        Assert.assertFalse(courtFinderService.courtsAreTheSame(c1, c2));
    }
}
