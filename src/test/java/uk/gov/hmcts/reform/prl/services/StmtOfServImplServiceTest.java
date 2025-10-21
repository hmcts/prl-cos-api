package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
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
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import static uk.gov.hmcts.reform.prl.services.StmtOfServImplService.STMT_OF_SERVICE_FOR_APPLICATION;
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

    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;

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
        coverLetterMap = List.of(element(
            UUID.fromString(TEST_UUID), CoverLetterMap.builder()
                .coverLetters(List.of(element(Document.builder().build()))).build()
        ));
        wrappedRespondents = Element.<PartyDetails>builder()
            .id(UUID.fromString(TEST_UUID))
            .value(respondent).build();

        listOfRespondents = Arrays.asList(wrappedRespondents, element(UUID.fromString(TEST_UUID), respondent2));

        DynamicListElement dynamicListElement = DynamicListElement.builder().code(TEST_UUID).label("").build();
        dynamicList1 = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();
        when(serviceOfApplicationPostService.getCoverSheets(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        ))
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
        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(Mockito.any(CaseData.class)))
            .thenReturn(DynamicMultiSelectList.builder().listItems(new ArrayList<>()).build());

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
        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(Mockito.any(CaseData.class)))
            .thenReturn(DynamicMultiSelectList.builder().listItems(new ArrayList<>()).build());

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
        when(serviceOfApplicationPostService.sendPostNotificationToParty(
            Mockito.any(), Mockito.anyString(), Mockito.any(),
            Mockito.any(), Mockito.anyString()
        ))
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
            .orderCollection(Arrays.asList(element(
                UUID.fromString(TEST_UUID), OrderDetails.builder()
                    .sosStatus("PENDING")
                    .serveOrderDetails(ServeOrderDetails.builder()
                                           .servedParties(Arrays.asList(element(ServedParties.builder().build()))).build())
                    .build()
            )))
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
        when(serviceOfApplicationPostService.sendPostNotificationToParty(
            Mockito.any(), Mockito.anyString(), Mockito.any(),
            Mockito.any(), Mockito.anyString()
        ))
            .thenReturn(BulkPrintDetails.builder().bulkPrintId(TEST_UUID).build());
        Map<String, Object> updatedCaseData = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull(updatedCaseData);
        assertEquals(
            TEST_UUID,
            ((List<Element<StmtOfServiceAddRecipient>>) updatedCaseData.get("stmtOfServiceForApplication"))
                .get(0).getValue().getSelectedPartyId()
        );
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
    public void testHandleSosForApplicationPacks_WhenConditionsFail() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceApplicationPack)
                                    .stmtOfServiceAddRecipient(Arrays.asList(element(StmtOfServiceAddRecipient.builder()
                                                                                         .respondentDynamicList(
                                                                                             DynamicList.builder()
                                                                                                 .value(
                                                                                                     DynamicListElement.builder().code(
                                                                                                         TEST_UUID).label(
                                                                                                         "Test Recipient").build())
                                                                                                 .build())
                                                                                         .stmtOfServiceDocument(Document.builder().documentFileName(
                                                                                             "test.pdf").build())
                                                                                         .build())))
                                    .build())
            .serviceOfApplication(null)
            .respondents(listOfRespondents)
            .build();

        Map<String, Object> result = stmtOfServImplService.handleSosAboutToSubmit(
            createCaseDetails(caseData), authToken);

        assertNull(result.get("finalServedApplicationDetailsList"));
    }

    @Test
    public void testCitizenSosSubmissionC100() {
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
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap,
            caseData,
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(
            startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(
            anyString(),
            anyString(),
            any(),
            any(),
            any()
        )).thenReturn(CaseDetails.builder().build());

        when(serviceOfApplicationService.generateCoverLetterBasedOnCaseAccess(
            Mockito.anyString(), Mockito.any(),
            Mockito.any(), Mockito.anyString()
        ))
            .thenReturn(List.of(Document.builder().build()));
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        stmtOfServImplService.saveCitizenSos(
            "", "", authToken, CitizenSos.builder()
                .partiesServed(List.of(TEST_UUID, "234", "1234"))
                .partiesServedDate("2020-08-01")
                .isOrder(YesOrNo.No)
                .citizenSosDocs(Document.builder().documentFileName("test").build())
                .build()
        );
        verify(allTabService, times(1))
            .submitAllTabsUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testCitizenSosSubmissionFl401() {
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
            .respondentsFL401(PartyDetails.builder().firstName("hello").lastName("World").partyId(UUID.fromString(
                TEST_UUID)).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap,
            caseData,
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(
            startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(
            anyString(),
            anyString(),
            any(),
            any(),
            any()
        )).thenReturn(CaseDetails.builder().build());

        when(serviceOfApplicationService.generateCoverLetterBasedOnCaseAccess(
            Mockito.anyString(), Mockito.any(),
            Mockito.any(), Mockito.anyString()
        ))
            .thenReturn(List.of(Document.builder().build()));
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        stmtOfServImplService.saveCitizenSos(
            "", "", authToken, CitizenSos.builder()
                .partiesServed(List.of(TEST_UUID, "234", "1234"))
                .isOrder(YesOrNo.No)
                .partiesServedDate("2020-08-01")
                .citizenSosDocs(Document.builder().documentFileName("test").build())
                .build()
        );
        verify(allTabService, times(1))
            .submitAllTabsUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());
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
            .respondentsFL401(PartyDetails.builder().firstName("hello").lastName("World").partyId(UUID.fromString(
                TEST_UUID)).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap,
            caseData,
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(
            startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(
            anyString(),
            anyString(),
            any(),
            any(),
            any()
        )).thenReturn(CaseDetails.builder().build());

        when(serviceOfApplicationService.generateCoverLetterBasedOnCaseAccess(
            Mockito.anyString(), Mockito.any(),
            Mockito.any(), Mockito.anyString()
        ))
            .thenReturn(List.of(Document.builder().build()));
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        stmtOfServImplService.saveCitizenSos(
            "", "", authToken, CitizenSos.builder()
                .partiesServed(List.of("123", "234", "1234"))
                .isOrder(YesOrNo.Yes)
                .partiesServedDate("2020-08-01")
                .citizenSosDocs(Document.builder().documentFileName("test").build())
                .build()
        );
        verify(allTabService, times(1))
            .submitAllTabsUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());
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
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap,
            caseData,
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(
            startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(
            anyString(),
            anyString(),
            any(),
            any(),
            any()
        )).thenReturn(CaseDetails.builder().build());

        when(serviceOfApplicationService.generateCoverLetterBasedOnCaseAccess(
            Mockito.anyString(), Mockito.any(),
            Mockito.any(), Mockito.anyString()
        ))
            .thenReturn(List.of(Document.builder().build()));
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        stmtOfServImplService.saveCitizenSos(
            "", "", authToken, CitizenSos.builder()
                .partiesServed(List.of("123", "234", "1234"))
                .isOrder(YesOrNo.Yes)
                .partiesServedDate("2020-08-01")
                .citizenSosDocs(Document.builder().documentFileName("test").build())
                .build()
        );
        verify(allTabService, times(1))
            .submitAllTabsUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    private CaseData createTestScenarioForApplicationPacks(String caseType, List<String> orderIds) {
        List<Element<StmtOfServiceAddRecipient>> recipients = Arrays.asList(
            element(StmtOfServiceAddRecipient.builder()
                        .orderList(DynamicMultiSelectList.builder()
                                       .value(orderIds.stream()
                                                  .map(id -> DynamicMultiselectListElement.builder().code(id).label(
                                                      "Order " + id).build())
                                                  .toList())
                                       .build())
                        .respondentDynamicList(DynamicList.builder()
                                                   .value(DynamicListElement.builder().code(TEST_UUID).label(
                                                       "Test Recipient").build())
                                                   .build())
                        .stmtOfServiceDocument(Document.builder().documentFileName("test.pdf").build())
                        .build()));

        return CaseData.builder()
            .caseTypeOfApplication(caseType)
            .statementOfService(StatementOfService.builder()
                .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceApplicationPack)
                .stmtOfServiceAddRecipient(recipients)
                .build())
            .serviceOfApplication(ServiceOfApplication.builder()
                .unServedRespondentPack(SoaPack.builder()
                    .personalServiceBy(SoaSolicitorServingRespondentsEnum.courtAdmin.toString())
                    .packDocument(List.of(element(Document.builder()
                        .documentFileName("application-pack.pdf")
                        .build())))
                    .build())
                .build())
            .respondents(listOfRespondents)
            .build();
    }

    private CaseDetails createCaseDetails(CaseData caseData) {
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageDocumentsService.getLoggedInUserType(anyString())).thenReturn(List.of(PrlAppsConstants.COURT_ADMIN_ROLE));
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());

        return CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();
    }

    @Test
    public void testHandleSosForApplicationPacks_WhenAllConditionsPass() {
        CaseData caseData = createTestScenarioForApplicationPacks(
            C100_CASE_TYPE,
            Arrays.asList(TEST_UUID)
        );

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        when(manageDocumentsService.getLoggedInUserType(anyString())).thenReturn(List.of(PrlAppsConstants.COURT_ADMIN_ROLE));
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());

        Map<String, Object> result = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull(result);
        assertNotNull(result.get("finalServedApplicationDetailsList"));
        assertNull(result.get("unServedRespondentPack"));
    }

    @Test
    public void testHandleSosForApplicationPacks_OrderListFieldIsClearedToPreventCcdValidationError() {
        DynamicMultiSelectList orderList = DynamicMultiSelectList.builder()
            .value(Arrays.asList(
                DynamicMultiselectListElement.builder().code("order-1").label("Test Order 1").build(),
                DynamicMultiselectListElement.builder().code("order-2").label("Test Order 2").build()
            ))
            .build();

        StmtOfServiceAddRecipient recipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(DynamicList.builder()
                                       .value(DynamicListElement.builder().code(TEST_UUID).label("Test Recipient").build())
                                       .build())
            .orderList(orderList)
            .stmtOfServiceDocument(Document.builder().documentFileName("test.pdf").build())
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceApplicationPack)
                                    .stmtOfServiceAddRecipient(Arrays.asList(element(recipient)))
                                    .build())
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum.courtAdmin.toString())
                                                                  .packDocument(Arrays.asList(element(Document.builder()
                                                                                                          .documentFileName(
                                                                                                              "application-pack.pdf")
                                                                                                          .build())))
                                                                  .build())
                                      .build())
            .respondents(listOfRespondents)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        when(manageDocumentsService.getLoggedInUserType(anyString())).thenReturn(List.of(PrlAppsConstants.COURT_ADMIN_ROLE));
        when(launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)).thenReturn(false);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull(result.get(STMT_OF_SERVICE_FOR_APPLICATION));

        @SuppressWarnings("unchecked")
        List<Element<StmtOfServiceAddRecipient>> processedRecipients =
            (List<Element<StmtOfServiceAddRecipient>>) result.get(STMT_OF_SERVICE_FOR_APPLICATION);

        assertFalse("Processed recipients list should not be empty", processedRecipients.isEmpty());

        processedRecipients.forEach(recipientElement -> {
            StmtOfServiceAddRecipient processedRecipient = recipientElement.getValue();
            assertNull(
                "orderList field should be cleared to prevent CCD validation error 'Field is not recognised'",
                processedRecipient.getOrderList()
            );
            assertNull(
                "respondentDynamicList should also be cleared",
                processedRecipient.getRespondentDynamicList()
            );
        });

        assertNotNull(
            "finalServedApplicationDetailsList should be populated when conditions are met",
            result.get("finalServedApplicationDetailsList")
        );
    }

    @Test
    public void testHandleSosForOrders_OrderListFieldIsClearedToPreventCcdValidationError() {

        DynamicMultiSelectList orderList = DynamicMultiSelectList.builder()
            .value(Arrays.asList(
                DynamicMultiselectListElement.builder().code("order-1").label("Test Order 1").build(),
                DynamicMultiselectListElement.builder().code("order-2").label("Test Order 2").build()
            ))
            .build();

        StmtOfServiceAddRecipient recipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(DynamicList.builder()
                                       .value(DynamicListElement.builder().code(TEST_UUID).label("Test Recipient").build())
                                       .build())
            .orderList(orderList)
            .stmtOfServiceDocument(Document.builder().documentFileName("test.pdf").build())
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceOrder)
                                    .stmtOfServiceAddRecipient(Arrays.asList(element(recipient)))
                                    .build())
            .orderCollection(Arrays.asList(element(
                UUID.fromString(TEST_UUID), OrderDetails.builder()
                    .sosStatus("PENDING")
                    .serveOrderDetails(ServeOrderDetails.builder()
                                           .servedParties(Arrays.asList(element(ServedParties.builder().build())))
                                           .build())
                    .build()
            )))
            .respondents(listOfRespondents)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        when(manageDocumentsService.getLoggedInUserType(anyString())).thenReturn(List.of(PrlAppsConstants.COURT_ADMIN_ROLE));

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull(result.get("stmtOfServiceForOrder"));

        @SuppressWarnings("unchecked")
        List<Element<StmtOfServiceAddRecipient>> processedRecipients =
            (List<Element<StmtOfServiceAddRecipient>>) result.get("stmtOfServiceForOrder");

        assertFalse("Processed recipients list should not be empty", processedRecipients.isEmpty());

        processedRecipients.forEach(recipientElement -> {
            StmtOfServiceAddRecipient processedRecipient = recipientElement.getValue();
            assertNull(
                "orderList field should be cleared to prevent CCD validation error 'Field is not recognised'",
                processedRecipient.getOrderList()
            );
            assertNull(
                "respondentDynamicList should also be cleared",
                processedRecipient.getRespondentDynamicList()
            );
        });
    }

    @Test
    public void testHandleSosForOrders_ServedOrderIdsAreCollectedCorrectly() {
        DynamicMultiSelectList orderList = DynamicMultiSelectList.builder()
            .value(Arrays.asList(
                DynamicMultiselectListElement.builder().code("order-1").label("Test Order 1").build(),
                DynamicMultiselectListElement.builder().code("order-2").label("Test Order 2").build()
            ))
            .build();

        StmtOfServiceAddRecipient recipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(DynamicList.builder()
                                       .value(DynamicListElement.builder().code(TEST_UUID).label("Test Recipient").build())
                                       .build())
            .orderList(orderList)
            .stmtOfServiceDocument(Document.builder().documentFileName("test.pdf").build())
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceOrder)
                                    .stmtOfServiceAddRecipient(Arrays.asList(element(recipient)))
                                    .build())
            .orderCollection(Arrays.asList(element(
                UUID.fromString(TEST_UUID), OrderDetails.builder()
                    .sosStatus("PENDING")
                    .serveOrderDetails(ServeOrderDetails.builder()
                                           .servedParties(Arrays.asList(element(ServedParties.builder().build())))
                                           .build())
                    .build()
            )))
            .respondents(listOfRespondents)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        when(manageDocumentsService.getLoggedInUserType(anyString())).thenReturn(List.of(PrlAppsConstants.COURT_ADMIN_ROLE));

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull("servedOrderIds should be populated", result.get("servedOrderIds"));

        @SuppressWarnings("unchecked")
        List<String> servedOrderIds = (List<String>) result.get("servedOrderIds");

        assertEquals("Should have 2 served order IDs", 2, servedOrderIds.size());
        assertEquals("First order ID should be order-1", "order-1", servedOrderIds.get(0));
        assertEquals("Second order ID should be order-2", "order-2", servedOrderIds.get(1));
    }

    @Test
    public void testHandleSosForOrders_ServedOrderIdsPreserveExistingIds() {
        DynamicMultiSelectList orderList = DynamicMultiSelectList.builder()
            .value(Arrays.asList(
                DynamicMultiselectListElement.builder().code("order-3").label("Test Order 3").build()
            ))
            .build();

        StmtOfServiceAddRecipient recipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(DynamicList.builder()
                                       .value(DynamicListElement.builder().code(TEST_UUID).label("Test Recipient").build())
                                       .build())
            .orderList(orderList)
            .stmtOfServiceDocument(Document.builder().documentFileName("test.pdf").build())
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceOrder)
                                    .stmtOfServiceAddRecipient(Arrays.asList(element(recipient)))
                                    .servedOrderIds(Arrays.asList("order-1", "order-2"))
                                    .build())
            .orderCollection(Arrays.asList(element(
                UUID.fromString(TEST_UUID), OrderDetails.builder()
                    .sosStatus("PENDING")
                    .serveOrderDetails(ServeOrderDetails.builder()
                                           .servedParties(Arrays.asList(element(ServedParties.builder().build())))
                                           .build())
                    .build()
            )))
            .respondents(listOfRespondents)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        when(manageDocumentsService.getLoggedInUserType(anyString())).thenReturn(List.of(PrlAppsConstants.COURT_ADMIN_ROLE));

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull("servedOrderIds should be populated", result.get("servedOrderIds"));

        @SuppressWarnings("unchecked")
        List<String> servedOrderIds = (List<String>) result.get("servedOrderIds");

        assertEquals("Should have 3 served order IDs (1 new + 2 existing)", 3, servedOrderIds.size());
        assertEquals("First order ID should be new order-3", "order-3", servedOrderIds.get(0));
        assertEquals("Second order ID should be existing order-1", "order-1", servedOrderIds.get(1));
        assertEquals("Third order ID should be existing order-2", "order-2", servedOrderIds.get(2));
    }

    @Test
    public void testHandleSosForOrders_ServedOrderIdsEmptyWhenNoOrdersSelected() {
        StmtOfServiceAddRecipient recipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(DynamicList.builder()
                                       .value(DynamicListElement.builder().code(TEST_UUID).label("Test Recipient").build())
                                       .build())
            .orderList(null)
            .stmtOfServiceDocument(Document.builder().documentFileName("test.pdf").build())
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceOrder)
                                    .stmtOfServiceAddRecipient(Arrays.asList(element(recipient)))
                                    .build())
            .orderCollection(Arrays.asList(element(
                UUID.fromString(TEST_UUID), OrderDetails.builder()
                    .sosStatus("PENDING")
                    .serveOrderDetails(ServeOrderDetails.builder()
                                           .servedParties(Arrays.asList(element(ServedParties.builder().build())))
                                           .build())
                    .build()
            )))
            .respondents(listOfRespondents)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        when(manageDocumentsService.getLoggedInUserType(anyString())).thenReturn(List.of(PrlAppsConstants.COURT_ADMIN_ROLE));

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull("servedOrderIds should be populated", result.get("servedOrderIds"));

        @SuppressWarnings("unchecked")
        List<String> servedOrderIds = (List<String>) result.get("servedOrderIds");

        assertEquals("Should have 0 served order IDs when no orders selected", 0, servedOrderIds.size());
    }

    @Test
    public void testHandleSosForOrders_SelectedOrderIdsPreservedInRecipient() {
        DynamicMultiSelectList orderList = DynamicMultiSelectList.builder()
            .value(Arrays.asList(
                DynamicMultiselectListElement.builder().code("order-1").label("Test Order 1").build(),
                DynamicMultiselectListElement.builder().code("order-2").label("Test Order 2").build()
            ))
            .build();

        StmtOfServiceAddRecipient recipient = StmtOfServiceAddRecipient.builder()
            .respondentDynamicList(DynamicList.builder()
                                       .value(DynamicListElement.builder().code(TEST_UUID).label("Test Recipient").build())
                                       .build())
            .orderList(orderList)
            .stmtOfServiceDocument(Document.builder().documentFileName("test.pdf").build())
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceWhatWasServed(StatementOfServiceWhatWasServed.statementOfServiceOrder)
                                    .stmtOfServiceAddRecipient(Arrays.asList(element(recipient)))
                                    .build())
            .orderCollection(Arrays.asList(element(
                UUID.fromString(TEST_UUID), OrderDetails.builder()
                    .sosStatus("PENDING")
                    .serveOrderDetails(ServeOrderDetails.builder()
                                           .servedParties(Arrays.asList(element(ServedParties.builder().build())))
                                           .build())
                    .build()
            )))
            .respondents(listOfRespondents)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        when(manageDocumentsService.getLoggedInUserType(anyString())).thenReturn(List.of(PrlAppsConstants.COURT_ADMIN_ROLE));

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.handleSosAboutToSubmit(caseDetails, authToken);

        assertNotNull("stmtOfServiceForOrder should be populated", result.get("stmtOfServiceForOrder"));

        @SuppressWarnings("unchecked")
        List<Element<StmtOfServiceAddRecipient>> processedRecipients =
            (List<Element<StmtOfServiceAddRecipient>>) result.get("stmtOfServiceForOrder");

        assertFalse("Processed recipients list should not be empty", processedRecipients.isEmpty());

        StmtOfServiceAddRecipient processedRecipient = processedRecipients.get(0).getValue();

        assertNull(
            "orderList should be cleared to prevent CCD validation error",
            processedRecipient.getOrderList()
        );

        assertNotNull(
            "selectedOrderIds should be populated for XUI display",
            processedRecipient.getSelectedOrderIds()
        );

        assertEquals(
            "Should have 2 selected order IDs",
            2,
            processedRecipient.getSelectedOrderIds().size()
        );

        assertEquals(
            "First order ID should be order-1",
            "order-1",
            processedRecipient.getSelectedOrderIds().get(0)
        );

        assertEquals(
            "Second order ID should be order-2",
            "order-2",
            processedRecipient.getSelectedOrderIds().get(1)
        );
    }

    @Test
    public void testRetrieveRespondentsList_PopulatesOrderListWithMultipleOrders() {
        UUID order1Id = UUID.randomUUID();
        UUID order2Id = UUID.randomUUID();
        UUID order3Id = UUID.randomUUID();

        List<Element<OrderDetails>> orderCollection = Arrays.asList(
            element(order1Id, OrderDetails.builder()
                .orderType("Care Order")
                .orderTypeId("careOrder")
                .dateCreated(LocalDateTime.now())
                .build()),
            element(order2Id, OrderDetails.builder()
                .orderType("Supervision Order")
                .orderTypeId("supervisionOrder")
                .dateCreated(LocalDateTime.now())
                .build()),
            element(order3Id, OrderDetails.builder()
                .orderType("Emergency Protection Order")
                .orderTypeId("emergencyProtectionOrder")
                .dateCreated(LocalDateTime.now())
                .build())
        );

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(listOfRespondents)
            .orderCollection(orderCollection)
            .build();

        DynamicMultiSelectList expectedOrderList = DynamicMultiSelectList.builder()
            .listItems(Arrays.asList(
                DynamicMultiselectListElement.builder().code(order1Id.toString()).label("Care Order").build(),
                DynamicMultiselectListElement.builder().code(order2Id.toString()).label("Supervision Order").build(),
                DynamicMultiselectListElement.builder().code(order3Id.toString()).label("Emergency Protection Order").build()
            ))
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData))
            .thenReturn(expectedOrderList);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.retrieveRespondentsList(caseDetails);

        assertNotNull("Result should not be null", result);
        assertNotNull("stmtOfServiceAddRecipient should be populated", result.get("stmtOfServiceAddRecipient"));

        @SuppressWarnings("unchecked")
        List<Element<StmtOfServiceAddRecipient>> recipients =
            (List<Element<StmtOfServiceAddRecipient>>) result.get("stmtOfServiceAddRecipient");

        assertFalse("Recipients list should not be empty", recipients.isEmpty());

        StmtOfServiceAddRecipient recipient = recipients.get(0).getValue();
        assertNotNull("Order list should be populated", recipient.getOrderList());
        assertEquals("Should have 3 orders in the list", 3, recipient.getOrderList().getListItems().size());
        assertEquals("First order code should match", order1Id.toString(),
            recipient.getOrderList().getListItems().get(0).getCode());
    }

    @Test
    public void testRetrieveRespondentsList_PopulatesEmptyOrderListWhenNoOrders() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(listOfRespondents)
            .orderCollection(new ArrayList<>())
            .build();

        DynamicMultiSelectList emptyOrderList = DynamicMultiSelectList.builder()
            .listItems(new ArrayList<>())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData))
            .thenReturn(emptyOrderList);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.retrieveRespondentsList(caseDetails);

        @SuppressWarnings("unchecked")
        List<Element<StmtOfServiceAddRecipient>> recipients =
            (List<Element<StmtOfServiceAddRecipient>>) result.get("stmtOfServiceAddRecipient");

        StmtOfServiceAddRecipient recipient = recipients.get(0).getValue();
        assertNotNull("Order list should not be null even when empty", recipient.getOrderList());
        assertEquals("Should have 0 orders when case has no orders", 0,
            recipient.getOrderList().getListItems().size());
    }

    @Test
    public void testRetrieveRespondentsList_VerifyDynamicMultiSelectListServiceCalled() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(listOfRespondents)
            .orderCollection(new ArrayList<>())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData))
            .thenReturn(DynamicMultiSelectList.builder().listItems(new ArrayList<>()).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        stmtOfServImplService.retrieveRespondentsList(caseDetails);

        verify(dynamicMultiSelectListService, times(1)).getOrdersAsDynamicMultiSelectList(caseData);
    }

    @Test
    public void testRetrieveRespondentsList_IncludesApplicantsAndRespondentsForC100() {
        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("ApplicantFirst1")
            .lastName("ApplicantLast1")
            .partyId(UUID.randomUUID())
            .build();

        PartyDetails applicant2 = PartyDetails.builder()
            .firstName("ApplicantFirst2")
            .lastName("ApplicantLast2")
            .partyId(UUID.randomUUID())
            .build();

        List<Element<PartyDetails>> applicants = Arrays.asList(
            element(UUID.randomUUID(), applicant1),
            element(UUID.randomUUID(), applicant2)
        );

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicants)
            .respondents(listOfRespondents)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData))
            .thenReturn(DynamicMultiSelectList.builder().listItems(new ArrayList<>()).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.retrieveRespondentsList(caseDetails);

        @SuppressWarnings("unchecked")
        List<Element<StmtOfServiceAddRecipient>> recipients =
            (List<Element<StmtOfServiceAddRecipient>>) result.get("stmtOfServiceAddRecipient");

        assertNotNull("Recipients should not be null", recipients);
        assertFalse("Recipients list should not be empty", recipients.isEmpty());

        StmtOfServiceAddRecipient recipient = recipients.get(0).getValue();
        DynamicList respondentDynamicList = recipient.getRespondentDynamicList();

        assertNotNull("Respondent dynamic list should not be null", respondentDynamicList);
        List<DynamicListElement> listItems = respondentDynamicList.getListItems();

        assertEquals("Should have 5 items (2 applicants + 2 respondents + All Respondents)",
            5, listItems.size());

        assertEquals("First item should be applicant 1",
            applicant1.getLabelForDynamicList() + " (Applicant 1)", listItems.get(0).getLabel());
        assertEquals("Second item should be applicant 2",
            applicant2.getLabelForDynamicList() + " (Applicant 2)", listItems.get(1).getLabel());

        assertEquals("Third item should be respondent 1",
            respondent.getLabelForDynamicList() + " (Respondent 1)", listItems.get(2).getLabel());
        assertEquals("Fourth item should be respondent 2",
            listOfRespondents.get(1).getValue().getLabelForDynamicList() + " (Respondent 2)",
            listItems.get(3).getLabel());

        assertEquals("Last item should be All Respondents",
            ALL_RESPONDENTS, listItems.get(4).getLabel());
    }

    @Test
    public void testRetrieveRespondentsList_IncludesApplicantAndRespondentForFL401() {
        PartyDetails applicantFL401 = PartyDetails.builder()
            .firstName("ApplicantFirst")
            .lastName("ApplicantLast")
            .partyId(UUID.randomUUID())
            .build();

        PartyDetails respondentFL401 = PartyDetails.builder()
            .firstName("RespondentFirst")
            .lastName("RespondentLast")
            .partyId(UUID.randomUUID())
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(applicantFL401)
            .respondentsFL401(respondentFL401)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData))
            .thenReturn(DynamicMultiSelectList.builder().listItems(new ArrayList<>()).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.retrieveRespondentsList(caseDetails);

        @SuppressWarnings("unchecked")
        List<Element<StmtOfServiceAddRecipient>> recipients =
            (List<Element<StmtOfServiceAddRecipient>>) result.get("stmtOfServiceAddRecipient");

        assertNotNull("Recipients should not be null", recipients);
        assertFalse("Recipients list should not be empty", recipients.isEmpty());

        StmtOfServiceAddRecipient recipient = recipients.get(0).getValue();
        DynamicList respondentDynamicList = recipient.getRespondentDynamicList();

        assertNotNull("Respondent dynamic list should not be null", respondentDynamicList);
        List<DynamicListElement> listItems = respondentDynamicList.getListItems();

        assertEquals("Should have 2 items (1 applicant + 1 respondent)", 2, listItems.size());

        assertEquals("First item should be applicant",
            applicantFL401.getLabelForDynamicList() + " (Applicant)", listItems.get(0).getLabel());
        assertEquals("First item code should match applicant ID",
            applicantFL401.getPartyId().toString(), listItems.get(0).getCode());

        assertEquals("Second item should be respondent",
            respondentFL401.getLabelForDynamicList() + " (Respondent)", listItems.get(1).getLabel());
        assertEquals("Second item code should match respondent ID",
            respondentFL401.getPartyId().toString(), listItems.get(1).getCode());
    }

    @Test
    public void testRetrieveRespondentsList_HandlesNullApplicantsForC100() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(null)
            .respondents(listOfRespondents)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData))
            .thenReturn(DynamicMultiSelectList.builder().listItems(new ArrayList<>()).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.retrieveRespondentsList(caseDetails);

        @SuppressWarnings("unchecked")
        List<Element<StmtOfServiceAddRecipient>> recipients =
            (List<Element<StmtOfServiceAddRecipient>>) result.get("stmtOfServiceAddRecipient");

        StmtOfServiceAddRecipient recipient = recipients.get(0).getValue();
        DynamicList respondentDynamicList = recipient.getRespondentDynamicList();

        assertNotNull("Respondent dynamic list should not be null", respondentDynamicList);
        List<DynamicListElement> listItems = respondentDynamicList.getListItems();

        assertEquals("Should have 3 items (2 respondents + All Respondents)", 3, listItems.size());
    }

    @Test
    public void testRetrieveRespondentsList_HandlesNullApplicantForFL401() {
        PartyDetails respondentFL401 = PartyDetails.builder()
            .firstName("RespondentFirst")
            .lastName("RespondentLast")
            .partyId(UUID.randomUUID())
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(null)
            .respondentsFL401(respondentFL401)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData))
            .thenReturn(DynamicMultiSelectList.builder().listItems(new ArrayList<>()).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(stringObjectMap)
            .build();

        Map<String, Object> result = stmtOfServImplService.retrieveRespondentsList(caseDetails);

        @SuppressWarnings("unchecked")
        List<Element<StmtOfServiceAddRecipient>> recipients =
            (List<Element<StmtOfServiceAddRecipient>>) result.get("stmtOfServiceAddRecipient");

        StmtOfServiceAddRecipient recipient = recipients.get(0).getValue();
        DynamicList respondentDynamicList = recipient.getRespondentDynamicList();

        assertNotNull("Respondent dynamic list should not be null", respondentDynamicList);
        List<DynamicListElement> listItems = respondentDynamicList.getListItems();

        assertEquals("Should have 1 item (respondent only)", 1, listItems.size());
        assertEquals("Item should be respondent",
            respondentFL401.getLabelForDynamicList() + " (Respondent)", listItems.get(0).getLabel());
    }
}
