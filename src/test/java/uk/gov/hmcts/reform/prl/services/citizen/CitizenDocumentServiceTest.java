package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentRequest;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.notifications.NotificationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CitizenDocumentServiceTest {

    public static final String authToken = "Bearer TestAuthToken";

    @InjectMocks
    private CitizenDocumentService citizenDocumentService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    private UserService userService;

    @Mock
    private ManageDocumentsService manageDocumentsService;

    @Mock
    private NotificationService notificationService;

    private DocumentRequest documentRequest;
    private Document caseDoc;
    private QuarantineLegalDoc quarantineCaseDoc;

    @Before
    public void setUp() {

        ReflectionTestUtils.setField(manageDocumentsService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(manageDocumentsService, "notificationService", notificationService);

        doCallRealMethod().when(manageDocumentsService).moveDocumentsToQuarantineTab(any(), any(), any(), any());
        doCallRealMethod().when(manageDocumentsService).moveDocumentsToRespectiveCategoriesNew(
            any(),
            any(),
            any(),
            any(),
            any()
        );
        doCallRealMethod().when(manageDocumentsService).getRestrictedOrConfidentialKey(any());
        doCallRealMethod().when(manageDocumentsService).getQuarantineDocumentForUploader(any(), any());
        doCallRealMethod().when(manageDocumentsService).moveToConfidentialOrRestricted(any(), any(), any(), any());

        documentRequest = DocumentRequest.builder()
            .caseId("123")
            .categoryId("POSITION_STATEMENTS")
            .partyId("00000000-0000-0000-0000-000000000000")
            .partyName("appf appl")
            .partyType("applicant")
            .restrictDocumentDetails("test details")
            .freeTextStatements("free text to generate document")
            .build();

        caseDoc = Document.builder()
            .documentFileName("test.pdf")
            .documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15")
            .documentBinaryUrl(null)
            .documentHash(null)
            .categoryId(null)
            .documentCreatedOn(Date.from(ZonedDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).toInstant()))
            .build();

        quarantineCaseDoc = QuarantineLegalDoc.builder()
            .categoryId("positionStatements")
            .positionStatementsDocument(caseDoc)
            .build();
    }

    @Test
    public void testCitizenUploadDocumentsAndMoveToQuarantine() throws Exception {
        //Given
        documentRequest = documentRequest.toBuilder()
            .isConfidential(Yes)
            .isRestricted(Yes)
            .restrictDocumentDetails("test")
            .documents(List.of(Document.builder().build())).build();

        CaseData caseData = CaseData.builder()
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .reviewDocuments(ReviewDocuments.builder().build())
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .build();

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(new ObjectMapper()))
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContents = new StartAllTabsUpdateDataContent(authToken,
                                                                                                         EventRequestData.builder().build(),
                                                                                                         StartEventResponse.builder().build(),
                                                                                                         stringObjectMap,
                                                                                                         caseData,
                                                                                                         null
        );

        when(allTabService.getStartUpdateForSpecificEvent("123", CaseEvent.CITIZEN_CASE_UPDATE.getValue())).thenReturn(
            startAllTabsUpdateDataContents);
        //when(caseService.getCase(any(), any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(QuarantineLegalDoc.class))).thenReturn(
            quarantineCaseDoc);
        when((userService.getUserDetails(any()))).thenReturn(UserDetails.builder()
                                                                 .roles(List.of(Roles.CITIZEN.getValue())).build());
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(caseDetails);

        //Action
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsUpdated = citizenDocumentService.citizenSubmitDocuments(
            authToken,
            documentRequest
        );

        assertNotNull(caseDetails);
        assertNotNull(caseDetailsUpdated);
        assertNotNull(caseDetailsUpdated.getData());
    }

    @Test
    public void testCitizenUploadDocumentsAndMoveRespectiveCategory() throws Exception {
        //Given
        documentRequest = documentRequest.toBuilder()
            .categoryId("FM5_STATEMENTS")
            .isConfidential(No)
            .isRestricted(No)
            .restrictDocumentDetails("test")
            .documents(List.of(caseDoc)).build();
        CaseData caseData = CaseData.builder()
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .reviewDocuments(ReviewDocuments.builder().build())
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(new ObjectMapper()))
            .build();

        //When
        //when(caseService.getCase(any(), any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("fm5StatementsDocument", caseDoc);
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc
            .builder()
            .hasTheConfidentialDocumentBeenRenamed(
                YesOrNo.No)
            .isConfidential(null)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                          .documentUrl("00000000-0000-0000-0000-000000000000")
                          .documentFileName("test")
                          .build())
            .isRestricted(null)
            .restrictedDetails(null)
            .categoryId("test")
            .uploaderRole("Citizen")
            .build();
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(QuarantineLegalDoc.class))).thenReturn(
            quarantineLegalDoc);
        when((userService.getUserDetails(any()))).thenReturn(UserDetails.builder()
                                                                 .roles(List.of(Roles.CITIZEN.getValue())).build());
        doNothing().when(notificationService).sendNotifications(any(CaseData.class), any(QuarantineLegalDoc.class), anyString(), );

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContents = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap,
            caseData,
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent("123", CaseEvent.CITIZEN_CASE_UPDATE.getValue())).thenReturn(
            startAllTabsUpdateDataContents);

        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(caseDetails);

        //Action
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsUpdated = citizenDocumentService.citizenSubmitDocuments(
            authToken,
            documentRequest
        );

        //Then
        assertNotNull(caseDetails);
        //CORRECT ASSERTIONS LATER
        assertNotNull(caseDetailsUpdated);
        assertNotNull(caseDetailsUpdated.getData());

    }

}
