package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import feign.FeignException;
import org.jspecify.annotations.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.framework.exceptions.DocumentGenerationException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentCategory;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class DgsServiceTest {

    private static final String AUTHORISATION = " ";
    private static final String TEMPLATE = "template";
    private static final String TEST_URL = "TestUrl";
    private static final String CASE_ID = "123";
    private static final String WITNESS_STATEMENTS_RESPONDENT = "WITNESS_STATEMENTS_RESPONDENT";
    private static final String RESPONDENT = "respondent";
    private static final String DOCUMENT_DETAILS = "test details";
    private static final String FREE_TEXT_STATEMENTS = "free text to generate document";
    private static final String PARTY_NAME = "appf appl";
    private static final String FIRST_NAME = "firstNameValue";
    private static final String LAST_NAME = "lastName";
    private static final String WITNESS_STATEMENTS_APPLICANT = "WITNESS_STATEMENTS_APPLICANT";
    private static final String PARTY_TYPE = "applicant";
    private DgsService dgsService;

    @Mock
    private DgsApiClient dgsApiClient;

    @Mock
    private CaseService caseService;

    @Mock
    private ObjectMapper objectMapper;

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

    @Before
    public void setUp() {
        dgsService = new DgsService(dgsApiClient, allegationOfHarmRevisedService, hearingDataService,
                                    userRoleService, caseService, objectMapper);

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
            .caseId(CASE_ID)
            .caseData(caseData)
            .build();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        Map<String, String> values = new HashMap<>();
        values.put("caseId", CASE_ID);
        values.put("freeTextUploadStatements","test");
        generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest
            .builder()
            .values(values)
            .build();
        when(dgsApiClient.generateDocument(Mockito.anyString(), Mockito.any(GenerateDocumentRequest.class)))
            .thenReturn(generatedDocumentInfo);

        documentRequest = DocumentRequest.builder()
            .caseId(CASE_ID)
            .categoryId("POSITION_STATEMENTS")
            .partyName(PARTY_NAME)
            .partyType(PARTY_TYPE)
            .restrictDocumentDetails(DOCUMENT_DETAILS)
            .freeTextStatements(FREE_TEXT_STATEMENTS)
            .build();
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
    }

    @Test
    public void testToGenerateDocument() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        Mockito.doNothing().when(hearingDataService).populatePartiesAndSolicitorsNames(caseData, dataMap);

        assertEquals(dgsService.generateDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE),generatedDocumentInfo);

    }

    @Test
    public void testToGenerateDocumentWithCaseData()  {
        Map<String, Object> respondentDetails = new HashMap<>();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
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
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        assertNotNull(dgsService.generateCoverLetterDocument(authToken, dataMap, PRL_DRAFT_TEMPLATE,
                                                             CASE_ID
        ));
    }

    @Test
    public void testToGenerateDocumentWithCaseDataNoDataExpectedException() {
        dgsService.generateDocument(authToken,null, PRL_DRAFT_TEMPLATE, null);
        Throwable exception = assertThrows(Exception.class, () -> {
            throw new Exception("Error generating and storing document for case");
        });
        assertEquals("Error generating and storing document for case", exception.getMessage());
    }

    @Test
    public void testToGenerateDocumentWithNoDataExpectedException() {
        Map<String, Object> dataMap = new HashMap<>();
        Mockito.doNothing().when(hearingDataService).populatePartiesAndSolicitorsNames(caseData, dataMap);
        dgsService.generateDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE);
        Throwable exception = assertThrows(Exception.class, () -> {
            throw new Exception("Error generating and storing document for case");
        });
        assertEquals("Error generating and storing document for case", exception.getMessage());
    }

    @Test
    public void testToGenerateWelshDocument()  {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        assertEquals(dgsService.generateWelshDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE),generatedDocumentInfo);
    }

    @Test
    public void testToGenerateWelshDocumentWithCaseData() {

        Map<String, Object> respondentDetails = new HashMap<>();
        respondentDetails.put("fullName", "test");
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        assertEquals(dgsService.generateWelshDocument(authToken, caseDetails.getCaseId(), "C100",
                                                      PRL_DRAFT_TEMPLATE, respondentDetails
        ), generatedDocumentInfo);
    }

    @Test
    public void testGenerateCitizenDocumentWithFreeTextUploadStatements() {
        Map<String, String> values = new HashMap<>();
        values.put("caseId", CASE_ID);
        GenerateAndUploadDocumentRequest request = GenerateAndUploadDocumentRequest
            .builder()
            .values(values)
            .build();

        // when
        GeneratedDocumentInfo result = dgsService.generateCitizenDocument(
            AUTHORISATION,
            request,
            TEMPLATE
        );

        // then
        assertNotNull(result);
        assertEquals(TEST_URL, result.getUrl());
    }

    @Test
    public void testToGenerateDocumentWithEmptyHearingsData()  {
        CaseData caseData1 = CaseData.builder().manageOrders(ManageOrders.builder().build()).build();
        CaseDetails caseDetails1 = CaseDetails.builder()
            .caseId(CASE_ID)
            .caseData(caseData1)
            .build();

        assertEquals(dgsService.generateDocument(authToken, caseDetails1, PRL_DRAFT_TEMPLATE), generatedDocumentInfo);
    }

    @Test
    public void testGenerateRespondentWitnessStatementForC100() {
        // Given
        ObjectMapper objectMapper = getObjectMapper();
        setUpGenerateCitizenDocument();
        documentRequest = DocumentRequest.builder()
            .caseId(CASE_ID)
            .categoryId(WITNESS_STATEMENTS_RESPONDENT)
            .partyName(PARTY_NAME)
            .partyType(RESPONDENT)
            .restrictDocumentDetails(DOCUMENT_DETAILS)
            .freeTextStatements(FREE_TEXT_STATEMENTS)
            .build();

        PartyDetails partyDetail = PartyDetails.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .build();
        Element<PartyDetails> element = Element.<PartyDetails>builder().id(UUID.randomUUID())
            .value(partyDetail).build();
        CaseData data = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element))
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsFromCcd = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(Long.parseLong(documentRequest.getCaseId()))
            .data(objectMapper.convertValue(data, Map.class))
            .build();
        when(caseService.getCase(AUTHORISATION, documentRequest.getCaseId())).thenReturn(caseDetailsFromCcd);

        // When
        GeneratedDocumentInfo response = dgsService.generateCitizenDocument(
            AUTHORISATION, documentRequest,
            TEMPLATE, DocumentCategory.WITNESS_STATEMENTS_RESPONDENT
        );

        // Then
        assertNotNull(response);
        assertNotNull(response.getBinaryUrl());
        assertNotNull(response.getHashToken());
    }



    @Test
    public void testGenerateRespondentWitnessStatementForFl401() {
        // Given
        ObjectMapper objectMapper = getObjectMapper();
        setUpGenerateCitizenDocument();
        documentRequest = DocumentRequest.builder()
            .caseId(CASE_ID)
            .categoryId(WITNESS_STATEMENTS_RESPONDENT)
            .partyName(PARTY_NAME)
            .partyType(RESPONDENT)
            .restrictDocumentDetails(DOCUMENT_DETAILS)
            .freeTextStatements(FREE_TEXT_STATEMENTS)
            .build();

        PartyDetails partyDetail = PartyDetails.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .build();
        CaseData data = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetail)
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsFromCcd = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(Long.parseLong(documentRequest.getCaseId()))
            .data(objectMapper.convertValue(data, Map.class))
            .build();
        when(caseService.getCase(AUTHORISATION, documentRequest.getCaseId())).thenReturn(caseDetailsFromCcd);

        // When
        GeneratedDocumentInfo response = dgsService.generateCitizenDocument(
            AUTHORISATION, documentRequest,
            TEMPLATE, DocumentCategory.WITNESS_STATEMENTS_RESPONDENT
        );

        // Then
        assertNotNull(response);
        assertNotNull(response.getBinaryUrl());
        assertNotNull(response.getHashToken());
    }




    @Test
    public void testGenerateApplicantWitnessStatementForC100() {
        // Given
        ObjectMapper objectMapper = getObjectMapper();
        setUpGenerateCitizenDocument();
        documentRequest = DocumentRequest.builder()
            .caseId(CASE_ID)
            .categoryId(WITNESS_STATEMENTS_APPLICANT)
            .partyName(PARTY_NAME)
            .partyType(PARTY_TYPE)
            .restrictDocumentDetails(DOCUMENT_DETAILS)
            .freeTextStatements(FREE_TEXT_STATEMENTS)
            .build();

        PartyDetails partyDetail = PartyDetails.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .build();
        Element<PartyDetails> element = Element.<PartyDetails>builder().id(UUID.randomUUID())
            .value(partyDetail).build();
        CaseData data = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(List.of(element))
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsFromCcd = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(Long.parseLong(documentRequest.getCaseId()))
            .data(objectMapper.convertValue(data, Map.class))
            .build();
        when(caseService.getCase(AUTHORISATION, documentRequest.getCaseId())).thenReturn(caseDetailsFromCcd);

        // When
        GeneratedDocumentInfo response = dgsService.generateCitizenDocument(
            AUTHORISATION, documentRequest,
            TEMPLATE, DocumentCategory.WITNESS_STATEMENTS_APPLICANT
        );

        // Then
        assertNotNull(response);
        assertNotNull(response.getBinaryUrl());
        assertNotNull(response.getHashToken());
    }

    @Test
    public void testGenerateApplicantWitnessStatementForFl401() {
        // Given
        ObjectMapper objectMapper = getObjectMapper();
        setUpGenerateCitizenDocument();
        documentRequest = DocumentRequest.builder()
            .caseId(CASE_ID)
            .categoryId(WITNESS_STATEMENTS_APPLICANT)
            .partyName(PARTY_NAME)
            .partyType(PARTY_TYPE)
            .restrictDocumentDetails(DOCUMENT_DETAILS)
            .freeTextStatements(FREE_TEXT_STATEMENTS)
            .build();

        PartyDetails partyDetail = PartyDetails.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .build();

        CaseData data = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .respondentsFL401(partyDetail)
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsFromCcd = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(Long.parseLong(documentRequest.getCaseId()))
            .data(objectMapper.convertValue(data, Map.class))
            .build();
        when(caseService.getCase(AUTHORISATION, documentRequest.getCaseId())).thenReturn(caseDetailsFromCcd);

        // When
        GeneratedDocumentInfo response = dgsService.generateCitizenDocument(
            AUTHORISATION, documentRequest,
            TEMPLATE, DocumentCategory.WITNESS_STATEMENTS_APPLICANT
        );

        // Then
        assertNotNull(response);
        assertNotNull(response.getBinaryUrl());
        assertNotNull(response.getHashToken());
    }




    @Test
    public void testGenerateCitizenDocumentWithCaseDetailsRetrievedFromCcd() {
        // Given
        setUpGenerateCitizenDocument();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsFromCcd = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(Long.parseLong(documentRequest.getCaseId()))
            .build();


        when(caseService.getCase(AUTHORISATION, documentRequest.getCaseId())).thenReturn(caseDetailsFromCcd);


        // When
        GeneratedDocumentInfo response = dgsService.generateCitizenDocument(
            AUTHORISATION, documentRequest,
            TEMPLATE, DocumentCategory.WITNESS_STATEMENTS_RESPONDENT
        );

        //Then
        assertNotNull(response);
        assertNotNull(response.getBinaryUrl());
        assertNotNull(response.getHashToken());
    }

    @Test
    public void testGenerateCitizenDocumentThrowsFeignException() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);
        assertExpectedException(() -> dgsService.generateCitizenDocument(AUTHORISATION, generateAndUploadDocumentRequest, TEMPLATE),
                                DocumentGenerationException.class, null);
    }

    @Test
    public void testGenerateCitizenDocumentCitizenUploadThrowsException() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);
        assertExpectedException(() -> dgsService.generateCitizenDocument(AUTHORISATION, documentRequest, TEMPLATE, null),
                                DocumentGenerationException.class, null);
    }


    @Test
    public void testToGenerateCoverLetterDocumentThrowsException() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("coverLetter", "test.pdf");
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);
        assertExpectedException(() -> dgsService.generateCoverLetterDocument(authToken, dataMap, PRL_DRAFT_TEMPLATE,
                                                                             CASE_ID
        ), DocumentGenerationException.class, null);

    }

    @Test
    public void testToGenerateWelshDocumentThrowsException() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);
        assertExpectedException(() -> dgsService.generateWelshDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE),
                                DocumentGenerationException.class, null);
    }

    @Test
    public void testToGenerateDocumentThrowsException() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        Mockito.doNothing().when(hearingDataService).populatePartiesAndSolicitorsNames(caseData, dataMap);

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);
        assertExpectedException(() -> dgsService.generateDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE),
                                DocumentGenerationException.class, null);
    }

    @Test
    public void testToGenerateDocumentWithCaseDataThrowsRuntimeException() {
        Map<String, Object> respondentDetails = new HashMap<>();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(RuntimeException.class);
        assertExpectedException(() -> dgsService.generateDocument(authToken, null, PRL_DRAFT_TEMPLATE, respondentDetails),
                                DocumentGenerationException.class, null);

    }

    @Test
    public void testToGenerateDocumentWithCaseDataThrowsExcetion() {
        Map<String, Object> respondentDetails = new HashMap<>();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsApiClient.generateDocument(any(),any())).thenThrow(FeignException.class);
        assertExpectedException(() -> dgsService.generateDocument(authToken, null, PRL_DRAFT_TEMPLATE, respondentDetails),
                                DocumentGenerationException.class, null);

    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

    private void setUpGenerateCitizenDocument() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(TEST_URL)
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();


        doReturn(generatedDocumentInfo).when(dgsApiClient).generateDocument(
            Mockito.anyString(),
            Mockito.any(GenerateDocumentRequest.class)
        );
    }


    private @NonNull ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        ReflectionTestUtils.setField(dgsService, "objectMapper", objectMapper);
        return objectMapper;
    }
}
