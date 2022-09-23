package uk.gov.hmcts.reform.prl.controllers.citizen;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseDocumentControllerTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "TestS2sToken";

    @InjectMocks
    private CaseDocumentController caseDocumentController;

    @Mock
    private DocumentGenService documentGenService;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    CaseService caseService;

    @Mock
    private AuthorisationService authorisationService;

    @Test
    public void testDocumentUpload() {
        //Given
        MultipartFile mockFile = mock(MultipartFile.class);
        Document mockDocument = Document.builder().build();
        DocumentResponse documentResponse = DocumentResponse
                .builder()
                .status("SUCCESS")
                .document(mockDocument)
                .build();

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        when(documentGenService.uploadDocument(authToken, mockFile)).thenReturn(documentResponse);

        //When
        ResponseEntity<?> response = caseDocumentController
                .uploadCitizenDocument(authToken, s2sToken, mockFile);
        //Then
        assertEquals(documentResponse, response.getBody());
    }

    @Test
    public void testDeleteDocument() {
        //Given
        DocumentResponse documentResponse = DocumentResponse
                .builder()
                .status("SUCCESS")
                .build();

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        when(documentGenService.deleteDocument(authToken, "TEST_DOCUMENT_ID")).thenReturn(documentResponse);

        //When
        ResponseEntity<?> response = caseDocumentController
                .deleteDocument(authToken, s2sToken, "TEST_DOCUMENT_ID");
        //Then
        assertEquals(documentResponse, response.getBody());
    }
}
