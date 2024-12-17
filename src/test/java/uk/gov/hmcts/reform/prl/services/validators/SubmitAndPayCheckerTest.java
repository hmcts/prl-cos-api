package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.respondent;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum.noNotRequired;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SubmitAndPayCheckerTest {

    @InjectMocks
    SubmitAndPayChecker submitAndPayChecker;

    @Mock
    EventsChecker eventsChecker;

    @Mock
    CaseNameChecker caseNameChecker;

    @Mock
    ChildrenAndApplicantsChecker childrenAndApplicantsChecker;

    @Mock
    ChildrenAndRespondentsChecker childrenAndRespondentsChecker;
    @Mock
    ChildrenAndOtherPeopleInThisApplicationChecker childrenAndOtherPeopleInThisApplicationChecker;

    @Mock
    ApplicationTypeChecker applicationTypeChecker;

    @Mock
    HearingUrgencyChecker hearingUrgencyChecker;

    @Mock
    ApplicantsChecker applicantsChecker;

    @Mock
    ChildChecker childChecker;

    @Mock
    ChildDetailsRevisedChecker childDetailsRevisedChecker;

    @Mock
    MiamPolicyUpgradeChecker miamPolicyUpgradeChecker;

    @Mock
    RespondentsChecker respondentsChecker;

    @Mock
    MiamChecker miamChecker;

    @Mock
    AllegationsOfHarmChecker allegationsOfHarmChecker;

    @Mock
    AllegationsOfHarmRevisedChecker allegationsOfHarmRevisedChecker;

    @Mock
    OtherPeopleInTheCaseChecker otherPeopleInTheCaseChecker;


    @Mock
    OtherPeopleInTheCaseRevisedChecker otherPeopleInTheCaseRevisedChecker;

    @Mock
    OtherChildrenNotPartOfTheApplicationChecker otherChildrenNotPartOfTheApplicationChecker;

    @Mock
    OtherProceedingsChecker otherProceedingsChecker;

    @Mock
    AttendingTheHearingChecker attendingTheHearingChecker;

    @Mock
    InternationalElementChecker internationalElementChecker;

    @Mock
    LitigationCapacityChecker litigationCapacityChecker;

    @Mock
    WelshLanguageRequirementsChecker welshLanguageRequirementsChecker;


    private CaseData caseData;
    private Address address;
    private Organisation organisation;
    private PartyDetails applicant;
    private Child child;

    private ChildDetailsRevised childDetailsRevised;
    private PartyDetails respondents;

    private Element<PartyDetails> wrappedApplicant;
    private List<Element<PartyDetails>> applicantList;
    private Element<Child> wrappedChildren;
    private List<Element<Child>> listOfChildren;
    private Element<ChildDetailsRevised> wrappedChildDetailsRevised;
    private List<Element<ChildDetailsRevised>> listOfChildDetailsRevised;
    private Element<PartyDetails> wrappedRespondents;
    private List<Element<PartyDetails>> respondentsList;

    @Before
    public void setUp() {

        address = Address.builder()
            .addressLine1("Test")
            .addressLine2("Test")
            .addressLine3("Test")
            .county("London")
            .country("UK")
            .postTown("Southgate")
            .postCode("N14 5EF")
            .build();

        organisation = Organisation.builder()
            .organisationID("TestingID")
            .organisationName("TestingName")
            .build();

        applicant = PartyDetails.builder()
            .firstName("TestName")
            .lastName("TestLastName")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(female)
            .placeOfBirth("London")
            .address(address)
            .isAddressConfidential(Yes)
            .isAtAddressLessThan5Years(Yes)
            .canYouProvideEmailAddress(No)
            .phoneNumber("1234567876")
            .isPhoneNumberConfidential(Yes)
            .representativeFirstName("testName")
            .representativeLastName("testLastName")
            .solicitorEmail("testing@gmail.com")
            .solicitorOrg(organisation)
            .build();

        child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(respondent))
            .parentalResponsibilityDetails("test")
            .build();

        childDetailsRevised = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        respondents = PartyDetails.builder()
            .firstName("TestingName")
            .lastName("TestLastName")
            .gender(female)
            .isDateOfBirthKnown(No)
            .isPlaceOfBirthKnown(No)
            .isCurrentAddressKnown(No)
            .isAtAddressLessThan5Years(No)
            .canYouProvideEmailAddress(No)
            .canYouProvidePhoneNumber(No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        applicantList = Collections.singletonList(wrappedApplicant);

        wrappedChildren = Element.<Child>builder().value(child).build();
        listOfChildren = Collections.singletonList(wrappedChildren);

        wrappedChildDetailsRevised = Element.<ChildDetailsRevised>builder().value(childDetailsRevised).build();
        listOfChildDetailsRevised = Collections.singletonList(wrappedChildDetailsRevised);


        wrappedRespondents = Element.<PartyDetails>builder().value(respondents).build();
        respondentsList = Collections.singletonList(wrappedRespondents);



    }

    @Test
    public void whenNoDataEnteredThenIsStartedReturnFalse() {
        caseData = CaseData.builder().build();
        assertFalse(submitAndPayChecker.isStarted(caseData));
    }

    @Test
    public void whenNoDataEnteredThenIsFinishedReturnFalse() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(caseNameChecker.isFinished(caseData)).thenReturn(false);
        when(applicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(applicationTypeChecker.isFinished(caseData)).thenReturn(false);
        when(hearingUrgencyChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(hearingUrgencyChecker.isFinished(caseData)).thenReturn(false);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(applicantsChecker.isFinished(caseData)).thenReturn(false);
        when(childChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(childChecker.isFinished(caseData)).thenReturn(false);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(respondentsChecker.isFinished(caseData)).thenReturn(false);
        when(miamChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(miamChecker.isFinished(caseData)).thenReturn(false);
        when(allegationsOfHarmRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(allegationsOfHarmRevisedChecker.isFinished(caseData)).thenReturn(false);
        when(otherPeopleInTheCaseRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(otherPeopleInTheCaseRevisedChecker.isFinished(caseData)).thenReturn(false);
        when(otherProceedingsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(otherProceedingsChecker.isFinished(caseData)).thenReturn(false);
        when(attendingTheHearingChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(attendingTheHearingChecker.isFinished(caseData)).thenReturn(false);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(litigationCapacityChecker.isFinished(caseData)).thenReturn(false);
        when(welshLanguageRequirementsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(welshLanguageRequirementsChecker.isFinished(caseData)).thenReturn(false);


        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getApplicationTypeChecker()).thenReturn(applicationTypeChecker);
        when(eventsChecker.getHearingUrgencyChecker()).thenReturn(hearingUrgencyChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getChildChecker()).thenReturn(childChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getMiamChecker()).thenReturn(miamChecker);
        when(eventsChecker.getAllegationsOfHarmChecker()).thenReturn(allegationsOfHarmChecker);
        when(eventsChecker.getOtherPeopleInTheCaseRevisedChecker()).thenReturn(otherPeopleInTheCaseRevisedChecker);
        when(eventsChecker.getOtherProceedingsChecker()).thenReturn(otherProceedingsChecker);
        when(eventsChecker.getAttendingTheHearingChecker()).thenReturn(attendingTheHearingChecker);
        when(eventsChecker.getInternationalElementChecker()).thenReturn(internationalElementChecker);
        when(eventsChecker.getLitigationCapacityChecker()).thenReturn(litigationCapacityChecker);
        when(eventsChecker.getWelshLanguageRequirementsChecker()).thenReturn(welshLanguageRequirementsChecker);


        assertFalse(submitAndPayChecker.isFinished(caseData));
    }

    @Test
    public void whenPartialCasaDataPresentThenIsFinshedReturnFalse() {

        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("testing")
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(Yes)
            .build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(caseNameChecker.isFinished(caseData)).thenReturn(false);
        when(applicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(applicationTypeChecker.isFinished(caseData)).thenReturn(false);
        when(hearingUrgencyChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(hearingUrgencyChecker.isFinished(caseData)).thenReturn(false);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(applicantsChecker.isFinished(caseData)).thenReturn(false);
        when(childChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(childChecker.isFinished(caseData)).thenReturn(false);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(respondentsChecker.isFinished(caseData)).thenReturn(false);
        when(miamChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(miamChecker.isFinished(caseData)).thenReturn(false);
        when(allegationsOfHarmChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(allegationsOfHarmChecker.isFinished(caseData)).thenReturn(false);
        when(otherPeopleInTheCaseChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(otherPeopleInTheCaseChecker.isFinished(caseData)).thenReturn(false);
        when(otherProceedingsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(otherProceedingsChecker.isFinished(caseData)).thenReturn(false);
        when(attendingTheHearingChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(attendingTheHearingChecker.isFinished(caseData)).thenReturn(false);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(litigationCapacityChecker.isFinished(caseData)).thenReturn(false);
        when(welshLanguageRequirementsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(welshLanguageRequirementsChecker.isFinished(caseData)).thenReturn(false);


        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getApplicationTypeChecker()).thenReturn(applicationTypeChecker);
        when(eventsChecker.getHearingUrgencyChecker()).thenReturn(hearingUrgencyChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getChildChecker()).thenReturn(childChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getMiamChecker()).thenReturn(miamChecker);
        when(eventsChecker.getMiamPolicyUpgradeChecker()).thenReturn(miamPolicyUpgradeChecker);
        when(eventsChecker.getAllegationsOfHarmChecker()).thenReturn(allegationsOfHarmChecker);
        when(eventsChecker.getOtherPeopleInTheCaseChecker()).thenReturn(otherPeopleInTheCaseChecker);
        when(eventsChecker.getOtherProceedingsChecker()).thenReturn(otherProceedingsChecker);
        when(eventsChecker.getAttendingTheHearingChecker()).thenReturn(attendingTheHearingChecker);
        when(eventsChecker.getInternationalElementChecker()).thenReturn(internationalElementChecker);
        when(eventsChecker.getLitigationCapacityChecker()).thenReturn(litigationCapacityChecker);
        when(eventsChecker.getWelshLanguageRequirementsChecker()).thenReturn(welshLanguageRequirementsChecker);

        assertFalse(submitAndPayChecker.isFinished(caseData));
    }

    @Test
    public void whenAllCaseDataPresentThenIsFinshedReturnTrue() {

        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("testing")
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(Yes)
            .applicationPermissionRequired(noNotRequired)
            .applicationDetails("Test details")
            .isCaseUrgent(Yes)
            .doYouNeedAWithoutNoticeHearing(Yes)
            .caseUrgencyTimeAndReason("reason")
            .effortsMadeWithRespondents("efforts")
            .reasonsForApplicationWithoutNotice("test")
            .setOutReasonsBelow("test")
            .areRespondentsAwareOfProceedings(No)
            .doYouRequireAHearingWithReducedNotice(No)
            .applicants(applicantList)
            .children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
            .respondents(respondentsList)
            .miamDetails(MiamDetails.builder()
                             .applicantAttendedMiam(Yes)
                             .mediatorRegistrationNumber("123456")
                             .familyMediatorServiceName("Test Name")
                             .soleTraderName("Trade Sole")
                             .miamCertificationDocumentUpload(Document.builder().build())
                             .build())
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(No).build())

            .build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(caseNameChecker.isFinished(caseData)).thenReturn(true);
        when(applicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicationTypeChecker.isFinished(caseData)).thenReturn(true);
        when(hearingUrgencyChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(hearingUrgencyChecker.isFinished(caseData)).thenReturn(true);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicantsChecker.isFinished(caseData)).thenReturn(true);
        when(childChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childChecker.isFinished(caseData)).thenReturn(true);
        when(childDetailsRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childDetailsRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(respondentsChecker.isFinished(caseData)).thenReturn(true);
        when(miamChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(miamChecker.isFinished(caseData)).thenReturn(true);
        when(allegationsOfHarmChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(allegationsOfHarmChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.isFinished(caseData)).thenReturn(true);
        when(otherProceedingsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherProceedingsChecker.isFinished(caseData)).thenReturn(true);
        when(otherChildrenNotPartOfTheApplicationChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherChildrenNotPartOfTheApplicationChecker.isFinished(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.isFinished(caseData)).thenReturn(true);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(litigationCapacityChecker.isFinished(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.isFinished(caseData)).thenReturn(true);


        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getApplicationTypeChecker()).thenReturn(applicationTypeChecker);
        when(eventsChecker.getHearingUrgencyChecker()).thenReturn(hearingUrgencyChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getChildChecker()).thenReturn(childChecker);
        when(eventsChecker.getChildDetailsRevisedChecker()).thenReturn(childDetailsRevisedChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getMiamChecker()).thenReturn(miamChecker);
        when(eventsChecker.getMiamPolicyUpgradeChecker()).thenReturn(miamPolicyUpgradeChecker);
        when(eventsChecker.getAllegationsOfHarmChecker()).thenReturn(allegationsOfHarmChecker);
        when(eventsChecker.getOtherChildrenNotPartOfTheApplicationChecker()).thenReturn(otherChildrenNotPartOfTheApplicationChecker);
        when(eventsChecker.getOtherPeopleInTheCaseChecker()).thenReturn(otherPeopleInTheCaseChecker);
        when(eventsChecker.getOtherProceedingsChecker()).thenReturn(otherProceedingsChecker);
        when(eventsChecker.getAttendingTheHearingChecker()).thenReturn(attendingTheHearingChecker);
        when(eventsChecker.getInternationalElementChecker()).thenReturn(internationalElementChecker);
        when(eventsChecker.getLitigationCapacityChecker()).thenReturn(litigationCapacityChecker);
        when(eventsChecker.getWelshLanguageRequirementsChecker()).thenReturn(welshLanguageRequirementsChecker);

        assertTrue(submitAndPayChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenHasMandatoryCompletedReturnFalse() {

        caseData = CaseData.builder().build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(caseNameChecker.isFinished(caseData)).thenReturn(false);
        when(applicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(applicationTypeChecker.isFinished(caseData)).thenReturn(false);
        when(hearingUrgencyChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(hearingUrgencyChecker.isFinished(caseData)).thenReturn(false);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(applicantsChecker.isFinished(caseData)).thenReturn(false);
        when(childChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(childChecker.isFinished(caseData)).thenReturn(false);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(respondentsChecker.isFinished(caseData)).thenReturn(false);
        when(miamChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(miamChecker.isFinished(caseData)).thenReturn(false);
        when(allegationsOfHarmChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(allegationsOfHarmChecker.isFinished(caseData)).thenReturn(false);
        when(otherPeopleInTheCaseChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(otherPeopleInTheCaseChecker.isFinished(caseData)).thenReturn(false);
        when(otherProceedingsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(otherProceedingsChecker.isFinished(caseData)).thenReturn(false);
        when(attendingTheHearingChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(attendingTheHearingChecker.isFinished(caseData)).thenReturn(false);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(litigationCapacityChecker.isFinished(caseData)).thenReturn(false);
        when(welshLanguageRequirementsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(welshLanguageRequirementsChecker.isFinished(caseData)).thenReturn(false);


        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getApplicationTypeChecker()).thenReturn(applicationTypeChecker);
        when(eventsChecker.getHearingUrgencyChecker()).thenReturn(hearingUrgencyChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getChildChecker()).thenReturn(childChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getMiamChecker()).thenReturn(miamChecker);
        when(eventsChecker.getAllegationsOfHarmChecker()).thenReturn(allegationsOfHarmChecker);
        when(eventsChecker.getOtherPeopleInTheCaseChecker()).thenReturn(otherPeopleInTheCaseChecker);
        when(eventsChecker.getOtherProceedingsChecker()).thenReturn(otherProceedingsChecker);
        when(eventsChecker.getAttendingTheHearingChecker()).thenReturn(attendingTheHearingChecker);
        when(eventsChecker.getInternationalElementChecker()).thenReturn(internationalElementChecker);
        when(eventsChecker.getLitigationCapacityChecker()).thenReturn(litigationCapacityChecker);
        when(eventsChecker.getWelshLanguageRequirementsChecker()).thenReturn(welshLanguageRequirementsChecker);


        assertFalse(submitAndPayChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenAllMandatoryCaseDataPresentThenHasMandatoryCompletedReturnTrue() {

        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("testing")
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(Yes)
            .applicationPermissionRequired(noNotRequired)
            .applicationDetails("Test details")
            .isCaseUrgent(Yes)
            .doYouNeedAWithoutNoticeHearing(Yes)
            .caseUrgencyTimeAndReason("reason")
            .effortsMadeWithRespondents("efforts")
            .reasonsForApplicationWithoutNotice("test")
            .setOutReasonsBelow("test")
            .areRespondentsAwareOfProceedings(No)
            .doYouRequireAHearingWithReducedNotice(No)
            .applicants(applicantList)
            .children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
            .respondents(respondentsList)
            .miamDetails(MiamDetails.builder()
                             .applicantAttendedMiam(Yes)
                             .mediatorRegistrationNumber("123456")
                             .familyMediatorServiceName("Test Name")
                             .soleTraderName("Trade Sole")
                             .miamCertificationDocumentUpload(Document.builder().build())
                             .build())
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(No).build())
            .build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(caseNameChecker.isFinished(caseData)).thenReturn(true);
        when(applicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicationTypeChecker.isFinished(caseData)).thenReturn(true);
        when(hearingUrgencyChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(hearingUrgencyChecker.isFinished(caseData)).thenReturn(true);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicantsChecker.isFinished(caseData)).thenReturn(true);
        when(childChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childChecker.isFinished(caseData)).thenReturn(true);
        when(childDetailsRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childDetailsRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(respondentsChecker.isFinished(caseData)).thenReturn(true);
        when(miamChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(miamChecker.isFinished(caseData)).thenReturn(true);
        when(allegationsOfHarmChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(allegationsOfHarmChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.isFinished(caseData)).thenReturn(true);
        when(otherProceedingsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherProceedingsChecker.isFinished(caseData)).thenReturn(true);
        when(otherChildrenNotPartOfTheApplicationChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherChildrenNotPartOfTheApplicationChecker.isFinished(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.isFinished(caseData)).thenReturn(true);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(litigationCapacityChecker.isFinished(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.isFinished(caseData)).thenReturn(true);


        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getApplicationTypeChecker()).thenReturn(applicationTypeChecker);
        when(eventsChecker.getHearingUrgencyChecker()).thenReturn(hearingUrgencyChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getChildChecker()).thenReturn(childChecker);
        when(eventsChecker.getChildDetailsRevisedChecker()).thenReturn(childDetailsRevisedChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getMiamChecker()).thenReturn(miamChecker);
        when(eventsChecker.getMiamPolicyUpgradeChecker()).thenReturn(miamPolicyUpgradeChecker);
        when(eventsChecker.getAllegationsOfHarmChecker()).thenReturn(allegationsOfHarmChecker);
        when(eventsChecker.getOtherChildrenNotPartOfTheApplicationChecker()).thenReturn(otherChildrenNotPartOfTheApplicationChecker);
        when(eventsChecker.getOtherPeopleInTheCaseChecker()).thenReturn(otherPeopleInTheCaseChecker);
        when(eventsChecker.getOtherProceedingsChecker()).thenReturn(otherProceedingsChecker);
        when(eventsChecker.getAttendingTheHearingChecker()).thenReturn(attendingTheHearingChecker);
        when(eventsChecker.getInternationalElementChecker()).thenReturn(internationalElementChecker);
        when(eventsChecker.getLitigationCapacityChecker()).thenReturn(litigationCapacityChecker);
        when(eventsChecker.getWelshLanguageRequirementsChecker()).thenReturn(welshLanguageRequirementsChecker);

        assertTrue(submitAndPayChecker.hasMandatoryCompleted(caseData));
    }


    @Test
    public void whenAllMandatoryCaseDataPresentThenHasMandatoryCompletedReturnTrueForV2() {
        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("testing")
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(Yes)
            .applicationPermissionRequired(noNotRequired)
            .applicationDetails("Test details")
            .isCaseUrgent(Yes)
            .taskListVersion(TASK_LIST_VERSION_V2)
            .doYouNeedAWithoutNoticeHearing(Yes)
            .caseUrgencyTimeAndReason("reason")
            .effortsMadeWithRespondents("efforts")
            .reasonsForApplicationWithoutNotice("test")
            .setOutReasonsBelow("test")
            .consentOrder(Yes)
            .areRespondentsAwareOfProceedings(No)
            .doYouRequireAHearingWithReducedNotice(No)
            .applicants(applicantList)
            .newChildDetails(listOfChildDetailsRevised)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
            .respondents(respondentsList)
            .miamDetails(MiamDetails.builder()
                             .applicantAttendedMiam(Yes)
                             .mediatorRegistrationNumber("123456")
                             .familyMediatorServiceName("Test Name")
                             .soleTraderName("Trade Sole")
                             .miamCertificationDocumentUpload(Document.builder().build())
                             .build())
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                         .newAllegationsOfHarmYesNo(No).build())
            .build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(caseNameChecker.isFinished(caseData)).thenReturn(true);
        when(applicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicationTypeChecker.isFinished(caseData)).thenReturn(true);
        when(hearingUrgencyChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(hearingUrgencyChecker.isFinished(caseData)).thenReturn(true);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicantsChecker.isFinished(caseData)).thenReturn(true);
        when(childChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childChecker.isFinished(caseData)).thenReturn(true);
        when(childDetailsRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childDetailsRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(childrenAndApplicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childrenAndApplicantsChecker.isFinished(caseData)).thenReturn(true);
        when(childrenAndRespondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childrenAndRespondentsChecker.isFinished(caseData)).thenReturn(true);
        when(childrenAndOtherPeopleInThisApplicationChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childrenAndOtherPeopleInThisApplicationChecker.isFinished(caseData)).thenReturn(true);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(respondentsChecker.isFinished(caseData)).thenReturn(true);
        when(miamChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(miamChecker.isFinished(caseData)).thenReturn(true);
        when(allegationsOfHarmRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(allegationsOfHarmRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(otherProceedingsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherProceedingsChecker.isFinished(caseData)).thenReturn(true);
        when(otherChildrenNotPartOfTheApplicationChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherChildrenNotPartOfTheApplicationChecker.isFinished(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.isFinished(caseData)).thenReturn(true);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(litigationCapacityChecker.isFinished(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.isFinished(caseData)).thenReturn(true);
        when(eventsChecker.getChildrenAndApplicantsChecker()).thenReturn(childrenAndApplicantsChecker);
        when(eventsChecker.getChildrenAndRespondentsChecker()).thenReturn(childrenAndRespondentsChecker);
        when(eventsChecker.getChildrenAndOtherPeopleInThisApplicationChecker()).thenReturn(childrenAndOtherPeopleInThisApplicationChecker);
        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getApplicationTypeChecker()).thenReturn(applicationTypeChecker);
        when(eventsChecker.getHearingUrgencyChecker()).thenReturn(hearingUrgencyChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getChildChecker()).thenReturn(childChecker);
        when(eventsChecker.getChildDetailsRevisedChecker()).thenReturn(childDetailsRevisedChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getMiamChecker()).thenReturn(miamChecker);
        when(eventsChecker.getMiamPolicyUpgradeChecker()).thenReturn(miamPolicyUpgradeChecker);
        when(eventsChecker.getAllegationsOfHarmChecker()).thenReturn(allegationsOfHarmChecker);
        when(eventsChecker.getOtherChildrenNotPartOfTheApplicationChecker()).thenReturn(otherChildrenNotPartOfTheApplicationChecker);
        when(eventsChecker.getOtherPeopleInTheCaseChecker()).thenReturn(otherPeopleInTheCaseChecker);
        when(eventsChecker.getOtherPeopleInTheCaseRevisedChecker()).thenReturn(otherPeopleInTheCaseRevisedChecker);
        when(eventsChecker.getOtherProceedingsChecker()).thenReturn(otherProceedingsChecker);
        when(eventsChecker.getAttendingTheHearingChecker()).thenReturn(attendingTheHearingChecker);
        when(eventsChecker.getInternationalElementChecker()).thenReturn(internationalElementChecker);
        when(eventsChecker.getLitigationCapacityChecker()).thenReturn(litigationCapacityChecker);
        when(eventsChecker.getWelshLanguageRequirementsChecker()).thenReturn(welshLanguageRequirementsChecker);
        when(eventsChecker.getAllegationsOfHarmRevisedChecker()).thenReturn(allegationsOfHarmRevisedChecker);

        assertTrue(submitAndPayChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenAllMandatoryCaseDataPresentThenHasMandatoryCompletedReturnTrueForV3() {
        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("testing")
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(Yes)
            .applicationPermissionRequired(noNotRequired)
            .applicationDetails("Test details")
            .isCaseUrgent(Yes)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .doYouNeedAWithoutNoticeHearing(Yes)
            .caseUrgencyTimeAndReason("reason")
            .effortsMadeWithRespondents("efforts")
            .reasonsForApplicationWithoutNotice("test")
            .setOutReasonsBelow("test")
            .consentOrder(Yes)
            .areRespondentsAwareOfProceedings(No)
            .doYouRequireAHearingWithReducedNotice(No)
            .applicants(applicantList)
            .newChildDetails(listOfChildDetailsRevised)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
            .respondents(respondentsList)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder()
                .mpuApplicantAttendedMiam(Yes)
                .mediatorRegistrationNumber("123456")
                .familyMediatorServiceName("Test Name")
                .soleTraderName("Trade Sole")
                .miamCertificationDocumentUpload(Document.builder().build())
                .build())
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                .newAllegationsOfHarmYesNo(No).build())
            .build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(caseNameChecker.isFinished(caseData)).thenReturn(true);
        when(applicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicationTypeChecker.isFinished(caseData)).thenReturn(true);
        when(hearingUrgencyChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(hearingUrgencyChecker.isFinished(caseData)).thenReturn(true);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicantsChecker.isFinished(caseData)).thenReturn(true);
        when(childChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childChecker.isFinished(caseData)).thenReturn(true);
        when(childDetailsRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childDetailsRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(childrenAndApplicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childrenAndApplicantsChecker.isFinished(caseData)).thenReturn(true);
        when(childrenAndRespondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childrenAndRespondentsChecker.isFinished(caseData)).thenReturn(true);
        when(childrenAndOtherPeopleInThisApplicationChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childrenAndOtherPeopleInThisApplicationChecker.isFinished(caseData)).thenReturn(true);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(respondentsChecker.isFinished(caseData)).thenReturn(true);
        when(miamPolicyUpgradeChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(miamPolicyUpgradeChecker.isFinished(caseData)).thenReturn(true);
        when(allegationsOfHarmRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(allegationsOfHarmRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(otherProceedingsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherProceedingsChecker.isFinished(caseData)).thenReturn(true);
        when(otherChildrenNotPartOfTheApplicationChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherChildrenNotPartOfTheApplicationChecker.isFinished(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.isFinished(caseData)).thenReturn(true);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(litigationCapacityChecker.isFinished(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.isFinished(caseData)).thenReturn(true);
        when(eventsChecker.getChildrenAndApplicantsChecker()).thenReturn(childrenAndApplicantsChecker);
        when(eventsChecker.getChildrenAndRespondentsChecker()).thenReturn(childrenAndRespondentsChecker);
        when(eventsChecker.getChildrenAndOtherPeopleInThisApplicationChecker()).thenReturn(childrenAndOtherPeopleInThisApplicationChecker);
        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getApplicationTypeChecker()).thenReturn(applicationTypeChecker);
        when(eventsChecker.getHearingUrgencyChecker()).thenReturn(hearingUrgencyChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getChildChecker()).thenReturn(childChecker);
        when(eventsChecker.getChildDetailsRevisedChecker()).thenReturn(childDetailsRevisedChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getMiamPolicyUpgradeChecker()).thenReturn(miamPolicyUpgradeChecker);
        when(eventsChecker.getAllegationsOfHarmChecker()).thenReturn(allegationsOfHarmChecker);
        when(eventsChecker.getOtherChildrenNotPartOfTheApplicationChecker()).thenReturn(otherChildrenNotPartOfTheApplicationChecker);
        when(eventsChecker.getOtherPeopleInTheCaseChecker()).thenReturn(otherPeopleInTheCaseChecker);
        when(eventsChecker.getOtherPeopleInTheCaseRevisedChecker()).thenReturn(otherPeopleInTheCaseRevisedChecker);
        when(eventsChecker.getOtherProceedingsChecker()).thenReturn(otherProceedingsChecker);
        when(eventsChecker.getAttendingTheHearingChecker()).thenReturn(attendingTheHearingChecker);
        when(eventsChecker.getInternationalElementChecker()).thenReturn(internationalElementChecker);
        when(eventsChecker.getLitigationCapacityChecker()).thenReturn(litigationCapacityChecker);
        when(eventsChecker.getWelshLanguageRequirementsChecker()).thenReturn(welshLanguageRequirementsChecker);
        when(eventsChecker.getAllegationsOfHarmRevisedChecker()).thenReturn(allegationsOfHarmRevisedChecker);

        assertTrue(submitAndPayChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenAllMandatoryCaseDataPresentThenHasMandatoryCompletedReturnTrueForV3ConsentOrderNo() {
        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("testing")
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(No)
            .applicationPermissionRequired(noNotRequired)
            .applicationDetails("Test details")
            .isCaseUrgent(Yes)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .doYouNeedAWithoutNoticeHearing(Yes)
            .caseUrgencyTimeAndReason("reason")
            .effortsMadeWithRespondents("efforts")
            .reasonsForApplicationWithoutNotice("test")
            .setOutReasonsBelow("test")
            .areRespondentsAwareOfProceedings(No)
            .doYouRequireAHearingWithReducedNotice(No)
            .applicants(applicantList)
            .newChildDetails(listOfChildDetailsRevised)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("TestString")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.dontKnow)
            .respondents(respondentsList)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder()
                .mpuApplicantAttendedMiam(Yes)
                .mediatorRegistrationNumber("123456")
                .familyMediatorServiceName("Test Name")
                .soleTraderName("Trade Sole")
                .miamCertificationDocumentUpload(Document.builder().build())
                .build())
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                .newAllegationsOfHarmYesNo(No).build())
            .build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(caseNameChecker.isFinished(caseData)).thenReturn(true);
        when(applicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicationTypeChecker.isFinished(caseData)).thenReturn(true);
        when(hearingUrgencyChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(hearingUrgencyChecker.isFinished(caseData)).thenReturn(true);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicantsChecker.isFinished(caseData)).thenReturn(true);
        when(childChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childChecker.isFinished(caseData)).thenReturn(true);
        when(childDetailsRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childDetailsRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(childrenAndApplicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childrenAndApplicantsChecker.isFinished(caseData)).thenReturn(true);
        when(childrenAndRespondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childrenAndRespondentsChecker.isFinished(caseData)).thenReturn(true);
        when(childrenAndOtherPeopleInThisApplicationChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(childrenAndOtherPeopleInThisApplicationChecker.isFinished(caseData)).thenReturn(true);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(respondentsChecker.isFinished(caseData)).thenReturn(true);
        when(miamPolicyUpgradeChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(miamPolicyUpgradeChecker.isFinished(caseData)).thenReturn(true);
        when(allegationsOfHarmRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(allegationsOfHarmRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseRevisedChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(otherPeopleInTheCaseRevisedChecker.isFinished(caseData)).thenReturn(true);
        when(otherProceedingsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherProceedingsChecker.isFinished(caseData)).thenReturn(true);
        when(otherChildrenNotPartOfTheApplicationChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(otherChildrenNotPartOfTheApplicationChecker.isFinished(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.isFinished(caseData)).thenReturn(true);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(internationalElementChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(litigationCapacityChecker.isFinished(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.isFinished(caseData)).thenReturn(true);
        when(eventsChecker.getChildrenAndApplicantsChecker()).thenReturn(childrenAndApplicantsChecker);
        when(eventsChecker.getChildrenAndRespondentsChecker()).thenReturn(childrenAndRespondentsChecker);
        when(eventsChecker.getChildrenAndOtherPeopleInThisApplicationChecker()).thenReturn(childrenAndOtherPeopleInThisApplicationChecker);
        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getApplicationTypeChecker()).thenReturn(applicationTypeChecker);
        when(eventsChecker.getHearingUrgencyChecker()).thenReturn(hearingUrgencyChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getChildChecker()).thenReturn(childChecker);
        when(eventsChecker.getChildDetailsRevisedChecker()).thenReturn(childDetailsRevisedChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getMiamPolicyUpgradeChecker()).thenReturn(miamPolicyUpgradeChecker);
        when(eventsChecker.getAllegationsOfHarmChecker()).thenReturn(allegationsOfHarmChecker);
        when(eventsChecker.getOtherChildrenNotPartOfTheApplicationChecker()).thenReturn(otherChildrenNotPartOfTheApplicationChecker);
        when(eventsChecker.getOtherPeopleInTheCaseChecker()).thenReturn(otherPeopleInTheCaseChecker);
        when(eventsChecker.getOtherPeopleInTheCaseRevisedChecker()).thenReturn(otherPeopleInTheCaseRevisedChecker);
        when(eventsChecker.getOtherProceedingsChecker()).thenReturn(otherProceedingsChecker);
        when(eventsChecker.getAttendingTheHearingChecker()).thenReturn(attendingTheHearingChecker);
        when(eventsChecker.getInternationalElementChecker()).thenReturn(internationalElementChecker);
        when(eventsChecker.getLitigationCapacityChecker()).thenReturn(litigationCapacityChecker);
        when(eventsChecker.getWelshLanguageRequirementsChecker()).thenReturn(welshLanguageRequirementsChecker);
        when(eventsChecker.getAllegationsOfHarmRevisedChecker()).thenReturn(allegationsOfHarmRevisedChecker);

        assertTrue(submitAndPayChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(submitAndPayChecker.getDefaultTaskState(CaseData.builder().build()));
    }


}
