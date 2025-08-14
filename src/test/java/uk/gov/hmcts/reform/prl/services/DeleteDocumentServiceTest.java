package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeleteDocumentServiceTest {

    @InjectMocks
    DeleteDocumentService deleteDocumentService;

    String authToken = "auth-token";

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    CaseDocumentClient caseDocumentClient;

    @Before
    public void init() {
        ReflectionTestUtils.setField(deleteDocumentService, "caseDocumentClient", caseDocumentClient);
    }

    @Test
    public void testDeleteDocument() {
        when(authTokenGenerator.generate()).thenReturn(authToken);
        deleteDocumentService.deleteDocument(authToken, "4f854707-91bf-4fa0-98ec-893ae0025cae");
        verify(caseDocumentClient, times(1))
            .deleteDocument(authToken,
                             authTokenGenerator.generate(),
                             UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0025cae"),
                             true);
    }

}
