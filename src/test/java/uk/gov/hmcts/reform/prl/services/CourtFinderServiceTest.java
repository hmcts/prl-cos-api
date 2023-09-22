package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.AreaOfLaw;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtAddress;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Relations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class CourtFinderServiceTest {

    @InjectMocks
    CourtFinderService courtFinderService;

    @Mock
    CourtFinderApi courtFinderApi;

    @Mock
    private CaseData caseDataMock;

    private Court londonCourt;
    private Court westLondonCourt;
    private Court newcastleCourt;
    private Court horshamCourt;
    private PartyDetails applicant;
    private PartyDetails inValidApplicant;
    private PartyDetails applicant2;
    private PartyDetails respondent;
    private PartyDetails inValidRespondent;

    private PartyDetails respondentNoPostcode;

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
            .courtSlug("central-family-court")
            .areasOfLaw(Collections.singletonList(AreaOfLaw.builder().build()))
            .countyLocationCode(111)
            .gbs("TESTGBS")
            .dxNumber(Collections.singletonList("160010 Kingsway 7"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();

        newcastleCourt = Court.builder()
            .courtName("Newcastle Civil & Family Courts and Tribunals Centre")
            .courtSlug("newcastle-civil-family-courts-and-tribunals-centre")
            .dxNumber(Collections.singletonList("336901 Newcastle upon Tyne 55"))
            .countyLocationCode(222)
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .build();


        horshamCourt = Court.builder()
            .courtName("Horsham County Court and Family Court")
            .courtSlug("horsham-county-court-and-family-court")
            .courtEmailAddresses(Collections.singletonList(courtEmailAddress))
            .countyLocationCode(333)
            .dxNumber(Collections.singletonList("The Law Courts"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();

        westLondonCourt = Court.builder()
            .courtName("West London Family Court")
            .courtSlug("west-london-family-court")
            .dxNumber(Collections.singletonList("310601 Feltham 4"))
            .countyLocationCode(444)
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
    public void givenValidCaseData_whenChildLivesWithRespondentWithNoPostcode() throws NotFoundException {
        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(LiveWithEnum.respondent))
            .build();
        Address respondentAddressWithoutPostCode = Address.builder()
            .addressLine1(null)
            .postTown(null)
            .country(null)
            .postCode(null)
            .build();
        respondentNoPostcode = PartyDetails.builder()
            .address(respondentAddressWithoutPostCode)
            .build();
        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();
        ;
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondentNoPostcode).build();
        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant))
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();
        assertThat(courtFinderService.getNearestFamilyCourt(caseData), is(newcastleCourt));
    }

    @Test
    public void givenValidCaseData_whenChildLivesWithAnotherPersonNoPostcode() throws NotFoundException {
        OtherPersonWhoLivesWithChild person = OtherPersonWhoLivesWithChild.builder()
            .address(Address.builder()
                         .postCode(null)
                         .build())
            .build();
        Element<OtherPersonWhoLivesWithChild> wrappedPerson = Element.<OtherPersonWhoLivesWithChild>builder()
            .value(person).build();
        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(anotherPerson))
            .personWhoLivesWithChild(Collections.singletonList(wrappedPerson))
            .build();
        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .children(Collections.singletonList(wrappedChild))
            .applicants(Collections.singletonList(wrappedApplicant))
            .respondents(Collections.singletonList(wrappedRespondent))
            .build();

        assertThat(courtFinderService.getNearestFamilyCourt(caseData), is(newcastleCourt));
    }

    @Test
    public void givenValidCaseData_whenChildLivesWithRespondent_thenReturnNull() throws NotFoundException {
        C100RebuildData c100RebuildData = C100RebuildData.builder().c100RebuildChildPostCode("TW20 2PL").build();
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
            .c100RebuildData(c100RebuildData)
            .build();
        assertNull(courtFinderService.getNearestFamilyCourt(caseData));
    }

    @Test
    public void givenValidCaseData_whenChildLivesWithRespondent_thenReturnNull_logsException() throws NotFoundException {
        assertNull(courtFinderService.getNearestFamilyCourt(caseDataMock));
    }

    @Test
    public void givenInvalidPostCode_NoServiceAreaReturned() throws NotFoundException {

        Child child = Child.builder()
            .childLiveWith(Collections.emptyList())
            .build();

        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(element(child)))
            .applicants(List.of(element(PartyDetails.builder().address(Address.builder()
                                                                           .postCode("INVALID")
                                                                           .build()).build())))
            .build();

        assertNull(courtFinderService.getNearestFamilyCourt(caseData));
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
            .courtSlug("central-family-court")
            .build();
        String courtSlug = basicCourt.getCourtSlug();
        Court completeCourt = Court.builder()
            .courtName("Central Family Court")
            .courtSlug("central-family-court")
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
    public void givenChildPresent_whenLivesWithApplicant_thenReturnApplicantPostcodeV2() throws NotFoundException {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .address(Address.builder()
                         .addressLine1("address")
                         .postTown("London")
                         .build())
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .dxNumber("123456")
            .gender(Gender.female)
            .lastName("lastName")
            .previousName("testPreviousname")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<PartyDetails> partyWrapped = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> listOfParty = Collections.singletonList(partyWrapped);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);


        ChildrenAndRespondentRelation childrenAndRespondentRelation = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name").childAndRespondentRelation(RelationshipsEnum.other)
            .childLivesWith(YesOrNo.No)
            .childAndRespondentRelationOtherDetails("dsdfs")
            .build();

        Element<ChildrenAndRespondentRelation> childrenAndRespondentRelationElement =
            Element.<ChildrenAndRespondentRelation>builder().value(childrenAndRespondentRelation).build();
        List<Element<ChildrenAndRespondentRelation>> childrenAndRespondentRelationList =
            Collections.singletonList(childrenAndRespondentRelationElement);

        ChildrenAndApplicantRelation childrenAndApplicantRelation = ChildrenAndApplicantRelation.builder()
            .applicantFullName("Test")
            .childFullName("Name").childAndApplicantRelation(RelationshipsEnum.other)
            .childAndApplicantRelationOtherDetails("dsdfs")
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndApplicantRelation> childrenAndApplicantRelationElement =
            Element.<ChildrenAndApplicantRelation>builder().value(childrenAndApplicantRelation).build();
        List<Element<ChildrenAndApplicantRelation>> childrenAndApplicantRelationList = Collections.singletonList(
            childrenAndApplicantRelationElement);


        ChildrenAndOtherPeopleRelation childrenAndOtherPeopleRelation = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> childrenAndOtherPeopleRelationElement =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(childrenAndOtherPeopleRelation).build();
        List<Element<ChildrenAndOtherPeopleRelation>> childrenAndOtherPeopleRelatationList =
            Collections.singletonList(childrenAndOtherPeopleRelationElement);


        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        CaseData caseData = CaseData.builder()
                .applicants(Collections.singletonList(wrappedApplicant))
                .caseTypeOfApplication("C100")
                .taskListVersion("v2")
                .otherPartyInTheCaseRevised(listOfParty)
                .relations(Relations.builder().childAndApplicantRelations(childrenAndApplicantRelationList)
                                .childAndRespondentRelations(childrenAndRespondentRelationList)
                        .childAndOtherPeopleRelations(childrenAndOtherPeopleRelatationList).build())
                .newChildDetails(listOfChildren)
                .respondents(Collections.singletonList(wrappedRespondent))
                .build();

        assertEquals("AB12 3AL", courtFinderService.getCorrectPartyPostcode(caseData));
        Assert.assertEquals("AB12 3AL", courtFinderService.getCorrectPartyPostcode(caseData));
    }


    @Test
    public void givenChildPresent_whenLivesWithApplicant_thenReturnRespondentsPostcodeV2() throws NotFoundException {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .address(Address.builder()
                         .addressLine1("address")
                         .postTown("London")
                         .build())
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .dxNumber("123456")
            .gender(Gender.female)
            .lastName("lastName")
            .previousName("testPreviousname")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<PartyDetails> partyWrapped = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> listOfParty = Collections.singletonList(partyWrapped);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);


        ChildrenAndRespondentRelation childrenAndRespondentRelation = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name").childAndRespondentRelation(RelationshipsEnum.other)
            .childLivesWith(YesOrNo.Yes)
            .childAndRespondentRelationOtherDetails("dsdfs")
            .build();

        Element<ChildrenAndRespondentRelation> childrenAndRespondentRelationElement =
            Element.<ChildrenAndRespondentRelation>builder().value(childrenAndRespondentRelation).build();
        List<Element<ChildrenAndRespondentRelation>> childrenAndRespondentRelationList =
            Collections.singletonList(childrenAndRespondentRelationElement);

        ChildrenAndApplicantRelation childrenAndApplicantRelation = ChildrenAndApplicantRelation.builder()
            .applicantFullName("Test")
            .childFullName("Name").childAndApplicantRelation(RelationshipsEnum.other)
            .childAndApplicantRelationOtherDetails("dsdfs")
            .childLivesWith(YesOrNo.No)
            .build();

        Element<ChildrenAndApplicantRelation> childrenAndApplicantRelationElement =
            Element.<ChildrenAndApplicantRelation>builder().value(childrenAndApplicantRelation).build();
        List<Element<ChildrenAndApplicantRelation>> childrenAndApplicantRelationList = Collections.singletonList(
            childrenAndApplicantRelationElement);


        ChildrenAndOtherPeopleRelation childrenAndOtherPeopleRelation = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> childrenAndOtherPeopleRelationElement =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(childrenAndOtherPeopleRelation).build();
        List<Element<ChildrenAndOtherPeopleRelation>> childrenAndOtherPeopleRelatationList =
            Collections.singletonList(childrenAndOtherPeopleRelationElement);


        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        CaseData caseData = CaseData.builder()
                .applicants(Collections.singletonList(wrappedApplicant))
                .caseTypeOfApplication("C100")
                .taskListVersion("v2")
                .otherPartyInTheCaseRevised(listOfParty)
                .relations(Relations.builder().childAndApplicantRelations(childrenAndApplicantRelationList)
                        .childAndRespondentRelations(childrenAndRespondentRelationList)
                        .childAndOtherPeopleRelations(childrenAndOtherPeopleRelatationList).build())
                .newChildDetails(listOfChildren)
                .respondents(Collections.singletonList(wrappedRespondent))
                .build();

        assertEquals("N20 0EG", courtFinderService.getCorrectPartyPostcode(caseData));
        Assert.assertEquals("N20 0EG", courtFinderService.getCorrectPartyPostcode(caseData));
    }


    @Test
    public void givenChildPresent_whenLivesWithApplicant_thenReturnOtherWithPostcodeV2() throws NotFoundException {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .address(Address.builder()
                         .addressLine1("address")
                         .postTown("London")
                         .postCode("G511TQ")
                         .build())
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .dxNumber("123456")
            .gender(Gender.female)
            .lastName("lastName")
            .previousName("testPreviousname")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<PartyDetails> partyWrapped = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> listOfParty = Collections.singletonList(partyWrapped);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);


        ChildrenAndRespondentRelation childrenAndRespondentRelation = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name").childAndRespondentRelation(RelationshipsEnum.other)
            .childLivesWith(YesOrNo.No)
            .childAndRespondentRelationOtherDetails("dsdfs")
            .build();

        Element<ChildrenAndRespondentRelation> childrenAndRespondentRelationElement =
            Element.<ChildrenAndRespondentRelation>builder().value(childrenAndRespondentRelation).build();
        List<Element<ChildrenAndRespondentRelation>> childrenAndRespondentRelationList =
            Collections.singletonList(childrenAndRespondentRelationElement);

        ChildrenAndApplicantRelation childrenAndApplicantRelation = ChildrenAndApplicantRelation.builder()
            .applicantFullName("Test")
            .childFullName("Name").childAndApplicantRelation(RelationshipsEnum.other)
            .childAndApplicantRelationOtherDetails("dsdfs")
            .childLivesWith(YesOrNo.No)
            .build();

        Element<ChildrenAndApplicantRelation> childrenAndApplicantRelationElement =
            Element.<ChildrenAndApplicantRelation>builder().value(childrenAndApplicantRelation).build();
        List<Element<ChildrenAndApplicantRelation>> childrenAndApplicantRelationList = Collections.singletonList(
            childrenAndApplicantRelationElement);


        ChildrenAndOtherPeopleRelation childrenAndOtherPeopleRelation = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> childrenAndOtherPeopleRelationElement =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(childrenAndOtherPeopleRelation).build();
        List<Element<ChildrenAndOtherPeopleRelation>> childrenAndOtherPeopleRelatationList =
            Collections.singletonList(childrenAndOtherPeopleRelationElement);


        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        CaseData caseData = CaseData.builder()
                .applicants(Collections.singletonList(wrappedApplicant))
                .caseTypeOfApplication("C100")
                .taskListVersion("v2")
                .otherPartyInTheCaseRevised(listOfParty)
                .relations(Relations.builder().childAndApplicantRelations(childrenAndApplicantRelationList)
                        .childAndRespondentRelations(childrenAndRespondentRelationList)
                        .childAndOtherPeopleRelations(childrenAndOtherPeopleRelatationList).build())
                .newChildDetails(listOfChildren)
                .respondents(Collections.singletonList(wrappedRespondent))
                .build();

        assertEquals("G511TQ", courtFinderService.getCorrectPartyPostcode(caseData));
        Assert.assertEquals("G511TQ", courtFinderService.getCorrectPartyPostcode(caseData));
    }


    @Test
    public void givenChildPresent_whenLivesWithApplicant_thenReturnOtherPostcodeV2() throws NotFoundException {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .address(Address.builder()
                         .addressLine1("address")
                         .postTown("London")
                         .build())
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1999, 12, 10))
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .dxNumber("123456")
            .gender(Gender.female)
            .lastName("lastName")
            .previousName("testPreviousname")
            .isDateOfBirthKnown(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<PartyDetails> partyWrapped = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> listOfParty = Collections.singletonList(partyWrapped);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);


        ChildrenAndRespondentRelation childrenAndRespondentRelation = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name").childAndRespondentRelation(RelationshipsEnum.other)
            .childLivesWith(YesOrNo.No)
            .childAndRespondentRelationOtherDetails("dsdfs")
            .build();

        Element<ChildrenAndRespondentRelation> childrenAndRespondentRelationElement =
            Element.<ChildrenAndRespondentRelation>builder().value(childrenAndRespondentRelation).build();
        List<Element<ChildrenAndRespondentRelation>> childrenAndRespondentRelationList =
            Collections.singletonList(childrenAndRespondentRelationElement);

        ChildrenAndApplicantRelation childrenAndApplicantRelation = ChildrenAndApplicantRelation.builder()
            .applicantFullName("Test")
            .childFullName("Name").childAndApplicantRelation(RelationshipsEnum.other)
            .childAndApplicantRelationOtherDetails("dsdfs")
            .childLivesWith(YesOrNo.No)
            .build();

        Element<ChildrenAndApplicantRelation> childrenAndApplicantRelationElement =
            Element.<ChildrenAndApplicantRelation>builder().value(childrenAndApplicantRelation).build();
        List<Element<ChildrenAndApplicantRelation>> childrenAndApplicantRelationList = Collections.singletonList(
            childrenAndApplicantRelationElement);


        ChildrenAndOtherPeopleRelation childrenAndOtherPeopleRelation = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> childrenAndOtherPeopleRelationElement =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(childrenAndOtherPeopleRelation).build();
        List<Element<ChildrenAndOtherPeopleRelation>> childrenAndOtherPeopleRelatationList =
            Collections.singletonList(childrenAndOtherPeopleRelationElement);


        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        CaseData caseData = CaseData.builder()
            .applicants(Collections.singletonList(wrappedApplicant))
            .caseTypeOfApplication("C100")
            .taskListVersion("v2")
            .otherPartyInTheCaseRevised(listOfParty)
                .relations(Relations.builder().childAndApplicantRelations(childrenAndApplicantRelationList)
                        .childAndRespondentRelations(childrenAndRespondentRelationList)
                        .childAndOtherPeopleRelations(childrenAndOtherPeopleRelatationList).build())
            .newChildDetails(listOfChildren)
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
            .courtId("222")
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


    @Test(expected = NotFoundException.class)
    public void whenNoChildDetailsPresentThrowNotFoundException() throws NotFoundException {
        CaseData caseData = CaseData.builder()
            .children(Collections.emptyList())
            .build();

        courtFinderService.getCorrectPartyPostcode(caseData);
    }


    @Test
    public void givenC100ApplicationsMatchedInExplanationReturnCourtEmailAddress() {

        CourtEmailAddress courtEmailAddressWithC100ApplicationsKey = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .explanation("C100 applications")
            .build();

        horshamCourt.setCourtEmailAddresses(Collections.singletonList(courtEmailAddressWithC100ApplicationsKey));

        Optional<CourtEmailAddress> emailAddress = courtFinderService.getEmailAddress(horshamCourt);

        Assert.assertTrue(emailAddress.isPresent());
    }

    @Test
    public void givenDescriptionAndExplanationMatchedReturnCourtEmailAddressNull() {

        CourtEmailAddress courtEmailAddressWithBothFields = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .description("Family public law (children in care)")
            .explanation("Paper process including C100 applications")
            .build();

        horshamCourt.setCourtEmailAddresses(Collections.singletonList(courtEmailAddressWithBothFields));

        Optional<CourtEmailAddress> emailAddress = courtFinderService.getEmailAddress(null);

        Assert.assertFalse(emailAddress.isPresent());
    }

    @Test
    public void testGetPostCodeOtherPerson() {
        PartyDetails partyDetails = PartyDetails.builder()
            .address(Address.builder().postCode("G511TQ").build())
            .build();
        Assert.assertNotNull(courtFinderService.getPostCodeOtherPerson(partyDetails));
    }
}
