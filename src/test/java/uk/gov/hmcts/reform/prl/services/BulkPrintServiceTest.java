package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@RunWith(MockitoJUnitRunner.class)
public class BulkPrintServiceTest {

    @Mock
    private SendLetterApi sendLetterApi;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @InjectMocks
    private BulkPrintService bulkPrintService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private DocumentGenService documentGenService;

    private UUID uuid;

    private Document docInfo;
    private String authToken;
    private String s2sToken;

    @Before
    public void setUp() {
        uuid = randomUUID();
        authToken = "auth-token";
        s2sToken = "s2sToken";
        docInfo = Document.builder()
            .documentUrl("TestUrl")
            .documentCreatedOn(new Date())
            .documentBinaryUrl("binaryUrl")
            .build();
    }

    @Test
    public void sendLetterServiceWithValidInput() throws IOException {
        Resource expectedResource = new ClassPathResource("task-list-markdown.md");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);
        SendLetterResponse sendLetterResponse = new SendLetterResponse(uuid);
        PartyDetails applicant = PartyDetails.builder()
            .solicitorEmail("test@gmail.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@applicant.com")
            .build();

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

        final CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("test")
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(applicant)))
            .respondents(List.of(element(PartyDetails.builder()
                                             .solicitorEmail("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))
            .build();

        when(sendLetterApi.sendLetter(any(), any(LetterWithPdfsRequest.class))).thenReturn(sendLetterResponse);

        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(documentGenService.convertToPdf(authToken,docInfo)).thenReturn(docInfo);

        when(caseDocumentClient.getDocumentBinary(authToken, s2sToken, "binaryUrl"))
            .thenReturn(expectedResponse);
        assertEquals(bulkPrintService.send(
            String.valueOf(caseData.getId()),
            authToken,
            "abc",
            List.of(docInfo),
            "test"
        ), uuid);

    }

    @Test
    public void sendLetterServiceWithInvalidBinaryUrl() {
        Resource expectedResource = new ClassPathResource("task-list-markdown.md");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);
        SendLetterResponse sendLetterResponse = new SendLetterResponse(uuid);
        PartyDetails applicant = PartyDetails.builder()
            .solicitorEmail("test@gmail.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@applicant.com")
            .build();

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

        final CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("test")
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(applicant)))
            .respondents(List.of(element(PartyDetails.builder()
                                             .solicitorEmail("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))
            .build();

        //when(sendLetterApi.sendLetter(any(), any(LetterWithPdfsRequest.class))).thenReturn(sendLetterResponse);


        when(authTokenGenerator.generate()).thenReturn(s2sToken);

        assertThrows(
            NullPointerException.class,
            () -> bulkPrintService.send("123",
                                        authToken,
                                        "abc",
                                        null,
                                        "test"
            ));

    }

    @Test
    public void sendLetterServiceBulkPrintFails() {
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

        when(documentGenService.convertToPdf(authToken,finalDoc)).thenThrow(new RuntimeException());
        when(authTokenGenerator.generate()).thenReturn(s2sToken);

        assertThrows(
                Exception.class,
                () -> bulkPrintService.send("123",
                        authToken,
                        "abc",
                        documentList,
                        "test"
                ));

    }

    @Test
    public void sendLetterServiceWithInValidInput() {
        assertThrows(
            NullPointerException.class,
            () -> bulkPrintService.send("123",
                                        authToken,
                                        "abc",
                                        null,
                                        "test"
            )
        );
    }


}
