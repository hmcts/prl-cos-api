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
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
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

    @InjectMocks
    CourtNavCaseService courtNavCaseService;

    @Mock
    CcdCoreCaseDataService ccdCoreCaseDataService;

    @Mock
    private AllTabServiceImpl allTabService;

    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    @Mock
    SystemUserService systemUserService;


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
        Document document = testDocument();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(eventToken)
            .event(Event.builder()
                       .id("courtnav-document-upload")
                       .build())
            .data(caseData.toMap(objectMapper))
            .build();
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
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), stringObjectMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        courtNavCaseService.refreshTabs(authToken,"1234567891234567");
        verify(documentGenService, times(1))
            .generateDocuments(Mockito.anyString(),
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
