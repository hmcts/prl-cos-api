package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDetailsOfWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ReasonForWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBailConditionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FL401StatementOfTruthAndSubmitCheckerTest {

    @InjectMocks
    FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

    @Mock
    EventsChecker eventsChecker;

    @Mock
    CaseNameChecker caseNameChecker;

    @Mock
    FL401ApplicationTypeChecker fl401ApplicationTypeChecker;

    @Mock
    WithoutNoticeOrderChecker withoutNoticeOrderChecker;

    @Mock
    ApplicantsChecker applicantsChecker;

    @Mock
    RespondentsChecker respondentsChecker;

    @Mock
    FL401ApplicantFamilyChecker fl401ApplicantFamilyChecker;

    @Mock
    RespondentRelationshipChecker respondentRelationshipChecker;

    @Mock
    RespondentBehaviourChecker respondentBehaviourChecker;

    @Mock
    FL401OtherProceedingsChecker fl401OtherProceedingsChecker;

    @Mock
    AttendingTheHearingChecker attendingTheHearingChecker;

    @Mock
    WelshLanguageRequirementsChecker welshLanguageRequirementsChecker;

    private CaseData caseData;

    private TypeOfApplicationOrders orders;
    private LinkToCA linkToCA;
    private WithoutNoticeOrderDetails withoutNoticeOrderDetails;
    private ReasonForWithoutNoticeOrder reasonForWithoutNoticeOrder;
    private RespondentBailConditionDetails respondentBailConditionDetails;
    private OtherDetailsOfWithoutNoticeOrder otherDetailsOfWithoutNoticeOrder;
    private PartyDetails applicant;
    private PartyDetails respondent;
    private ApplicantFamilyDetails applicantFamilyDetails;
    private ApplicantChild applicantChild;
    private RespondentRelationObjectType respondentRelationObjectType;
    private RespondentRelationOptionsInfo respondentRelationOptionsInfo;
    private RespondentBehaviour respondentBehaviour;

    @Before
    public void setUp() {

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();
        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);

        //Type of application
        orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        //without notice order
        withoutNoticeOrderDetails = WithoutNoticeOrderDetails.builder().orderWithoutGivingNotice(YesOrNo.Yes).build();
        reasonForWithoutNoticeOrder = ReasonForWithoutNoticeOrder.builder().reasonForOrderWithoutGivingNotice(
            Arrays.asList(ReasonForOrderWithoutGivingNoticeEnum.harmToApplicantOrChild)).build();
        respondentBailConditionDetails = RespondentBailConditionDetails.builder().isRespondentAlreadyInBailCondition(
            YesNoDontKnow.dontKnow).build();
        otherDetailsOfWithoutNoticeOrder = OtherDetailsOfWithoutNoticeOrder.builder().otherDetails("test").build();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date = "12-09-1999";

        //convert String to LocalDate
        LocalDate localDate = LocalDate.parse(date, formatter);

        //applicant details
        applicant = PartyDetails.builder()
            .firstName("fl401applicant")
            .lastName("fl401applicantlast")
            .dateOfBirth(localDate)
            .gender(Gender.female)
            .address(Address.builder()
                         .addressLine1("Test")
                         .addressLine2("Test")
                         .addressLine3("Test")
                         .county("London")
                         .country("UK")
                         .postTown("Southgate")
                         .postCode("N14 5EF")
                         .build())
            .isAddressConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("testapplicant@demo.com")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .representativeFirstName("applicantSolicitor")
            .representativeLastName("test")
            .solicitorEmail("testemail@solicitor.com")
            .solicitorTelephone("1234567890")
            .solicitorReference("testref123")
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .build();

        //respondent details
        respondent = PartyDetails.builder()
            .firstName("Fl401Respondent")
            .lastName("Fl401RespondentLast")
            .isDateOfBirthKnown(YesOrNo.No)
            .respondentLivedWithApplicant(YesOrNo.No)
            .isCurrentAddressKnown(YesOrNo.No)
            .canYouProvideEmailAddress(YesOrNo.No)
            .canYouProvidePhoneNumber(YesOrNo.No)
            .build();

        //applicants family
        applicantFamilyDetails = ApplicantFamilyDetails.builder()
            .doesApplicantHaveChildren(No)
            .build();

        //Relationshiptorespondent

        respondentRelationObjectType = RespondentRelationObjectType.builder()
            .applicantRelationship(ApplicantRelationshipEnum.noneOfTheAbove)
            .build();
        respondentRelationOptionsInfo = RespondentRelationOptionsInfo.builder()
            .applicantRelationshipOptions(ApplicantRelationshipOptionsEnum.aunt)
            .build();

        //Respondentbehaviour
        respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(applicantStopFromRespondentDoingToChildEnum_Value_1))
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();

    }

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {
        caseData = CaseData.builder().build();
        assertFalse(fl401StatementOfTruthAndSubmitChecker.isStarted(caseData));
    }

    @Test
    public void whenPartialCaseDataPresentIsFinishedReturnsFalse() {
        caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(caseNameChecker.isFinished(caseData)).thenReturn(false);
        when(fl401ApplicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(fl401ApplicationTypeChecker.isFinished(caseData)).thenReturn(false);
        when(withoutNoticeOrderChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(withoutNoticeOrderChecker.isFinished(caseData)).thenReturn(false);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(applicantsChecker.isFinished(caseData)).thenReturn(false);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(respondentsChecker.isFinished(caseData)).thenReturn(false);
        when(fl401ApplicantFamilyChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(fl401ApplicantFamilyChecker.isFinished(caseData)).thenReturn(false);
        when(respondentRelationshipChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(respondentRelationshipChecker.isFinished(caseData)).thenReturn(false);
        when(respondentBehaviourChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(respondentBehaviourChecker.isFinished(caseData)).thenReturn(false);

        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getFl401ApplicationTypeChecker()).thenReturn(fl401ApplicationTypeChecker);
        when(eventsChecker.getWithoutNoticeOrderChecker()).thenReturn(withoutNoticeOrderChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getFl401ApplicantFamilyChecker()).thenReturn(fl401ApplicantFamilyChecker);
        when(eventsChecker.getRespondentRelationshipChecker()).thenReturn(respondentRelationshipChecker);
        when(eventsChecker.getRespondentBehaviourChecker()).thenReturn(respondentBehaviourChecker);

        assertFalse(fl401StatementOfTruthAndSubmitChecker.isFinished(caseData));
    }

    @Test
    public void whenAllDetailsProvidedIsFinishedReturnsTrue() {

        caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .orderWithoutGivingNoticeToRespondent(withoutNoticeOrderDetails)
            .reasonForOrderWithoutGivingNotice(reasonForWithoutNoticeOrder)
            .bailDetails(respondentBailConditionDetails)
            .anyOtherDtailsForWithoutNoticeOrder(otherDetailsOfWithoutNoticeOrder)
            .applicantsFL401(applicant)
            .respondentsFL401(respondent)
            .applicantFamilyDetails(applicantFamilyDetails)
            .respondentRelationObject(respondentRelationObjectType)
            .respondentRelationOptions(respondentRelationOptionsInfo)
            .respondentBehaviourData(respondentBehaviour)
            .build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(caseNameChecker.isFinished(caseData)).thenReturn(true);
        when(fl401ApplicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(fl401ApplicationTypeChecker.isFinished(caseData)).thenReturn(true);
        when(withoutNoticeOrderChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(withoutNoticeOrderChecker.isFinished(caseData)).thenReturn(true);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicantsChecker.isFinished(caseData)).thenReturn(true);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(respondentsChecker.isFinished(caseData)).thenReturn(true);
        when(fl401ApplicantFamilyChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(fl401ApplicantFamilyChecker.isFinished(caseData)).thenReturn(true);
        when(respondentRelationshipChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(respondentRelationshipChecker.isFinished(caseData)).thenReturn(true);
        when(respondentBehaviourChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(respondentBehaviourChecker.isFinished(caseData)).thenReturn(true);
        when(fl401OtherProceedingsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(fl401OtherProceedingsChecker.isFinished(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.isFinished(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.isFinished(caseData)).thenReturn(true);

        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getFl401ApplicationTypeChecker()).thenReturn(fl401ApplicationTypeChecker);
        when(eventsChecker.getWithoutNoticeOrderChecker()).thenReturn(withoutNoticeOrderChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getFl401ApplicantFamilyChecker()).thenReturn(fl401ApplicantFamilyChecker);
        when(eventsChecker.getRespondentRelationshipChecker()).thenReturn(respondentRelationshipChecker);
        when(eventsChecker.getRespondentBehaviourChecker()).thenReturn(respondentBehaviourChecker);
        when(eventsChecker.getFl401OtherProceedingsChecker()).thenReturn(fl401OtherProceedingsChecker);
        when(eventsChecker.getAttendingTheHearingChecker()).thenReturn(attendingTheHearingChecker);
        when(eventsChecker.getWelshLanguageRequirementsChecker()).thenReturn(welshLanguageRequirementsChecker);

        assertTrue(fl401StatementOfTruthAndSubmitChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataPresentHasMandatoryCompletedReturnsFalse() {
        caseData = CaseData.builder().build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(caseNameChecker.isFinished(caseData)).thenReturn(false);
        when(fl401ApplicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(fl401ApplicationTypeChecker.isFinished(caseData)).thenReturn(false);
        when(withoutNoticeOrderChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(withoutNoticeOrderChecker.isFinished(caseData)).thenReturn(false);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(applicantsChecker.isFinished(caseData)).thenReturn(false);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(respondentsChecker.isFinished(caseData)).thenReturn(false);
        when(fl401ApplicantFamilyChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(fl401ApplicantFamilyChecker.isFinished(caseData)).thenReturn(false);
        when(respondentRelationshipChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(respondentRelationshipChecker.isFinished(caseData)).thenReturn(false);
        when(respondentBehaviourChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        when(respondentBehaviourChecker.isFinished(caseData)).thenReturn(false);

        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getFl401ApplicationTypeChecker()).thenReturn(fl401ApplicationTypeChecker);
        when(eventsChecker.getWithoutNoticeOrderChecker()).thenReturn(withoutNoticeOrderChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getFl401ApplicantFamilyChecker()).thenReturn(fl401ApplicantFamilyChecker);
        when(eventsChecker.getRespondentRelationshipChecker()).thenReturn(respondentRelationshipChecker);
        when(eventsChecker.getRespondentBehaviourChecker()).thenReturn(respondentBehaviourChecker);

        assertFalse(fl401StatementOfTruthAndSubmitChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenMandatoryCaseDataPresentHasMandatoryCompletedReturnsTrue() {
        caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .orderWithoutGivingNoticeToRespondent(withoutNoticeOrderDetails)
            .reasonForOrderWithoutGivingNotice(reasonForWithoutNoticeOrder)
            .bailDetails(respondentBailConditionDetails)
            .anyOtherDtailsForWithoutNoticeOrder(otherDetailsOfWithoutNoticeOrder)
            .applicantsFL401(applicant)
            .respondentsFL401(respondent)
            .applicantFamilyDetails(applicantFamilyDetails)
            .respondentRelationObject(respondentRelationObjectType)
            .respondentRelationOptions(respondentRelationOptionsInfo)
            .respondentBehaviourData(respondentBehaviour)
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.no)
            .attendHearing(AttendHearing.builder()
                               .isWelshNeeded(No)
                               .isInterpreterNeeded(No)
                               .isDisabilityPresent(No)
                               .isSpecialArrangementsRequired(No)
                               .isIntermediaryNeeded(No)
                               .build())
            .welshLanguageRequirement(No)
            .build();

        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(caseNameChecker.isFinished(caseData)).thenReturn(true);
        when(fl401ApplicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(fl401ApplicationTypeChecker.isFinished(caseData)).thenReturn(true);
        when(withoutNoticeOrderChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(withoutNoticeOrderChecker.isFinished(caseData)).thenReturn(true);
        when(applicantsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(applicantsChecker.isFinished(caseData)).thenReturn(true);
        when(respondentsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(respondentsChecker.isFinished(caseData)).thenReturn(true);
        when(fl401ApplicantFamilyChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(fl401ApplicantFamilyChecker.isFinished(caseData)).thenReturn(true);
        when(respondentRelationshipChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(respondentRelationshipChecker.isFinished(caseData)).thenReturn(true);
        when(respondentBehaviourChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(respondentBehaviourChecker.isFinished(caseData)).thenReturn(true);
        when(fl401OtherProceedingsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(fl401OtherProceedingsChecker.isFinished(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(attendingTheHearingChecker.isFinished(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(welshLanguageRequirementsChecker.isFinished(caseData)).thenReturn(true);

        when(eventsChecker.getCaseNameChecker()).thenReturn(caseNameChecker);
        when(eventsChecker.getFl401ApplicationTypeChecker()).thenReturn(fl401ApplicationTypeChecker);
        when(eventsChecker.getWithoutNoticeOrderChecker()).thenReturn(withoutNoticeOrderChecker);
        when(eventsChecker.getApplicantsChecker()).thenReturn(applicantsChecker);
        when(eventsChecker.getRespondentsChecker()).thenReturn(respondentsChecker);
        when(eventsChecker.getFl401ApplicantFamilyChecker()).thenReturn(fl401ApplicantFamilyChecker);
        when(eventsChecker.getRespondentRelationshipChecker()).thenReturn(respondentRelationshipChecker);
        when(eventsChecker.getRespondentBehaviourChecker()).thenReturn(respondentBehaviourChecker);
        when(eventsChecker.getFl401OtherProceedingsChecker()).thenReturn(fl401OtherProceedingsChecker);
        when(eventsChecker.getAttendingTheHearingChecker()).thenReturn(attendingTheHearingChecker);
        when(eventsChecker.getWelshLanguageRequirementsChecker()).thenReturn(welshLanguageRequirementsChecker);

        assertTrue(fl401StatementOfTruthAndSubmitChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(fl401StatementOfTruthAndSubmitChecker.getDefaultTaskState(caseData));
    }

}
