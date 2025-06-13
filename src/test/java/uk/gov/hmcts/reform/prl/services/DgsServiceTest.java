package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.framework.exceptions.DocumentGenerationException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DgsServiceTest {

    @InjectMocks
    private DgsService dgsService;

    @Mock
    private DgsApiClient dgsApiClient;
    @Mock
    private UserRoleService userRoleService;

    @Mock
    private AllegationOfHarmRevisedService allegationOfHarmRevisedService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private HearingDataService hearingDataService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String PRL_DRAFT_TEMPLATE = "FL-DIV-GOR-ENG-00062.docx";
    private CaseData caseData;
    private CaseDetails caseDetails;
    private GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest;

    private DocumentRequest documentRequest;

    @BeforeEach
    void setUp() {

        caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(
                                  List.of(Element.<HearingData>builder()
                                              .id(UUID.randomUUID())
                                              .value(HearingData.builder().build())
                                              .build()))
                              .build())
            .build();

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
            .categoryId("POSITION_STATEMENTS")
            .partyName("appf appl")
            .partyType("applicant")
            .restrictDocumentDetails("test details")
            .freeTextStatements("free text to generate document")
            .build();
    }

    @Test
    void testToGenerateDocument() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        Mockito.doNothing().when(hearingDataService).populatePartiesAndSolicitorsNames(caseData, dataMap);

        assertEquals(dgsService.generateDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE),generatedDocumentInfo);

    }

    @Test
    void testToGenerateDocumentWithCaseData() throws Exception {
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
    void testToGenerateCoverLetterDocument() throws Exception {
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
    void testToGenerateDocumentWithCaseDataNoDataExpectedException() throws Exception {
        dgsService.generateDocument(authToken,null, PRL_DRAFT_TEMPLATE, null);
        Throwable exception = assertThrows(Exception.class, () -> {
            throw new Exception("Error generating and storing document for case");
        });
        assertEquals("Error generating and storing document for case", exception.getMessage());
    }

    @Test
    void testToGenerateDocumentWithNoDataExpectedException() throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        Mockito.doNothing().when(hearingDataService).populatePartiesAndSolicitorsNames(caseData, dataMap);
        dgsService.generateDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE);
        Throwable exception = assertThrows(Exception.class, () -> {
            throw new Exception("Error generating and storing document for case");
        });
        assertEquals("Error generating and storing document for case", exception.getMessage());
    }

    @Test
    void testToGenerateWelshDocument() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        assertEquals(dgsService.generateWelshDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE),generatedDocumentInfo);
    }

    @Test
    void testToGenerateWelshDocumentWithCaseData() {

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
    void testgenerateCitizenDocument() throws Exception {
        dgsService.generateCitizenDocument(" ", generateAndUploadDocumentRequest, " ");
        assertEquals("test", generateAndUploadDocumentRequest.getValues().get("freeTextUploadStatements"));
    }

    @Test
    void testToGenerateDocumentWithEmptyHearingsData() throws Exception {
        CaseData caseData1 = CaseData.builder().manageOrders(ManageOrders.builder().build()).build();
        CaseDetails caseDetails1 = CaseDetails.builder()
            .caseId("123")
            .caseData(caseData1)
            .build();

        assertEquals(dgsService.generateDocument(authToken, caseDetails1, PRL_DRAFT_TEMPLATE), generatedDocumentInfo);
    }

    @Test
    void testGenerateCitizenDocument() throws Exception {
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

    @Test
    void testGenerateCitizenDocumentThrowsFeignException() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);
        DocumentGenerationException ex = assertThrows(DocumentGenerationException.class, () -> {
            dgsService.generateCitizenDocument(" ", generateAndUploadDocumentRequest, " ");
        });
    }

    @Test
    void testGenerateCitizenDocumentCitizenUploadThrowsException() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);

        DocumentGenerationException ex = assertThrows(DocumentGenerationException.class, () -> {
            dgsService.generateCitizenDocument(" ", documentRequest, " ");
        });
    }


    @Test
    void testToGenerateCoverLetterDocumentThrowsException() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("coverLetter", "test.pdf");
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            dgsService.generateCoverLetterDocument(authToken, dataMap, PRL_DRAFT_TEMPLATE, "123");
        });
    }

    @Test
    void testToGenerateWelshDocumentThrowsException() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);

        DocumentGenerationException ex = assertThrows(DocumentGenerationException.class, () -> {
            dgsService.generateWelshDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE);
        });
    }

    @Test
    void testToGenerateDocumentThrowsException() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        Mockito.doNothing().when(hearingDataService).populatePartiesAndSolicitorsNames(caseData, dataMap);

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);

        DocumentGenerationException ex = assertThrows(DocumentGenerationException.class, () -> {
            dgsService.generateDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE);
        });
    }

    @Test
    void testToGenerateDocumentWithCaseDataThrowsRuntimeExcetion() {
        Map<String, Object> respondentDetails = new HashMap<>();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(RuntimeException.class);

        DocumentGenerationException ex = assertThrows(DocumentGenerationException.class, () -> {
            dgsService.generateDocument(authToken, null, PRL_DRAFT_TEMPLATE,respondentDetails);
        });
    }

    @Test
    void testToGenerateDocumentWithCaseDataThrowsExcetion() {
        Map<String, Object> respondentDetails = new HashMap<>();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);

        DocumentGenerationException ex = assertThrows(DocumentGenerationException.class, () -> {
            dgsService.generateDocument(authToken, null, PRL_DRAFT_TEMPLATE,respondentDetails);
        });
    }
}
