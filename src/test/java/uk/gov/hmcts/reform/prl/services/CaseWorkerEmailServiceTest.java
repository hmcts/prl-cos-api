package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.CaseWorkerEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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
    private IdamClient idamClient;

    @InjectMocks
    private UserService userService;

    @InjectMocks
    private CaseWorkerEmailService caseWorkerEmailService;

    private Map<String, String> expectedEmailVarsAsMap;

    @Test
    public void whenUserDetailsProvidedThenValidEmailReturned() {

        when(userService.getUserDetails("Auth")).thenReturn(UserDetails.builder()
                                                                .email("test@email.com")
                                                                .build());

        UserDetails userDetails = userService.getUserDetails("Auth");

        assertEquals(caseWorkerEmailService.getRecipientEmail(userDetails), "test@email.com");

    }


    @Test
    public void whenApplicantPresentThenApplicantStringCreated() {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        PartyDetails respondent = PartyDetails.builder()
            .lastName("TestLast")
            .build();

        String respondentNames = "TestLast";

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(Collections.singletonList(OrderTypeEnum.childArrangementsOrder))
            .isCaseUrgent(YesOrNo.YES)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(String.valueOf(caseData.getId()))
            .caseData(caseData)
            .build();

        UserDetails userDetails = UserDetails.builder()
            .build();

        String caseUrgency = null;

        if (caseDetails.getCaseData().getIsCaseUrgent().equals(YesOrNo.YES)) {
            caseUrgency = URGENT_CASE;
        } else if (caseDetails.getCaseData().getDoYouNeedAWithoutNoticeHearing().equals(YesOrNo.YES)) {
            caseUrgency = WITHOUT_NOTICE;
        } else if ((caseDetails.getCaseData().getIsCaseUrgent().equals(YesOrNo.NO))
            && (caseDetails.getCaseData().getDoYouNeedAWithoutNoticeHearing().equals(YesOrNo.YES))
            && (caseDetails.getCaseData().getDoYouRequireAHearingWithReducedNotice().equals(YesOrNo.NO))) {
            caseUrgency = STANDARAD_HEARING;
        }

        final String[] typeOfOrder = new String[1];

        caseDetails.getCaseData().getOrdersApplyingFor().forEach(orderType -> {
            if (orderType.equals(OrderTypeEnum.childArrangementsOrder)) {
                typeOfOrder[0] = OrderTypeEnum.childArrangementsOrder.getDisplayedValue();
            } else if (orderType.equals(OrderTypeEnum.prohibitedStepsOrder)) {
                typeOfOrder[0] = OrderTypeEnum.prohibitedStepsOrder.getDisplayedValue();
            } else {
                typeOfOrder[0] = OrderTypeEnum.specificIssueOrder.getDisplayedValue();
            }
        });

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(caseDetails.getCaseId())
            .caseName(caseDetails.getCaseData().getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName("TestLast")
            .typeOfHearing("Urgent Case")
            .hearingDateRequested("  ")
            .ordersApplyingFor("Child Arrangements Order")
            .caseLink(manageCaseUrl + "/" + caseDetails.getCaseId())
            .build();

        assertEquals(caseWorkerEmailService.buildEmail(caseDetails, userDetails), email);

    }

    @Test
    public void whenRespondentPresentThenRespondentStringCreated() {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        PartyDetails respondent = PartyDetails.builder()
            .lastName("respondentLast")
            .build();

        String respondentNames = "TestLast";

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(Collections.singletonList(OrderTypeEnum.childArrangementsOrder))
            .isCaseUrgent(YesOrNo.YES)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(String.valueOf(caseData.getId()))
            .caseData(caseData)
            .build();

        UserDetails userDetails = UserDetails.builder()
            .build();

        String caseUrgency = null;

        if (caseDetails.getCaseData().getIsCaseUrgent().equals(YesOrNo.YES)) {
            caseUrgency = URGENT_CASE;
        } else if (caseDetails.getCaseData().getDoYouNeedAWithoutNoticeHearing().equals(YesOrNo.YES)) {
            caseUrgency = WITHOUT_NOTICE;
        } else if ((caseDetails.getCaseData().getIsCaseUrgent().equals(YesOrNo.NO))
            && (caseDetails.getCaseData().getDoYouNeedAWithoutNoticeHearing().equals(YesOrNo.YES))
            && (caseDetails.getCaseData().getDoYouRequireAHearingWithReducedNotice().equals(YesOrNo.NO))) {
            caseUrgency = STANDARAD_HEARING;
        }

        final String[] typeOfOrder = new String[1];

        caseDetails.getCaseData().getOrdersApplyingFor().forEach(orderType -> {
            if (orderType.equals(OrderTypeEnum.childArrangementsOrder)) {
                typeOfOrder[0] = OrderTypeEnum.childArrangementsOrder.getDisplayedValue();
            } else if (orderType.equals(OrderTypeEnum.prohibitedStepsOrder)) {
                typeOfOrder[0] = OrderTypeEnum.prohibitedStepsOrder.getDisplayedValue();
            } else {
                typeOfOrder[0] = OrderTypeEnum.specificIssueOrder.getDisplayedValue();
            }
        });

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(caseDetails.getCaseId())
            .caseName(caseDetails.getCaseData().getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName("respondentLast")
            .typeOfHearing("Urgent Case")
            .hearingDateRequested("  ")
            .ordersApplyingFor("Child Arrangements Order")
            .caseLink(manageCaseUrl + "/" + caseDetails.getCaseId())
            .build();

        assertEquals(caseWorkerEmailService.buildEmail(caseDetails, userDetails), email);

    }

    @Test
    public void whenTypeOfApplicationPresentThenOrdersApplyForWillBeDispalyed() {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        PartyDetails respondent = PartyDetails.builder()
            .lastName("respondentLast")
            .build();

        String respondentNames = "TestLast";

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(Collections.singletonList(OrderTypeEnum.specificIssueOrder))
            .isCaseUrgent(YesOrNo.NO)
            .doYouNeedAWithoutNoticeHearing(YesOrNo.YES)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(String.valueOf(caseData.getId()))
            .caseData(caseData)
            .build();

        UserDetails userDetails = UserDetails.builder()
            .build();

        String caseUrgency = null;

        if (caseDetails.getCaseData().getIsCaseUrgent().equals(YesOrNo.YES)) {
            caseUrgency = URGENT_CASE;
        } else if (caseDetails.getCaseData().getDoYouNeedAWithoutNoticeHearing().equals(YesOrNo.YES)) {
            caseUrgency = WITHOUT_NOTICE;
        } else if ((caseDetails.getCaseData().getIsCaseUrgent().equals(YesOrNo.NO))
            && (caseDetails.getCaseData().getDoYouNeedAWithoutNoticeHearing().equals(YesOrNo.YES))
            && (caseDetails.getCaseData().getDoYouRequireAHearingWithReducedNotice().equals(YesOrNo.NO))) {
            caseUrgency = STANDARAD_HEARING;
        }

        final String[] typeOfOrder = new String[1];

        caseDetails.getCaseData().getOrdersApplyingFor().forEach(orderType -> {
            if (orderType.equals(OrderTypeEnum.childArrangementsOrder)) {
                typeOfOrder[0] = OrderTypeEnum.childArrangementsOrder.getDisplayedValue();
            } else if (orderType.equals(OrderTypeEnum.prohibitedStepsOrder)) {
                typeOfOrder[0] = OrderTypeEnum.prohibitedStepsOrder.getDisplayedValue();
            } else {
                typeOfOrder[0] = OrderTypeEnum.specificIssueOrder.getDisplayedValue();
            }
        });

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(caseDetails.getCaseId())
            .caseName(caseDetails.getCaseData().getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName("respondentLast")
            .typeOfHearing("Without Notice")
            .hearingDateRequested("  ")
            .ordersApplyingFor("Specific Issue Order")
            .caseLink(manageCaseUrl + "/" + caseDetails.getCaseId())
            .build();

        assertEquals(caseWorkerEmailService.buildEmail(caseDetails, userDetails), email);
    }

    @Test
    public void testGetRecipientDetails() {

        UserDetails userDetails = UserDetails.builder()
            .email("test@email.com")
            .build();

        String expected = "test@email.com";

        assertEquals(caseWorkerEmailService.getRecipientEmail(userDetails), expected);
    }
}
