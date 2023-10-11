package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.SendGridRequest;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationEmailService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SendGridControllerTest {

    @InjectMocks
    private SendGridController sendGridController;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Mock
    private SendgridService sendgridService;

    @Mock
    IdamClient idamClient;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    String auth = "authorisation";

    @Test
    public void testSendGridSendEmail() throws Exception {

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("subject", "Case documents for : ");
        combinedMap.put("content", "Case details");
        combinedMap.put("attachmentType", "pdf");
        combinedMap.put("disposition", "attachment");
        combinedMap.put("specialNote", "Yes");
        ReflectionTestUtils.setField(sendGridController, "sendgridService", sendgridService);

        Document finalDoc = Document.builder()
            .documentUrl("finalDoc")
            .documentBinaryUrl("finalDoc")
            .documentHash("finalDoc")
            .build();

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        final List<Document> documentList = List.of(coverSheet, finalDoc);

        EmailNotificationDetails emailNotificationDetails = EmailNotificationDetails.builder().build();

        SendGridRequest sendGridRequest = SendGridRequest.sendGridRequestWith().emailProps(combinedMap)
            .toEmailAddress("test@test.com").listOfAttachments(documentList).servedParty("test").build();

        when(sendgridService.sendEmailWithAttachments(auth, combinedMap,"test@test.com",documentList,"test"))
            .thenReturn(emailNotificationDetails);
        UserDetails userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .id("testId")
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        sendGridController.sendEmail(auth,s2sToken, sendGridRequest);

        verify(sendgridService).sendEmailWithAttachments(auth, combinedMap,"test@test.com",documentList,"test");
    }


}
