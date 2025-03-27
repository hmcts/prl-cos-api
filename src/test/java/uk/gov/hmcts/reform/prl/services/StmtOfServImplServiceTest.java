package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.StatementOfServiceWhatWasServed;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.ServeOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.CoverLetterMap;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StatementOfService;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALL_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C9_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL415_FILENAME;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class StmtOfServImplServiceTest {

    @InjectMocks
    private StmtOfServImplService stmtOfServImplService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Mock
    private AllTabServiceImpl allTabService;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private ManageDocumentsService manageDocumentsService;

    private DynamicList dynamicList1;
    private PartyDetails respondent;
    private Element<PartyDetails> wrappedRespondents;
    private List<Element<PartyDetails>> listOfRespondents;
    private List<Element<CoverLetterMap>> coverLetterMap;
    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";
    private LocalDateTime testDate = LocalDateTime.of(2024, 04, 28, 1, 0);

    public static final String authToken = "Bearer TestAuthToken";
    private static final String ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER = "enable-citizen-access-code-in-cover-letter";

    @Before
    public void setup() {
        respondent = PartyDetails.builder()
            .lastName("TestLast")
            .firstName("TestFirst")
            .build();
        PartyDetails respondent2 = PartyDetails.builder()
            .lastName("TestLast")
            .firstName("TestFirst")
            .solicitorEmail("test@gmail.com")
            .build();
        coverLetterMap = List.of(element(UUID.fromString(TEST_UUID), CoverLetterMap.builder()
                                             .coverLetters(List.of(element(Document.builder().build()))).build()));
        wrappedRespondents = Element.<PartyDetails>builder()
            .id(UUID.fromString(TEST_UUID))
            .value(respondent).build();

        listOfRespondents = Arrays.asList(wrappedRespondents, element(UUID.fromString(TEST_UUID), respondent2));

        DynamicListElement dynamicListElement = DynamicListElement.builder().code(TEST_UUID).label("").build();
        dynamicList1 = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();
        when(serviceOfApplicationPostService.getCoverSheets(Mockito.any(), Mockito.any(), Mockito.any(),Mockito.any(),Mockito.any()))
            .thenReturn(List.of(Document.builder().build()));
        when(launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)).thenReturn(true);
    }

    @Test
    public void testToAddRespondentsAsDynamicListForC100() {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        StmtOfServiceAddRecipient stmtOfServiceAddRecipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(dynamicList1)
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
            .caseTypeOfApplication(C100_CASE_TYPE)
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

        Map<String, Object> updatedCaseData = stmtOfServImplService.retrieveRespondentsList(caseDetails);
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.get("stmtOfServiceAddRecipient"));

    }


    @Test
    public void testToAddRespondentsAsDynamicListForFL401() {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        StmtOfServiceAddRecipient stmtOfServiceAddRecipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(dynamicList1)
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
                                  .partyId(UUID.fromString(TEST_UUID))
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

        Map<String, Object> updatedCaseData = stmtOfServImplService.retrieveRespondentsList(caseDetails);
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.get("stmtOfServiceAddRecipient"));

    }

    @Test
    public void testToHandleSosAboutToSubmitForC100WhileServingApplicationPack() {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder().code(TEST_UUID).label("").build()))
            .value(DynamicListElement.builder().code(TEST_UUID).label("All respondents").build())
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
        Document c9Doc = Document.builder()
            .documentFileName(C9_DOCUMENT_FILENAME)
            .build();
        Document finalDocument = Document.builder()
            .documentFileName("C100.pdf")
            .build();
        List<Element<Document>> documentList = new ArrayList<>();
        documentList.add(element(c9Doc));
        documentList.add(element(finalDocument));

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(listOfRespondents)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                         .courtAdmin.toString())
                                                                  .packDocument(documentList)
                                                                  .build()).build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceApplicationPack)
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();
        when(launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)).thenReturn(true);
        when(manageDocumentsService.getLoggedInUserType(Mockito.anyString())).thenReturn(List.of(PrlAppsConstants.SOLICITOR_ROLE));
        when(serviceOfApplicationPostService.sendPostNotificationToParty(Mockito.any(), Mockito.anyString(), Mockito.any(),
                                                                         Mockito.any(), Mockito.anyString()))
            .thenReturn(BulkPrintDetails.builder().bulkPrintId(TEST_UUID).build());
        Map<String, Object> updatedCaseData = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testToHandleSosAboutToSubmitForC100WhileServingOrder() {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder().code(TEST_UUID).label("").build()))
            .value(DynamicListElement.builder().code(TEST_UUID).label("").build())
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
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(listOfRespondents)
            .orderCollection(Arrays.asList(element(UUID.fromString(TEST_UUID), OrderDetails.builder()
                .sosStatus("PENDING")
                .serveOrderDetails(ServeOrderDetails.builder()
                                       .servedParties(Arrays.asList(element(ServedParties.builder().build()))).build())
                .build())))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                         .courtAdmin.toString())
                                                                  .packDocument(List.of(Element.<Document>builder()
                                                                                            .value(Document.builder().build())
                                                                                            .build()))
                                                                  .build()).build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceOrder)
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();
        when(manageDocumentsService.getLoggedInUserType(Mockito.anyString())).thenReturn(List.of(PrlAppsConstants.CITIZEN_ROLE));
        when(launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)).thenReturn(true);
        Map<String, Object> updatedCaseData = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull(updatedCaseData);

    }

    @Test
    public void testToHandleSosAboutToSubmitForFL401WhileServingApplicationPack() {

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
        Document fl415 = Document.builder()
            .documentFileName(SOA_FL415_FILENAME)
            .build();
        Document finalDocument = Document.builder()
            .documentFileName("FL401.pdf")
            .build();
        List<Element<Document>> documentList = new ArrayList<>();
        documentList.add(element(fl415));
        documentList.add(element(finalDocument));
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .respondentsFL401(PartyDetails.builder()
                                  .firstName("testFl401")
                                  .lastName("lastFl401")
                                  .partyId(UUID.fromString(TEST_UUID))
                                  .build())
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                         .courtBailiff.toString())
                                                                  .coverLettersMap(coverLetterMap)
                                                                  .packDocument(documentList)
                                                                  .build()).build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceApplicationPack)
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();
        when(launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)).thenReturn(false);
        when(manageDocumentsService.getLoggedInUserType(Mockito.anyString())).thenReturn(List.of(PrlAppsConstants.COURT_ADMIN_ROLE));
        Map<String, Object> updatedCaseData = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testToHandleSosAboutToSubmitForFL401ServedByCourtAdmin() {

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
        Document fl415 = Document.builder()
            .documentFileName(SOA_FL415_FILENAME)
            .build();
        Document finalDocument = Document.builder()
            .documentFileName("FL401.pdf")
            .build();
        List<Element<Document>> documentList = new ArrayList<>();
        documentList.add(element(fl415));
        documentList.add(element(finalDocument));
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .respondentsFL401(PartyDetails.builder()
                                  .firstName("testFl401")
                                  .lastName("lastFl401")
                                  .partyId(UUID.fromString(TEST_UUID))
                                  .build())
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                         .courtAdmin.toString())
                                                                  .coverLettersMap(coverLetterMap)
                                                                  .packDocument(documentList)
                                                                  .build()).build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceApplicationPack)
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();
        when(launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)).thenReturn(true);
        when(manageDocumentsService.getLoggedInUserType(Mockito.anyString())).thenReturn(List.of(PrlAppsConstants.COURT_ADMIN_ROLE));
        Map<String, Object> updatedCaseData = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testToHandleSosAboutToSubmitForFL401WhileServingOrder() {

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
                                  .partyId(UUID.fromString(TEST_UUID))
                                  .build())
            .finalServedApplicationDetailsList(List.of(element(ServedApplicationDetails.builder().build())))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                         .courtBailiff.toString())
                                                                  .packDocument(List.of(Element.<Document>builder()
                                                                                            .value(Document.builder().build())
                                                                                            .build()))
                                                                  .build()).build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceOrder)
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> updatedCaseData = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull(updatedCaseData);

    }

    @Test
    public void testToHandleSosAboutToSubmitForC100Scenario2WhileServingApplicationPack() {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder().code(TEST_UUID).label("").build()))
            .value(DynamicListElement.builder().code(TEST_UUID).label("").build())
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
        Document c9Doc = Document.builder()
            .documentFileName(C9_DOCUMENT_FILENAME)
            .build();
        Document finalDocument = Document.builder()
            .documentFileName("C100.pdf")
            .build();
        List<Element<Document>> documentList = new ArrayList<>();
        documentList.add(element(c9Doc));
        documentList.add(element(finalDocument));
        UUID partyId = UUID.randomUUID();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(Collections.singletonList(Element.<PartyDetails>builder().id(partyId)
                                                .value(listOfRespondents.stream().findFirst().get().getValue()).build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                         .courtBailiff.toString())
                                                                  .coverLettersMap(coverLetterMap)
                                                                  .packDocument(documentList)
                                                                  .build()).build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceApplicationPack)
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .stmtOfServiceForApplication(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();
        when(launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)).thenReturn(true);
        when(serviceOfApplicationPostService.sendPostNotificationToParty(Mockito.any(), Mockito.anyString(), Mockito.any(),
                                                                         Mockito.any(), Mockito.anyString()))
            .thenReturn(BulkPrintDetails.builder().bulkPrintId(TEST_UUID).build());
        Map<String, Object> updatedCaseData = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull(updatedCaseData);
        assertEquals(TEST_UUID,
                     ((List<Element<StmtOfServiceAddRecipient>>)updatedCaseData.get("stmtOfServiceForApplication"))
                         .get(0).getValue().getSelectedPartyId());
    }

    @Test
    public void testToHandleSosAboutToSubmitForC100Scenario2WhileServingOrder() {

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
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(listOfRespondents)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                         .courtAdmin.toString())
                                                                  .packDocument(List.of(Element.<Document>builder()
                                                                                            .value(Document.builder().build())
                                                                                            .build()))
                                                                  .build()).build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceOrder)
                                    .stmtOfServiceForOrder(listOfSos)
                                    .stmtOfServiceAddRecipient(listOfSos)
                                    .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> updatedCaseData = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull(updatedCaseData);

    }

    @Test
    public void testcitizenSosSubmissionC100() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unservedCitizenRespondentPack(SoaPack.builder()
                                                                  .personalServiceBy(
                                                                      SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
                                                                  .packDocument(List.of(element(Document.builder().build())))
                                                                         .partyIds(List.of(element(TEST_UUID)))
                                                                  .build())
                                      .build())
            .finalServedApplicationDetailsList(new ArrayList<>())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder().build())))
                                    .build())
            .respondents(listOfRespondents)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap,
                                                                                                        caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        when(serviceOfApplicationService.generateCoverLetterBasedOnCaseAccess(Mockito.anyString(),Mockito.any(),
                                                                              Mockito.any(),Mockito.anyString()))
            .thenReturn(List.of(Document.builder().build()));
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        stmtOfServImplService.saveCitizenSos("","", authToken, CitizenSos.builder()
                .partiesServed(List.of(TEST_UUID, "234", "1234"))
                .partiesServedDate("2020-08-01")
                .isOrder(YesOrNo.No)
                .citizenSosDocs(Document.builder().documentFileName("test").build())
            .build());
        verify(allTabService, times(1))
            .submitAllTabsUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.any(),Mockito.any(),Mockito.any());
    }

    @Test
    public void testcitizenSosSubmissionFl401() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unservedCitizenRespondentPack(SoaPack.builder()
                                                                  .personalServiceBy(
                                                                      SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
                                                                  .packDocument(List.of(element(Document.builder().build())))
                                                                         .partyIds(List.of(element("123")))
                                                                  .build())
                                      .build())
            .statementOfService(StatementOfService.builder()

                                    .build())
            .respondentsFL401(PartyDetails.builder().firstName("hello").lastName("World").partyId(UUID.fromString(TEST_UUID)).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap,
                                                                                                        caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        when(serviceOfApplicationService.generateCoverLetterBasedOnCaseAccess(Mockito.anyString(),Mockito.any(),
                                                                              Mockito.any(),Mockito.anyString()))
            .thenReturn(List.of(Document.builder().build()));
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        stmtOfServImplService.saveCitizenSos("","", authToken, CitizenSos.builder()
            .partiesServed(List.of(TEST_UUID, "234", "1234"))
            .isOrder(YesOrNo.No)
            .partiesServedDate("2020-08-01")
            .citizenSosDocs(Document.builder().documentFileName("test").build())
            .build());
        verify(allTabService, times(1))
            .submitAllTabsUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.any(),Mockito.any(),Mockito.any());
    }

    @Test
    public void testcitizenSosSubmissionFl401Order() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unservedCitizenRespondentPack(SoaPack.builder()
                                                                         .personalServiceBy(
                                                                             SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
                                                                         .packDocument(List.of(element(Document.builder().build())))
                                                                         .partyIds(List.of(element("123")))
                                                                         .build())
                                      .build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForOrder(List.of(element(StmtOfServiceAddRecipient.builder().build())))
                                    .build())
            .respondentsFL401(PartyDetails.builder().firstName("hello").lastName("World").partyId(UUID.fromString(TEST_UUID)).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap,
                                                                                                        caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        when(serviceOfApplicationService.generateCoverLetterBasedOnCaseAccess(Mockito.anyString(),Mockito.any(),
                                                                              Mockito.any(),Mockito.anyString()))
            .thenReturn(List.of(Document.builder().build()));
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        stmtOfServImplService.saveCitizenSos("","", authToken, CitizenSos.builder()
            .partiesServed(List.of("123", "234", "1234"))
            .isOrder(YesOrNo.Yes)
            .partiesServedDate("2020-08-01")
            .citizenSosDocs(Document.builder().documentFileName("test").build())
            .build());
        verify(allTabService, times(1))
            .submitAllTabsUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.any(),Mockito.any(),Mockito.any());
    }

    @Test
    public void testcitizenSosSubmissionC100Order() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unservedCitizenRespondentPack(SoaPack.builder()
                                                                         .personalServiceBy(
                                                                             SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
                                                                         .packDocument(List.of(element(Document.builder().build())))
                                                                         .partyIds(List.of(element("123")))
                                                                         .build())
                                      .build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForOrder(List.of(element(StmtOfServiceAddRecipient.builder().build())))
                                    .build())
            .respondents(listOfRespondents)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap,
                                                                                                        caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        when(serviceOfApplicationService.generateCoverLetterBasedOnCaseAccess(Mockito.anyString(),Mockito.any(),
                                                                              Mockito.any(),Mockito.anyString()))
            .thenReturn(List.of(Document.builder().build()));
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        stmtOfServImplService.saveCitizenSos("","", authToken, CitizenSos.builder()
            .partiesServed(List.of("123", "234", "1234"))
            .isOrder(YesOrNo.Yes)
            .partiesServedDate("2020-08-01")
            .citizenSosDocs(Document.builder().documentFileName("test").build())
            .build());
        verify(allTabService, times(1))
            .submitAllTabsUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.any(),Mockito.any(),Mockito.any());
    }
}
