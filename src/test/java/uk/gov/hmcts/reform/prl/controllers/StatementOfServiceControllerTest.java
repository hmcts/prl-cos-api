package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.StatementOfServiceWhatWasServed;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StatementOfService;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.StmtOfServImplService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALL_RESPONDENTS;

@RunWith(MockitoJUnitRunner.Silent.class)
public class StatementOfServiceControllerTest {

    @InjectMocks
    private StatementOfServiceController statementOfServiceController;

    @Mock
    private StmtOfServImplService stmtOfServImplService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private ObjectMapper objectMapper;

    private DynamicList dynamicList;
    private PartyDetails respondent;
    private Element<PartyDetails> wrappedRespondents;
    private List<Element<PartyDetails>> listOfRespondents;
    private UUID uuid;
    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Before
    public void setup() {

        PartyDetails respondent = PartyDetails.builder()
            .lastName("TestLast")
            .firstName("TestFirst")
            .build();

        wrappedRespondents = Element.<PartyDetails>builder()
            .id(UUID.fromString(TEST_UUID))
            .value(respondent).build();
        listOfRespondents = Collections.singletonList(wrappedRespondents);

        DynamicListElement dynamicListElement = DynamicListElement.builder().code(TEST_UUID).label("").build();
        dynamicList = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();
    }

    @Test
    public void testToAboutToStartC100() {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        StmtOfServiceAddRecipient stmtOfServiceAddRecipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(dynamicList)
            .stmtOfServiceDocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName("testFile.pdf")
                                       .build())
            .build();

        Element<StmtOfServiceAddRecipient> wrappedSos = Element.<StmtOfServiceAddRecipient>builder()
            .id(UUID.fromString(TEST_UUID))
            .value(stmtOfServiceAddRecipient).build();
        List<Element<StmtOfServiceAddRecipient>> listOfSos = Collections.singletonList(wrappedSos);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(listOfRespondents)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(stmtOfServImplService.retrieveRespondentsList(caseDetails)).thenReturn(stringObjectMap);

        assertNotNull(statementOfServiceController.sosAboutToStart(authToken, s2sToken, callbackRequest));

    }


    @Test
    public void testToAboutToStartForFL401() {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        StmtOfServiceAddRecipient stmtOfServiceAddRecipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(dynamicList)
            .stmtOfServiceDocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName("testFile.pdf")
                                       .build())
            .build();

        Element<StmtOfServiceAddRecipient> wrappedSos = Element.<StmtOfServiceAddRecipient>builder()
            .id(UUID.fromString(TEST_UUID))
            .value(stmtOfServiceAddRecipient).build();
        List<Element<StmtOfServiceAddRecipient>> listOfSos = Collections.singletonList(wrappedSos);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(PartyDetails.builder()
                                  .firstName("testFl401")
                                  .lastName("lastFl401")
                                  .build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(stmtOfServImplService.retrieveRespondentsList(caseDetails)).thenReturn(stringObjectMap);

        assertNotNull(statementOfServiceController.sosAboutToStart(authToken,s2sToken,callbackRequest));

    }

    @Test
    public void testAboutToSubmitC100() {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder().code(TEST_UUID).label("").build()))
            .value(DynamicListElement.builder().code(TEST_UUID).label(ALL_RESPONDENTS).build())
            .build();

        StmtOfServiceAddRecipient stmtOfServiceAddRecipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(dynamicList)
            .stmtOfServiceDocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName("testFile.pdf")
                                       .build())
            .build();

        Element<StmtOfServiceAddRecipient> wrappedSos = Element.<StmtOfServiceAddRecipient>builder()
            .id(UUID.fromString(TEST_UUID))
            .value(stmtOfServiceAddRecipient).build();
        List<Element<StmtOfServiceAddRecipient>> listOfSos = Collections.singletonList(wrappedSos);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(listOfRespondents)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(stmtOfServImplService.retrieveRespondentsList(caseDetails)).thenReturn(stringObjectMap);

        assertNotNull(statementOfServiceController.sosAboutToSubmit(authToken,s2sToken,callbackRequest));

    }

    @Test
    public void testToRetrieveAllRespondentNamesForFL401() {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder().code(TEST_UUID).label("").build()))
            .value(DynamicListElement.builder().code(TEST_UUID).label(ALL_RESPONDENTS).build())
            .build();

        StmtOfServiceAddRecipient stmtOfServiceAddRecipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(dynamicList)
            .stmtOfServiceDocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName("testFile.pdf")
                                       .build())
            .build();

        Element<StmtOfServiceAddRecipient> wrappedSos = Element.<StmtOfServiceAddRecipient>builder()
            .id(UUID.fromString(TEST_UUID))
            .value(stmtOfServiceAddRecipient).build();
        List<Element<StmtOfServiceAddRecipient>> listOfSos = Collections.singletonList(wrappedSos);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(PartyDetails.builder()
                                  .firstName("testFl401")
                                  .lastName("lastFl401")
                                  .build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(stmtOfServImplService.retrieveRespondentsList(caseDetails)).thenReturn(stringObjectMap);

        assertNotNull(statementOfServiceController.sosAboutToStart(authToken,s2sToken,callbackRequest));

    }

    @Test
    public void testSubmittedConfirmationForApplicationPack() throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder().code(TEST_UUID).label("").build()))
            .value(DynamicListElement.builder().code(TEST_UUID).label(ALL_RESPONDENTS).build())
            .build();

        StmtOfServiceAddRecipient stmtOfServiceAddRecipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(dynamicList)
            .stmtOfServiceDocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName("testFile.pdf")
                                       .build())
            .build();

        Element<StmtOfServiceAddRecipient> wrappedSos = Element.<StmtOfServiceAddRecipient>builder()
            .id(UUID.fromString(TEST_UUID))
            .value(stmtOfServiceAddRecipient).build();
        List<Element<StmtOfServiceAddRecipient>> listOfSos = Collections.singletonList(wrappedSos);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(PartyDetails.builder()
                                  .firstName("testFl401")
                                  .lastName("lastFl401")
                                  .build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceApplicationPack)
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(stmtOfServImplService.retrieveRespondentsList(caseDetails)).thenReturn(stringObjectMap);
        assertNotNull(statementOfServiceController.sosSubmitConfirmation(authToken, s2sToken, callbackRequest));
    }

    @Test
    public void testSubmittedConfirmationForOrder() throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder().code(TEST_UUID).label("").build()))
            .value(DynamicListElement.builder().code(TEST_UUID).label(ALL_RESPONDENTS).build())
            .build();

        StmtOfServiceAddRecipient stmtOfServiceAddRecipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(dynamicList)
            .stmtOfServiceDocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName("testFile.pdf")
                                       .build())
            .build();

        Element<StmtOfServiceAddRecipient> wrappedSos = Element.<StmtOfServiceAddRecipient>builder()
            .id(UUID.fromString(TEST_UUID))
            .value(stmtOfServiceAddRecipient).build();
        List<Element<StmtOfServiceAddRecipient>> listOfSos = Collections.singletonList(wrappedSos);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(PartyDetails.builder()
                                  .firstName("testFl401")
                                  .lastName("lastFl401")
                                  .build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceOrder)
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(stmtOfServImplService.retrieveRespondentsList(caseDetails)).thenReturn(stringObjectMap);
        assertNotNull(statementOfServiceController.sosSubmitConfirmation(authToken, s2sToken, callbackRequest));
    }
}
