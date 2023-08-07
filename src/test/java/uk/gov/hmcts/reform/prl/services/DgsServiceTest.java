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
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.TypeOfDocumentUpload;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
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

    private DocumentRequest documentRequest;

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

        documentRequest = DocumentRequest.builder()
            .caseId("123")
            .typeOfUpload(TypeOfDocumentUpload.GENERATE)
            .categoryId("POSITION_STATEMENTS")
            .partyName("appf appl")
            .partyType("applicant")
            .restrictDocumentDetails("test details")
            .freeTextStatements("free text to generate document")
            .build();
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
    public void testToGenerateCoverLetterDocument() throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("coverLetter", "test.pdf");
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        assertNotNull(dgsService.generateCoverLetterDocument(authToken, dataMap, PRL_DRAFT_TEMPLATE,
                                                "123"));
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
        //dgsService.generateDocument(authToken,null, PRL_DRAFT_TEMPLATE);
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

    @Test
    public void testGenerateCitizenDocument() throws Exception {
        //Given
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        //When
        doReturn(generatedDocumentInfo).when(dgsApiClient).generateDocument(
            Mockito.anyString(),
            Mockito.any(GenerateDocumentRequest.class)
        );

        //Action
        GeneratedDocumentInfo response = dgsService.generateCitizenDocument(" ", documentRequest, " ");

        //Then
        assertNotNull(response);
        assertNotNull(response.getBinaryUrl());
        assertNotNull(response.getHashToken());
    }
}
