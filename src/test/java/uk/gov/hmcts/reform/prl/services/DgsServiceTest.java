package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class DgsServiceTest {

    @InjectMocks
    private DgsService dgsService;

    @Mock
    private DgsApiClient dgsApiClient;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String PRL_DRAFT_TEMPLATE = "PRL-DRAFT-TRY-FINAL-11.docx";


    @Before
    public void setUp() {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

    }

    @Ignore
    @Test
    public void testToGenerateDocument() throws Exception {

        CaseDetails caseDetails = CaseDetailsProvider.full();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsService.generateDocument(authToken, null, PRL_DRAFT_TEMPLATE)).thenReturn(generatedDocumentInfo);

        assertEquals(dgsService.generateDocument(authToken, null, PRL_DRAFT_TEMPLATE),generatedDocumentInfo);

    }

}
