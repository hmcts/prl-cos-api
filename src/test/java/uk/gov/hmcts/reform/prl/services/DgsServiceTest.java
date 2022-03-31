package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DgsServiceTest {

    @InjectMocks
    private DgsService dgsService;

    @Mock
    private DgsApiClient dgsApiClient;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String PRL_DRAFT_TEMPLATE = "FL-DIV-GOR-ENG-00062.docx";


    @Before
    public void setUp() {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

    }

    @Test
    public void testToGenerateDocument() throws Exception {

        CaseData caseData = CaseData.builder()
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId("123")
            .caseData(caseData)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsService.generateDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE)).thenReturn(generatedDocumentInfo);

        assertEquals(dgsService.generateDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE),generatedDocumentInfo);

    }

    @Test
    public void testToGenerateDocumentWithNoDataExpectedException() throws Exception {

        CaseDetails caseDetails = CaseDetailsProvider.full();

        when(dgsService.generateDocument(authToken, null, PRL_DRAFT_TEMPLATE)).thenReturn(generatedDocumentInfo);

        Throwable exception = assertThrows(Exception.class, () -> {
            throw new Exception("Error generating and storing document for case");
        });

        assertEquals("Error generating and storing document for case", exception.getMessage());

    }

    @Test
    public void testToGenerateWelshDocument() throws Exception {

        CaseData caseData = CaseData.builder()
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId("123")
            .caseData(caseData)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsService.generateWelshDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE)).thenReturn(generatedDocumentInfo);

        assertEquals(dgsService.generateWelshDocument(authToken, caseDetails, PRL_DRAFT_TEMPLATE),generatedDocumentInfo);

    }
}
