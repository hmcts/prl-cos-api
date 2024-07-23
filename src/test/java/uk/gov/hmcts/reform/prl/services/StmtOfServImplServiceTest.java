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
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.StatementOfServiceWhatWasServed;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StatementOfService;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER;
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
    private AllTabServiceImpl allTabService;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    private DynamicList dynamicList;
    private PartyDetails respondent;
    private Element<PartyDetails> wrappedRespondents;
    private List<Element<PartyDetails>> listOfRespondents;
    private UUID uuid;
    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";
    private LocalDateTime testDate = LocalDateTime.of(2024, 04, 28, 1, 0);

    public static final String authToken = "Bearer TestAuthToken";

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
    public void testToAddRespondentsAsDynamicListForC100() {

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

        Map<String, Object> updatedCaseData = stmtOfServImplService.retrieveRespondentsList(caseDetails);
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.get("stmtOfServiceAddRecipient"));

    }

    @Test
    public void testToRetrieveAllRespondentNamesForC100WhileServingApplicationPack() {

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

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
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

        Map<String, Object> updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

        assertNotNull(updatedCaseData);

    }

    @Test
    public void testToRetrieveAllRespondentNamesForC100WhileServingOrder() {

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
            .caseTypeOfApplication("C100")
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

        Map<String, Object> updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

        assertNotNull(updatedCaseData);

    }

    @Test
    public void testToRetrieveAllRespondentNamesForFL401WhileServingApplicationPack() {

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
            .caseTypeOfApplication("FL401")
            .respondentsFL401(PartyDetails.builder()
                                  .firstName("testFl401")
                                  .lastName("lastFl401")
                                  .build())
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                         .courtBailiff.toString())
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

        Map<String, Object> updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

        assertNotNull(updatedCaseData);

    }

    @Test
    public void testToRetrieveAllRespondentNamesForFL401WhileServingOrder() {

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

        Map<String, Object> updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

        assertNotNull(updatedCaseData);

    }

    @Test
    public void testToRetrieveAllRespondentNamesForC100Scenario2WhileServingApplicationPack() {

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
            .caseTypeOfApplication("C100")
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

        Map<String, Object> updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

        assertNotNull(updatedCaseData);

    }

    @Test
    public void testToRetrieveAllRespondentNamesForC100Scenario2WhileServingOrder() {

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

        Map<String, Object> updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

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
                                                                  .build())
                                      .build())
            .statementOfService(StatementOfService.builder()

                                    .build())
            .respondents(List.of(element(UUID.fromString(TEST_UUID),PartyDetails.builder().build())))
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
            .thenReturn(Document.builder().build());
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        stmtOfServImplService.saveCitizenSos("","", authToken, CitizenSos.builder()
                .partiesServed(List.of("123", "234", "1234"))
                .partiesServedDate("2020-08-01")
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
            .thenReturn(Document.builder().build());
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        stmtOfServImplService.saveCitizenSos("","", authToken, CitizenSos.builder()
            .partiesServed(List.of("123", "234", "1234"))
            .isOrder(YesOrNo.No)
            .partiesServedDate("2020-08-01")
            .citizenSosDocs(Document.builder().documentFileName("test").build())
            .build());
        verify(allTabService, times(1))
            .submitAllTabsUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.any(),Mockito.any(),Mockito.any());
    }
}
