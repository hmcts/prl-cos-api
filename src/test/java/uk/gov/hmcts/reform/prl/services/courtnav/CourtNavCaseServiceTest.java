package uk.gov.hmcts.reform.prl.services.courtnav;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURTNAV;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(SpringRunner.class)
@EnableRetry
@SpringBootTest(classes = {CourtNavCaseService.class})
public class CourtNavCaseServiceTest {

    private final String authToken = "Bearer abc";
    private static final String systemUpdateUser = "system User";
    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "PRLAPPS";
    private final String eventName = "system-update";
    private final String systemUserId = "systemUserID";
    private final String eventToken = "eventToken";
    private final String s2sToken = "s2s token";
    private final String randomUserId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
    private static final String randomAlphaNumeric = "A1b2c3EFGH";

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private IdamClient idamClient;

    @Mock
    private CaseUtils caseUtils;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CaseDocumentClient caseDocumentClient;

    @MockBean
    private DocumentGenService documentGenService;

    @MockBean
    private ObjectMapper objectMapper;

    @Autowired
    CourtNavCaseService courtNavCaseService;

    @MockBean
    CcdCoreCaseDataService ccdCoreCaseDataService;

    @MockBean
    private AllTabServiceImpl allTabService;

    @MockBean
    private ManageDocumentsService manageDocumentsService;

    @MockBean
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    @MockBean
    private NoticeOfChangePartiesService noticeOfChangePartiesService;

    private Map<String, Object> caseDataMap = new HashMap<>();
    private CaseData caseData;
    public MultipartFile file;
    private StartEventResponse startEventResponse;
    private CaseDetails caseDetails;

    @Before
    public void setup() {
        caseDataMap.put("id", "1234567891234567");
        caseDataMap.put("applicantCaseName", "xyz");
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(123L)
            .state("SUBMITTED_PAID")
            .build();
        caseData = CaseData.builder().id(1234567891234567L).applicantCaseName("xyz").documentManagementDetails(
            DocumentManagementDetails.builder().build()).build();
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(randomUserId).build());
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        file = new MockMultipartFile(
            "file",
            "private-law.pdf",
            MediaType.TEXT_PLAIN_VALUE,
            "FL401 case".getBytes()
        );
        startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();
        when(ccdCoreCaseDataService.startUpdate(Mockito.anyString(),Mockito.any(),Mockito.anyString(),Mockito.anyBoolean()))
            .thenReturn(
                startEventResponse);
        when(ccdCoreCaseDataService.startSubmitCreate(authToken, s2sToken, null, true)).thenReturn(
            startEventResponse);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper)).thenReturn(caseData);
    }

    @Test
    public void shouldStartAndSubmitEventWithEventData() {
        courtNavCaseService.createCourtNavCase("Bearer abc", caseData);
        verify(ccdCoreCaseDataService).submitCreate(Mockito.anyString(), Mockito.anyString(),
                                                    Mockito.anyString(),
                                                    Mockito.any(CaseDataContent.class), Mockito.anyBoolean());
    }

    @Test
    public void shouldUploadDocumentWhenAllFieldsAreCorrect() {
        QuarantineLegalDoc courtNavDocument = QuarantineLegalDoc.builder().build();
        List<Element<QuarantineLegalDoc>> courtNavUploadedDocListDocs =  new ArrayList<>();
        courtNavUploadedDocListDocs.add(element(courtNavDocument));
        QuarantineLegalDoc courtNavRestrictedDocument = QuarantineLegalDoc.builder().uploadedBy(COURTNAV).build();
        List<Element<QuarantineLegalDoc>> courtNavUploadedRestrictedDocsList =  new ArrayList<>();
        courtNavUploadedRestrictedDocsList.add(element(courtNavRestrictedDocument));
        ReviewDocuments reviewDocuments = ReviewDocuments.builder()
            .courtNavUploadedDocListDocTab(courtNavUploadedDocListDocs)
            .restrictedDocuments(courtNavUploadedRestrictedDocsList)
            .build();

        caseData = caseData.toBuilder().reviewDocuments(reviewDocuments).build();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(eventToken)
            .event(Event.builder()
                       .id("courtnav-document-upload")
                       .build())
            .data(caseData.toMap(objectMapper))
            .build();
        Document document = testDocument();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1234567891234567")).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                     Mockito.any(), Mockito.any(), Mockito.any())
        ).thenReturn(StartEventResponse.builder().eventId("courtnav-document-upload").token(eventToken).build());
        when(caseDocumentClient.uploadDocuments(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any())).thenReturn(uploadResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(
            authToken,
            s2sToken,
            randomUserId, PrlAppsConstants.JURISDICTION,
            PrlAppsConstants.CASE_TYPE,
            "1234567891234567",
            true,
            caseDataContent
             )
        ).thenReturn(CaseDetails.builder().id(1234567891234567L).data(Map.of(
            "typeOfDocument",
            "fl401Doc1"
        )).build());
        when(ccdCoreCaseDataService.startUpdate(authToken, null, "1234567891234567", true))
            .thenReturn(startEventResponse);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        courtNavCaseService.uploadDocument("Bearer abc", file, "WITNESS_STATEMENT",
                                           "1234567891234567"
        );
        verify(caseDocumentClient, times(1)).uploadDocuments(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyList()
        );
        verify(ccdCoreCaseDataService, times(1)).submitUpdate(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.anyBoolean()
        );
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocumentWhenInvalidDocumentTypeOfDocumentIsRequested() {
        courtNavCaseService.uploadDocument("Bearer abc", file, "InvalidTypeOfDocument",
                                           "1234567891234567"
        );
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocumentWhenInvalidDocumentFormatIsRequested() {
        courtNavCaseService.uploadDocument("Bearer abc", file, "InvalidTypeOfDocument",
                                           "1234567891234567"
        );
    }

    @Test
    public void shouldRetry3TimesOnCcdConflictException() {
        QuarantineLegalDoc courtNavDocument = QuarantineLegalDoc.builder().build();
        List<Element<QuarantineLegalDoc>> courtNavUploadedDocListDocs =  new ArrayList<>();
        courtNavUploadedDocListDocs.add(element(courtNavDocument));
        ReviewDocuments reviewDocuments = ReviewDocuments.builder()
            .courtNavUploadedDocListDocTab(courtNavUploadedDocListDocs)
            .build();

        caseData = caseData.toBuilder().reviewDocuments(reviewDocuments).build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        Document document = testDocument();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1234567891234567")).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                     Mockito.any(), Mockito.any(), Mockito.any())
        ).thenReturn(StartEventResponse.builder().eventId("courtnav-document-upload").token(eventToken).build());

        when(caseDocumentClient.uploadDocuments(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any())).thenReturn(uploadResponse);

        when(ccdCoreCaseDataService.submitUpdate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
            .thenThrow(new FeignException.Conflict("conflict", mock(Request.class), null, null));

        try {
            courtNavCaseService.uploadDocument("Bearer abc", file, "WITNESS_STATEMENT", "1234567891234567");
        } catch (FeignException.Conflict ex) {
            // expected behaviour after 3 retries, it'll still conflict
        }
        // Verify we start & submit an event 3 times - as we have to restart the whole transaction again on conflict
        verify(ccdCoreCaseDataService, times(3))
            .startUpdate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        verify(ccdCoreCaseDataService, times(3))
            .submitUpdate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean());
    }

    @Test
    public void shouldNotRetryOnAGatewayTimeout() {
        QuarantineLegalDoc courtNavDocument = QuarantineLegalDoc.builder().build();
        List<Element<QuarantineLegalDoc>> courtNavUploadedDocListDocs =  new ArrayList<>();
        courtNavUploadedDocListDocs.add(element(courtNavDocument));
        ReviewDocuments reviewDocuments = ReviewDocuments.builder()
            .courtNavUploadedDocListDocTab(courtNavUploadedDocListDocs)
            .build();

        caseData = caseData.toBuilder().reviewDocuments(reviewDocuments).build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        Document document = testDocument();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1234567891234567")).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                     Mockito.any(), Mockito.any(), Mockito.any())
        ).thenReturn(StartEventResponse.builder().eventId("courtnav-document-upload").token(eventToken).build());

        when(caseDocumentClient.uploadDocuments(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any())).thenReturn(uploadResponse);

        when(ccdCoreCaseDataService.submitUpdate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
            .thenThrow(new FeignException.GatewayTimeout("conflict", mock(Request.class), null, null));

        assertThrows(FeignException.GatewayTimeout.class,
                     () -> courtNavCaseService.uploadDocument("Bearer abc", file, "WITNESS_STATEMENT", "1234567891234567"));
    }


    @Test(expected = ResponseStatusException.class)
    public void shouldThrowExceptionForNumberOfAttchmentsSize() {
        List<Element<UploadedDocuments>> courtNavUploadedDocs = new ArrayList<>();
        UploadedDocuments uploadedDocuments = UploadedDocuments.builder().build();
        courtNavUploadedDocs.add(element(uploadedDocuments));
        QuarantineLegalDoc courtNavDocument = QuarantineLegalDoc.builder().build();
        List<Element<QuarantineLegalDoc>> courtNavUploadedDocListDocs =  new ArrayList<>();
        courtNavUploadedDocListDocs.add(element(courtNavDocument));
        QuarantineLegalDoc courtNavRestrictedDocument = QuarantineLegalDoc.builder().uploadedBy(COURTNAV).build();
        List<Element<QuarantineLegalDoc>> courtNavUploadedRestrictedDocsList =  new ArrayList<>();
        courtNavUploadedRestrictedDocsList.add(element(courtNavRestrictedDocument));
        ReviewDocuments reviewDocuments = ReviewDocuments.builder()
            .courtNavUploadedDocListDocTab(courtNavUploadedDocListDocs)
            .restrictedDocuments(courtNavUploadedRestrictedDocsList)
            .build();

        caseData = CaseData.builder().id(1234567891234567L).applicantCaseName("xyz").documentManagementDetails(
                DocumentManagementDetails.builder().build())
            .numberOfAttachments("2").reviewDocuments(reviewDocuments).build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        courtNavCaseService.uploadDocument("Bearer abc", file, "WITNESS_STATEMENT",
                                           "1234567891234567"
        );
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldThrowExceptionWhenInvalidTypeOfDocumentIsPassed() {
        file = new MockMultipartFile(
            "file",
            "private-law.json",
            MediaType.TEXT_PLAIN_VALUE,
            "FL401 case".getBytes()
        );
        courtNavCaseService.uploadDocument("Bearer abc", file, "WITNESS_STATEMENT",
                                           "1234567891234567"
        );
    }

    @Test
    public void testRefreshTabs() throws Exception {
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(documentGenService.createUpdatedCaseDataWithDocuments(authToken, caseData)).thenReturn(stringObjectMap);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), stringObjectMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        courtNavCaseService.refreshTabs(authToken,"1234567891234567");
        verify(documentGenService, times(1))
            .createUpdatedCaseDataWithDocuments(Mockito.anyString(),
                               Mockito.any(CaseData.class));
    }

    public static Document testDocument() {
        Document.Link binaryLink = new Document.Link();
        binaryLink.href = randomAlphaNumeric;
        Document.Link selfLink = new Document.Link();
        selfLink.href = randomAlphaNumeric;

        Document.Links links = new Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        Document document = Document.builder().build();
        document.links = links;
        document.originalDocumentName = randomAlphaNumeric;

        return document;
    }

}
