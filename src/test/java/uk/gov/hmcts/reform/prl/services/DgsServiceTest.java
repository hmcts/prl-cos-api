package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DgsServiceTest {

    @InjectMocks
    private DgsService dgsService;

    @Mock
    private DgsApiClient dgsApiClient;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String PRL_DRAFT_TEMPLATE = "FL-DIV-GOR-ENG-00062.docx";
    private CaseData caseData;
    private CaseDetails caseDetails;
    private GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest;

    @Before
    public void setUp() {

        caseData = CaseData.builder().build();

        caseDetails = CaseDetails.builder()
            .caseId("123")
            .caseData(caseData)
            .build();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        Map<String, String> values = new HashMap<>();
        values.put("caseId","123");
        values.put("freeTextUploadStatements","test");
        generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest
            .builder()
            .values(values)
            .build();
        when(dgsApiClient.generateDocument(Mockito.anyString(), Mockito.any(GenerateDocumentRequest.class)))
            .thenReturn(generatedDocumentInfo);
    }

    @Test
    public void testToGenerateDocument() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        assertEquals(dgsService.generateDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE),generatedDocumentInfo);

    }

    @Test
    public void testToGenerateDocumentWithCaseData() throws Exception {
        Map<String, Object> respondentDetails = new HashMap<>();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        assertEquals(dgsService.generateDocument(authToken, null, PRL_DRAFT_TEMPLATE,
                                                 respondentDetails), generatedDocumentInfo);
    }

    @Test
    public void testToGenerateDocumentWithCaseDataNoDataExpectedException() throws Exception {
        dgsService.generateDocument(authToken,null, PRL_DRAFT_TEMPLATE, null);
        Throwable exception = assertThrows(Exception.class, () -> {
            throw new Exception("Error generating and storing document for case");
        });
        assertEquals("Error generating and storing document for case", exception.getMessage());
    }

    @Test
    public void testToGenerateDocumentWithNoDataExpectedException() throws Exception {
        dgsService.generateDocument(authToken,null, PRL_DRAFT_TEMPLATE);
        Throwable exception = assertThrows(Exception.class, () -> {
            throw new Exception("Error generating and storing document for case");
        });
        assertEquals("Error generating and storing document for case", exception.getMessage());
    }

    @Test
    public void testToGenerateWelshDocument() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        assertEquals(dgsService.generateWelshDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE),generatedDocumentInfo);
    }

    @Test
    public void testToGenerateWelshDocumentWithCaseData() throws Exception {

        Map<String, Object> respondentDetails = new HashMap<>();
        respondentDetails.put("fullName", "test");
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        assertEquals(dgsService.generateWelshDocument(authToken, caseDetails.getCaseId(), "C100",
                                                      PRL_DRAFT_TEMPLATE, respondentDetails
        ), generatedDocumentInfo);
    }

    @Test
    public void testgenerateCitizenDocument() throws Exception {
        dgsService.generateCitizenDocument(" ", generateAndUploadDocumentRequest, " ");
        assertEquals("test", generateAndUploadDocumentRequest.getValues().get("freeTextUploadStatements"));
    }
}
