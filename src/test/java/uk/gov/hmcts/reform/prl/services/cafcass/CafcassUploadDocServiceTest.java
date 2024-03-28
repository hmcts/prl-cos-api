package uk.gov.hmcts.reform.prl.services.cafcass;

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
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CafcassUploadDocServiceTest {

    private final String authToken = "Bearer abc";
    private final String s2sToken = "s2s token";
    private final String randomUserId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
    private static final String randomAlphaNumeric = "Abc123EFGH";

    @Mock
    private IdamClient idamClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private AllTabServiceImpl allTabService;

    @InjectMocks
    CafcassUploadDocService cafcassUploadDocService;

    private CaseData caseData;

    private MultipartFile file;

    @Before
    public void setup() {
        caseData = CaseData.builder().id(Long.parseLong(TEST_CASE_ID)).applicantCaseName("xyz").build();
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(randomUserId).build());
        when(authTokenGenerator.generate()).thenReturn(s2sToken);

        file = new MockMultipartFile(
            "file",
            "private-law.pdf",
            MediaType.TEXT_PLAIN_VALUE,
            "FL401 case".getBytes()
        );
    }

    @Test
    public void shouldUploadDocumentWhenAllFieldsAreCorrect() throws Exception {

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
                       .id("cafcass-document-upload")
                       .build())
            .data(Map.of("FL401", tempDoc))
            .build();
        CaseDetails tempCaseDetails = CaseDetails.builder().data(Map.of("id", TEST_CASE_ID)).state(
            "SUBMITTED_PAID").createdDate(
            LocalDateTime.now()).lastModified(LocalDateTime.now()).id(Long.valueOf(TEST_CASE_ID)).build();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(tempCaseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                     Mockito.any(), Mockito.any(), Mockito.any())
        ).thenReturn(StartEventResponse.builder().eventId("cafcass-document-upload").token("eventToken").build());
        when(caseDocumentClient.uploadDocuments(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any())).thenReturn(uploadResponse);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(idamClient.getUserInfo(Mockito.any())).thenReturn(UserInfo.builder().uid(randomUserId).build());
        when(coreCaseDataApi.submitEventForCaseWorker(
                 authToken,
                 s2sToken,
                 randomUserId, PrlAppsConstants.JURISDICTION,
                 PrlAppsConstants.CASE_TYPE,
                 TEST_CASE_ID,
                 true,
                 caseDataContent
             )
        ).thenReturn(CaseDetails.builder().id(Long.valueOf(TEST_CASE_ID)).data(Map.of(
            "typeOfDocument",
            "fl401Doc1"
        )).build());

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            Long.valueOf(TEST_CASE_ID)).data(stringObjectMap).build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), stringObjectMap, caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(startAllTabsUpdateDataContent);

        when(objectMapper.convertValue(tempCaseDetails.getData(), CaseData.class)).thenReturn(caseData);
        cafcassUploadDocService.uploadDocument("Bearer abc", file, "16_4_Report",
                                           TEST_CASE_ID
        );
        verify(allTabService, times(1)).submitAllTabsUpdate(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        );
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocumentWhenInvalidDocumentTypeOfDocumentIsRequested() {
        cafcassUploadDocService.uploadDocument("Bearer abc", file, "InvalidTypeOfDocument",
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
        cafcassUploadDocService.uploadDocument("Bearer abc", file, "FL401",
                                           "1234567891234567"
        );
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocumentWhenInvalidCaseId() {
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
                       .id("cafcass-document-upload")
                       .build())
            .data(Map.of("FL401", tempDoc))
            .build();
        CaseDetails tempCaseDetails = CaseDetails.builder().data(Map.of("id", TEST_CASE_ID)).state(
            "SUBMITTED_PAID").createdDate(
            LocalDateTime.now()).lastModified(LocalDateTime.now()).id(Long.valueOf(TEST_CASE_ID)).build();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(null);
        cafcassUploadDocService.uploadDocument("Bearer abc", file, "FL401",
                                               "1234567891234567"
        );
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
