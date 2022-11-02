package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EmailService;

import java.util.List;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class CitizenResponseNotificationEmailServiceTest {


    @Mock
    private EmailService emailService;

    @InjectMocks
    CitizenResponseNotificationEmailService solicitorNotificationEmailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void verifyEmailNotificationTriggeredForApplicantSolicitor() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                                            .solicitorEmail("test@gmail.com")
                                            .representativeLastName("LastName")
                                            .representativeFirstName("FirstName")
                                            .build())))
            .respondents(List.of(element(PartyDetails.builder()
                                             .solicitorEmail("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))
            .build();


        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(objectMapper.convertValue(Mockito.any((CaseDetails.class)), CaseData.class)).thenReturn(caseData);
        solicitorNotificationEmailService.sendC100ApplicantSolicitorNotification(caseDetails);

        Mockito.verify(emailService,Mockito.times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(),Mockito.any());
    }

    @Test
    public void verifyNoEmailNotificationTriggeredForApplicantSolicitor() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .state(State.CASE_WITHDRAWN)
            .applicants(List.of(element(PartyDetails.builder()
                                            .solicitorEmail("test@gmail.com")
                                            .representativeLastName("LastName")
                                            .representativeFirstName("FirstName")
                                            .build())))
            .respondents(List.of(element(PartyDetails.builder()
                                             .solicitorEmail("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))
            .build();


        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(objectMapper.convertValue(Mockito.any((CaseDetails.class)), CaseData.class)).thenReturn(caseData);
        solicitorNotificationEmailService.sendC100ApplicantSolicitorNotification(caseDetails);

        Mockito.verify(emailService,Mockito.times(0)).send(Mockito.anyString(),
                                                           Mockito.any(),
                                                           Mockito.any(),Mockito.any());
    }
}
