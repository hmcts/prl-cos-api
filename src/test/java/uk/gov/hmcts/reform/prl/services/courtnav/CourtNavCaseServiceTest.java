package uk.gov.hmcts.reform.prl.services.courtnav;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import uk.gov.hmcts.reform.prl.clients.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CourtNavCaseServiceTest {

    private final String authToken = "Bearer abc";
    private final String s2sToken = "s2s token";
    private final String randomUserId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
    private static final String randomAlphaNumeric = "Abc123EFGH";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private IdamClient idamClient;

    @Mock
    private CaseUtils caseUtils;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private DocumentGenService documentGenService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    SystemUserService systemUserService;

    @Mock
    CoreCaseDataService coreCaseDataService;

    @InjectMocks
    CourtNavCaseService courtNavCaseService;

    @Mock
    private AllTabServiceImpl allTabService;

    private CaseData caseData;

    public MultipartFile file;
    private final String userToken = "Bearer testToken";
    private final String systemUpdateUserId = "systemUserID";

    private EventRequestData eventRequestData;

    private String bearerToken;

    @Before
    public void setup() {
        caseData = CaseData.builder().id(1234567891234567L).applicantCaseName("xyz").build();
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(randomUserId).build());
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(coreCaseDataApi.startForCaseworker(any(), any(), any(), any(), any(), any())
        ).thenReturn(StartEventResponse.builder().eventId("courtnav-case-creation").token("eventToken").build());
        file = new MockMultipartFile(
            "file",
            "private-law.pdf",
            MediaType.TEXT_PLAIN_VALUE,
            "FL401 case".getBytes()
        );
    }

    @Test
    public void shouldStartAndSubmitEventWithEventData() throws Exception {
        Map<String, Object> tempMap = new HashMap<>();
        courtNavCaseService.createCourtNavCase("Bearer abc", caseData);
        verify(coreCaseDataApi).startForCaseworker(authToken, s2sToken,
                                                   randomUserId, PrlAppsConstants.JURISDICTION,
                                                   PrlAppsConstants.CASE_TYPE, "courtnav-case-creation"
        );
    }

    @Test
    public void shouldUploadDocumentWhenAllFieldsAreCorrect() {
        uk.gov.hmcts.reform.prl.models.documents.Document tempDoc = uk.gov.hmcts.reform.prl.models.documents
            .Document.builder()
            .documentFileName("private-law.pdf")
            .documentUrl(randomAlphaNumeric)
            .documentBinaryUrl(randomAlphaNumeric)
            .build();

        Document document = testDocument();
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken("eventToken")
            .event(Event.builder()
                       .id("courtnav-document-upload")
                       .build())
            .data(Map.of("WITNESS_STATEMENT", tempDoc))
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails tempCaseDetails = CaseDetails.builder().id(1234567891234567L).data(stringObjectMap)
            .state("SUBMITTED_PAID").createdDate(
            LocalDateTime.now()).lastModified(LocalDateTime.now()).id(1234567891234567L).build();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));

        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUpdateUserId);

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(bearerToken).build();
        when(coreCaseDataService.startUpdate(
            authToken,eventRequestData, String.valueOf(caseData.getId()),true))
            .thenReturn(startEventResponse);
        when(objectMapper.convertValue(tempCaseDetails.getData(), CaseData.class)).thenReturn(caseData);

        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
        when(coreCaseDataService.createCaseDataContent(startEventResponse,caseDataUpdated)).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(authToken, eventRequestData, caseDataContent,String.valueOf(caseData.getId()), true))
            .thenReturn(caseDetails);


        when(coreCaseDataApi.getCase(authToken, s2sToken, "1234567891234567")).thenReturn(tempCaseDetails);
        when(caseDocumentClient.uploadDocuments(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any())).thenReturn(uploadResponse);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(idamClient.getUserInfo(Mockito.any())).thenReturn(UserInfo.builder().uid(randomUserId).build());

        courtNavCaseService.uploadDocument("Bearer abc", file, "WITNESS_STATEMENT",
                                           "1234567891234567"
        );

        verify(coreCaseDataService, Mockito.times(1)).startUpdate(authToken,
                                                                  eventRequestData,
                                                                  String.valueOf(caseData.getId()),
                                                                  true);
        verify(coreCaseDataService, Mockito.times(1)).submitUpdate(authToken,
                                                                   eventRequestData,
                                                                   caseDataContent,
                                                                   String.valueOf(caseData.getId()),
                                                                   true);
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocumentWhenInvalidDocumentTypeOfDocumentIsRequested() {
        CaseEvent caseEvent = CaseEvent.COURTNAV_DOCUMENT_UPLOAD_EVENT_ID;
        eventRequestData = EventRequestData.builder()
            .eventId(caseEvent.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
        when(coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId)).thenReturn(eventRequestData);

        courtNavCaseService.uploadDocument("Bearer abc", file, "InvalidTypeOfDocument",
                                           "1234567891234567"
        );
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocumentWhenInvalidDocumentFormatIsRequested() {
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUpdateUserId);
        courtNavCaseService.uploadDocument("Bearer abc", file, "InvalidTypeOfDocument",
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
        when(documentGenService.generateDocuments(authToken, caseData)).thenReturn(stringObjectMap);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        doNothing().when(allTabService).updateAllTabsIncludingConfTab(caseData);

        courtNavCaseService.refreshTabs(authToken,stringObjectMap, 1234567891234567L);
        verify(documentGenService, times(1))
            .generateDocuments(authToken,
                               caseData);
        verify(allTabService, times(1))
            .updateAllTabsIncludingConfTab(caseData);

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
