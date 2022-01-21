package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CaseWorkerEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseWorkerEmailServiceTest {

    private static final String manageCaseUrl = null;
    public static final String EMAIL_TEMPLATE_ID_1 = "111";
    public static final String EMAIL_TEMPLATE_ID_2 = "222";
    private static final String URGENT_CASE = "Urgent Case";
    private static final String WITHOUT_NOTICE = "Without Notice";
    private static final String STANDARAD_HEARING = "Standard Hearing";

    public static final CaseWorkerEmail expectedEmailVars =  CaseWorkerEmail.builder()
        .caseReference("123")
        .caseName("Case 123")
        .applicantName("applicantName")
        .respondentLastName("respondentName")
        .hearingDateRequested("  ")
        .ordersApplyingFor("CA Order")
        .typeOfHearing("Urgent")
        .courtEmail("C@justice.gov.uk")
        .caseLink("http://localhost:3333/")
        .build();


    @Mock
    private EmailService emailService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserService userService;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    private String applicantNames;
    private String respondentLastName;
    private List<Element<PartyDetails>> listOfApplicants;
    private List<Element<PartyDetails>> listOfRespondents;
    private EmailTemplateVars emailTemplateVars;
    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        listOfApplicants = Collections.singletonList(wrappedApplicants);

        PartyDetails respondent = PartyDetails.builder()
            .lastName("respondentLast")
            .build();

        respondentLastName = "TestLast";

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        listOfRespondents = Collections.singletonList(wrappedRespondents);

        caseDetails = CaseDetails.builder()
            .id(12345L)
            .build();


    }

    @Test
    public void shouldBuildEmailWithApplicantDetails() {

        CaseData caseData = CaseData.builder()
            .id(caseDetails.getId())
            .applicantCaseName("testCase")
            .applicants(listOfApplicants)
            .build();

        emailTemplateVars = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantNames)
            .build();

        when(caseWorkerEmailService.buildEmail(caseDetails)).thenReturn(emailTemplateVars);

        verifyNoInteractions(caseWorkerEmailService);
    }

    @Test
    public void shouldBuildEmailWithRespondentDetails() {

        CaseData caseData = CaseData.builder()
            .id(caseDetails.getId())
            .applicantCaseName("testName")
            .respondents(listOfRespondents)
            .build();

        emailTemplateVars = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .respondentLastName(respondentLastName)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        when(caseWorkerEmailService.buildEmail(caseDetails)).thenReturn(emailTemplateVars);

        verifyNoInteractions(caseWorkerEmailService);
    }

    @Test
    public void shouldBuildEmailWithOrdersApplyAsChildArrangementOrder() {

        List<OrderTypeEnum> typeOfOrder = new ArrayList<>();
        typeOfOrder.add(OrderTypeEnum.childArrangementsOrder);

        CaseData caseData = CaseData.builder()
            .id(caseDetails.getId())
            .applicantCaseName("testName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(typeOfOrder)
            .build();

        emailTemplateVars = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName(respondentLastName)
            .ordersApplyingFor(OrderTypeEnum.childArrangementsOrder.getDisplayedValue())
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        when(caseWorkerEmailService.buildEmail(caseDetails)).thenReturn(emailTemplateVars);
        verifyNoInteractions(caseWorkerEmailService);
    }

    @Test
    public void shouldBuildEmailWithTypeOfHearingAsUrgent() {

        List<OrderTypeEnum> typeOfOrder = new ArrayList<>();
        typeOfOrder.add(OrderTypeEnum.childArrangementsOrder);
        typeOfOrder.add(OrderTypeEnum.prohibitedStepsOrder);

        CaseData caseData = CaseData.builder()
            .id(caseDetails.getId())
            .applicantCaseName("testName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(typeOfOrder)
            .isCaseUrgent(YesOrNo.YES)
            .doYouNeedAWithoutNoticeHearing(YesOrNo.NO)
            .doYouRequireAHearingWithReducedNotice(YesOrNo.NO)
            .build();

        emailTemplateVars = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName(respondentLastName)
            .ordersApplyingFor(OrderTypeEnum.childArrangementsOrder.getDisplayedValue())
            .typeOfHearing("Urgent ")
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        when(caseWorkerEmailService.buildEmail(caseDetails)).thenReturn(emailTemplateVars);

        verifyNoInteractions(caseWorkerEmailService);
    }

    @Test
    public void shouldBuildEmailWithTypeOfHearingAsWithOutNotice() {

        List<OrderTypeEnum> typeOfOrder = new ArrayList<>();
        typeOfOrder.add(OrderTypeEnum.childArrangementsOrder);
        typeOfOrder.add(OrderTypeEnum.prohibitedStepsOrder);

        CaseData caseData = CaseData.builder()
            .id(caseDetails.getId())
            .applicantCaseName("testName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(typeOfOrder)
            .isCaseUrgent(YesOrNo.NO)
            .doYouNeedAWithoutNoticeHearing(YesOrNo.YES)
            .doYouRequireAHearingWithReducedNotice(YesOrNo.NO)
            .build();

        emailTemplateVars = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName(respondentLastName)
            .ordersApplyingFor(OrderTypeEnum.childArrangementsOrder.getDisplayedValue()
                                   + ", " + OrderTypeEnum.prohibitedStepsOrder.getDisplayedValue())
            .typeOfHearing("Without notice")
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        when(caseWorkerEmailService.buildEmail(caseDetails)).thenReturn(emailTemplateVars);

    }

    @Test
    public void shouldBuildEmailWithTypeOfHearingAsReducedNotice() {

        List<OrderTypeEnum> typeOfOrder = new ArrayList<>();
        typeOfOrder.add(OrderTypeEnum.specificIssueOrder);
        typeOfOrder.add(OrderTypeEnum.prohibitedStepsOrder);

        CaseData caseData = CaseData.builder()
            .id(caseDetails.getId())
            .applicantCaseName("testName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(typeOfOrder)
            .isCaseUrgent(YesOrNo.NO)
            .doYouNeedAWithoutNoticeHearing(YesOrNo.NO)
            .doYouRequireAHearingWithReducedNotice(YesOrNo.YES)
            .build();

        emailTemplateVars = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName(respondentLastName)
            .ordersApplyingFor(OrderTypeEnum.specificIssueOrder.getDisplayedValue()
                                   + ", " + OrderTypeEnum.prohibitedStepsOrder.getDisplayedValue())
            .typeOfHearing("Reduced notice")
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        when(caseWorkerEmailService.buildEmail(caseDetails)).thenReturn(emailTemplateVars);

        verifyNoInteractions(caseWorkerEmailService);
    }

    @Test
    public void shouldBuildEmailWithTypeOfHearingAsStandardHearing() {


        List<OrderTypeEnum> typeOfOrder = new ArrayList<>();
        typeOfOrder.add(OrderTypeEnum.childArrangementsOrder);
        typeOfOrder.add(OrderTypeEnum.prohibitedStepsOrder);

        CaseData caseData = CaseData.builder()
            .id(caseDetails.getId())
            .applicantCaseName("testName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(typeOfOrder)
            .isCaseUrgent(YesOrNo.NO)
            .doYouNeedAWithoutNoticeHearing(YesOrNo.NO)
            .doYouRequireAHearingWithReducedNotice(YesOrNo.NO)
            .build();

        emailTemplateVars = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName(respondentLastName)
            .ordersApplyingFor(OrderTypeEnum.childArrangementsOrder.getDisplayedValue()
                                   + ", " + OrderTypeEnum.prohibitedStepsOrder.getDisplayedValue())
            .typeOfHearing("Standard hearing")
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        when(caseWorkerEmailService.buildEmail(caseDetails)).thenReturn(emailTemplateVars);

        verifyNoInteractions(caseWorkerEmailService);
    }

    @Test
    public void testGetRecipientDetails() {

        String expected = "test@email.com";

        when(userDetails.getEmail()).thenReturn("test@email.com");

        when(caseWorkerEmailService.getRecipientEmail(userDetails)).thenReturn(expected);
    }


}
