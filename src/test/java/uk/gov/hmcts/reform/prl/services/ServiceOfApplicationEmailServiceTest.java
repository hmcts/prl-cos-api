package uk.gov.hmcts.reform.prl.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfApplicationEmailServiceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Mock
    SendgridService sendgridService;

    @Test
    public void testCafcassEmail() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .build();

        serviceOfApplicationEmailService.sendEmailNotificationToCafcass(caseData, "email", "cafcass");

        verify(emailService, times(1)).sendSoa(Mockito.anyString(),
                                               Mockito.any(),
                                               Mockito.any(), Mockito.any()
        );
    }

    @Test
    public void testLocalAuthorityEmail() throws IOException {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .build();
        when(sendgridService.sendEmailUsingTemplateWithAttachments(Mockito.any(),Mockito.anyString(),Mockito.any()))
            .thenReturn(true);
        serviceOfApplicationEmailService.sendEmailNotificationToLocalAuthority("", caseData, "email",
                                                                              List.of(Document.builder().build()),
                                                                               "Local authority");

        verify(sendgridService, times(1))
            .sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testLocalAuthorityEmailNotification() throws Exception {
        when(sendgridService.sendEmailUsingTemplateWithAttachments(Mockito.any(),Mockito.anyString(),Mockito.any()))
            .thenReturn(true);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                                            .solicitorEmail("test@gmail.com")
                                            .representativeLastName("LastName")
                                            .representativeFirstName("FirstName")
                                            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                                            .canYouProvideEmailAddress(YesOrNo.Yes)
                                            .email("test@applicant.com")
                                            .build())))
            .respondents(List.of(element(PartyDetails.builder()
                                             .solicitorEmail("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))

            .build();
        EmailNotificationDetails emailNotificationDetails = serviceOfApplicationEmailService.sendEmailNotificationToLocalAuthority("", caseData,
                                                                               "test@applicant.com",
                                                                               List.of(Document.builder().build()),
                                                                               PrlAppsConstants.SERVED_PARTY_LOCAL_AUTHORITY);

        assertNotNull(emailNotificationDetails);
    }

    @Test
    public void testsendEmailUsingTemplateWithAttachments() throws Exception {
        when(sendgridService.sendEmailUsingTemplateWithAttachments(Mockito.any(),Mockito.anyString(),Mockito.any()))
            .thenReturn(true);
        serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments("test",
                                                                               "", List.of(Document.builder().build()),
                                                                               SendgridEmailTemplateNames
                                                                                   .SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB,
                                                                               new HashMap<>(),
                                                                               PrlAppsConstants.SERVED_PARTY_RESPONDENT_SOLICITOR);

        verify(sendgridService, times(1))
            .sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testsendEmailUsingTemplateWithAttachmentsWelsh() throws Exception {
        serviceOfApplicationEmailService.sendLanguageEmailUsingTemplateWithAttachments("test",
            "", List.of(Document.builder().build()),
            SendgridEmailTemplateNames
                .SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB,
            new HashMap<>(),
            PrlAppsConstants.SERVED_PARTY_RESPONDENT_SOLICITOR,
            LanguagePreference.welsh);

        verify(sendgridService, times(1))
            .sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }

    public void testCitizenEmailVars() {
        EmailTemplateVars emailTemplateVars = serviceOfApplicationEmailService.buildCitizenEmailVars(CaseData.builder()
                                                                                                         .id(123L)
                                                                                                         .build(),
                                                                                 PartyDetails.builder().build(), "Yes");
        assertEquals("123", emailTemplateVars.getCaseReference());
    }
}
