package uk.gov.hmcts.reform.prl.services.solicitornotifications;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EmailService;

import java.util.List;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorNotificationEmailServiceTest {


    @Mock
    private EmailService emailService;

    @InjectMocks
    SolicitorNotificationEmailService solicitorNotificationEmailService;

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
        Mockito.when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        solicitorNotificationEmailService.sendC100ApplicantSolicitorNotification(caseDetails);

        Mockito.verify(emailService,Mockito.times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(),Mockito.any());
    }

    @Test
    public void verifyEmailNotificationTriggeredForRespondentSolicitor() throws  Exception {
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
        Mockito.when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        solicitorNotificationEmailService.sendC100RespondentSolicitorNotification(caseDetails);

        Mockito.verify(emailService,Mockito.times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(),Mockito.any());
    }

    @Test
    public void verifyEmailNotificationTriggeredForRespondent() throws Exception {
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
                                             .email("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))
            .build();


        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        solicitorNotificationEmailService.sendC100RespondentNotification(caseDetails);

        Mockito.verify(emailService,Mockito.times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(),Mockito.any());
    }
}
