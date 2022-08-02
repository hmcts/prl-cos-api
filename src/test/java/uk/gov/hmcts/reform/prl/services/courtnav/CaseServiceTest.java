package uk.gov.hmcts.reform.prl.services.courtnav;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseServiceTest {

    private final String authToken = "Bearer abc";
    private final String s2sToken = "s2s token";
    private final String randomUserId = "e3ceb507-0137-43a9-8bd3-85dd23720648";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private IdamClient idamClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @InjectMocks
    CaseService caseService;


    private CaseData caseData;

    public MultipartFile file;

    @Before
    public void setup() {
        caseData = CaseData.builder().id(1234567891234567L).applicantCaseName("xyz").build();
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(randomUserId).build());
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(coreCaseDataApi.startForCaseworker(any(), any(), any(), any(), any(), any())
        ).thenReturn(StartEventResponse.builder().eventId("courtnav-case-creation").token("eventToken").build());
        file = new MockMultipartFile(
            "file",
            "private-law.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "FL401 case".getBytes()
        );
    }

    @Test
    public void shouldStartAndSubmitEventWithEventData() {
        caseService.createCourtNavCase("Bearer abc", caseData);
        verify(coreCaseDataApi).startForCaseworker(authToken, s2sToken,
                                                   randomUserId, PrlAppsConstants.JURISDICTION,
                                                   PrlAppsConstants.CASE_TYPE, "courtnav-case-creation"
        );
    }

    @Ignore
    @Test
    public void shouldUploadDocumentWhenAllFieldsAreCorrect() {
        Document document = testDocument();
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken("eventToken")
            .event(Event.builder()
                       .id("eventId")
                       .build())
            .data(Map.of("fl401Doc1",testDocument()))
            .build();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any())
        ).thenReturn(StartEventResponse.builder().eventId("courtnav-document-upload").token("eventToken").build());
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(randomUserId).build());
        when(coreCaseDataApi.submitEventForCaseWorker(authToken,
                                                      s2sToken,
                                                      randomUserId,
                                                      PrlAppsConstants.JURISDICTION,
                                                      PrlAppsConstants.CASE_TYPE,
                                                      "1234567891234567",
                                                      true,
                                                      caseDataContent)
        ).thenReturn(CaseDetails.builder().id(1234567891234567L).data(Map.of("typeOfDocument",
                                                                             "fl401Doc1")).build());
        caseService.uploadDocument("Bearer abc", file, "fl401Doc1",
                                   "1234567891234567");
        verify(coreCaseDataApi, times(1)).startForCaseworker(authToken, s2sToken,
                                                             randomUserId, PrlAppsConstants.JURISDICTION,
                                                             PrlAppsConstants.CASE_TYPE,
                                                             "courtnav-document-upload"
        );
    }


    public static Document testDocument() {
        Document.Link binaryLink = new Document.Link();
        binaryLink.href = randomAlphanumeric(10);
        Document.Link selfLink = new Document.Link();
        selfLink.href = randomAlphanumeric(10);

        Document.Links links = new Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        Document document = Document.builder().build();
        document.links = links;
        document.originalDocumentName = randomAlphanumeric(10);

        return document;
    }

}
