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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
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
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StatementOfService;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALL_RESPONDENTS;

@RunWith(MockitoJUnitRunner.Silent.class)
public class StmtOfServImplServiceTest {

    @InjectMocks
    private StmtOfServImplService stmtOfServImplService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

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

        CaseData updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

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

        CaseData updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

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

        CaseData updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

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

        CaseData updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

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

        CaseData updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

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

        CaseData updatedCaseData = stmtOfServImplService.retrieveAllRespondentNames(caseDetails, authToken);

        assertNotNull(updatedCaseData);

    }
}
