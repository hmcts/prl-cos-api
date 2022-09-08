package uk.gov.hmcts.reform.prl.services.solicitornotifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class SolicitorNotificationServiceImplTest {

    @InjectMocks
    private SolicitorNotificationServiceImpl solicitorNotificationServiceImpl;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SolicitorNotificationEmailService solicitorNotificationEmailService;

    @Test
    public void verifyEmailNotificationTriggeredForApplicantSolicitor() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(State.CASE_ISSUE.getValue())
            .data(casedata)
            .build();
        solicitorNotificationServiceImpl.generateAndSendNotificationToApplicantSolicitor(caseDetails);

        Mockito.verify(solicitorNotificationEmailService,times(1))
            .sendC100ApplicantSolicitorNotification(caseDetails);
    }


    @Test
    public void verifyEmailNotificationTriggeredForRespondentSolicitor() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(State.ALL_FINAL_ORDERS_ISSUED.getValue())
            .data(casedata)
            .build();
        solicitorNotificationServiceImpl.generateAndSendNotificationToRespondentSolicitor(caseDetails);

        Mockito.verify(solicitorNotificationEmailService,times(1))
            .sendC100RespondentSolicitorNotification(caseDetails);
    }

    @Test
    public void verifyEmailNotificationTriggeredForRespondent() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(State.ALL_FINAL_ORDERS_ISSUED.getValue())
            .data(casedata)
            .build();
        solicitorNotificationServiceImpl.generateAndSendNotificationToRespondent(caseDetails);

        Mockito.verify(solicitorNotificationEmailService,times(1))
            .sendC100RespondentNotification(caseDetails);
    }

    @Test
    public void verifyNotificationNotTriggeredForRespSolicitorWhenFinalOrderNotIssued() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(State.CASE_ISSUE.getValue())
            .data(casedata)
            .build();
        solicitorNotificationServiceImpl.generateAndSendNotificationToRespondentSolicitor(caseDetails);

        Mockito.verify(solicitorNotificationEmailService,times(0))
            .sendC100RespondentSolicitorNotification(caseDetails);

    }

    @Test
    public void verifyNotificationNotTriggeredForAppSolicitorWhenCaseIsWithdrawn() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(State.CASE_WITHDRAWN.getValue())
            .data(casedata)
            .build();
        solicitorNotificationServiceImpl.generateAndSendNotificationToApplicantSolicitor(caseDetails);

        Mockito.verify(solicitorNotificationEmailService,times(0))
            .sendC100ApplicantSolicitorNotification(caseDetails);

    }

    @Test
    public void verifyNotificationNotTriggeredForRespondentWhenFinalOrderNotIssued() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(State.CASE_ISSUE.getValue())
            .data(casedata)
            .build();
        solicitorNotificationServiceImpl.generateAndSendNotificationToRespondent(caseDetails);

        Mockito.verify(solicitorNotificationEmailService,times(0))
            .sendC100RespondentNotification(caseDetails);

    }
}
