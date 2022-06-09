package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfApplicationEmailServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private Time dateTime;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void testC100EmailNotification() throws Exception {
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
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        serviceOfApplicationEmailService.sendEmailC100(caseDetails);
        verify(emailService,times(2)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(),Mockito.any());
    }

    @Test
    public void testFl401EmailNotification() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantsFL401(PartyDetails.builder()
                                 .solicitorEmail("test@gmail.com")
                                 .representativeLastName("LastName")
                                 .representativeFirstName("FirstName")
                                 .build())
            .respondentsFL401(PartyDetails.builder()
                                  .solicitorEmail("test@gmail.com")
                                  .representativeLastName("LastName")
                                  .representativeFirstName("FirstName")
                                  .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                  .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        serviceOfApplicationEmailService.sendEmailFL401(caseDetails);
        verify(emailService,times(2)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(),Mockito.any());
    }

    @Test
    public void testFl401EmailNotificationWithoutRespondentSolicitor() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantsFL401(PartyDetails.builder()
                                 .solicitorEmail("test@gmail.com")
                                 .representativeLastName("LastName")
                                 .representativeFirstName("FirstName")
                                 .build())
            .respondentsFL401(PartyDetails.builder()
                                  .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        serviceOfApplicationEmailService.sendEmailFL401(caseDetails);
        verify(emailService,times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(),Mockito.any());
    }
}
