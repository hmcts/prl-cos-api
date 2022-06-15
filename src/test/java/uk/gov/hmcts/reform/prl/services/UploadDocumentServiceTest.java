package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@RunWith(MockitoJUnitRunner.class)
public class UploadDocumentServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @InjectMocks
    private UploadDocumentService uploadDocumentService;

    private static final String FILE_NAME = "fileName";
    private static final String CONTENT_TYPE = "application/json";
    private static final String AUTH = "auth";

    @Test
    public void uploadDocumentSuccess() {
        byte[] pdf = new byte[]{1,2,3,4,5};
        MultipartFile file = new InMemoryMultipartFile("files", FILE_NAME, CONTENT_TYPE, pdf);

        Document document = testDocument();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(authTokenGenerator.generate()).thenReturn("s2s");
        when(caseDocumentClient.uploadDocuments(AUTH, "s2s", CASE_TYPE, JURISDICTION, newArrayList(file))).thenReturn(uploadResponse);
        assertEquals(document, uploadDocumentService.uploadDocument(pdf, FILE_NAME, CONTENT_TYPE, AUTH));

    }

    @Test
    public void uploadDocumentFailure() {
        byte[] pdf = new byte[]{1,2,3,4,5};
        MultipartFile file = new InMemoryMultipartFile("files", FILE_NAME, CONTENT_TYPE, pdf);
        UploadResponse uploadResponse = new UploadResponse(Collections.emptyList());
        when(authTokenGenerator.generate()).thenReturn("s2s");
        when(caseDocumentClient.uploadDocuments(AUTH, "s2s", CASE_TYPE, JURISDICTION, newArrayList(file))).thenReturn(uploadResponse);
        assertThrows(RuntimeException.class, () -> uploadDocumentService.uploadDocument(pdf, FILE_NAME, CONTENT_TYPE, AUTH));


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
