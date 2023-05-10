package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CafcassCdamServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private UUID uuid;

    private String authToken;
    private String s2sToken;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @InjectMocks
    private CafcassCdamService cafcassCdamService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        uuid = randomUUID();
        authToken = "auth-token";
        s2sToken = "s2sToken";
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
    }

    @Test
    @DisplayName("test case for Case document API.")
    public void testGetDocumentBinary() {

        when(caseDocumentClient.getDocumentBinary(authToken, s2sToken, uuid)).thenReturn(new ResponseEntity<Resource>(
            HttpStatus.OK));

        final ResponseEntity<Resource> response = cafcassCdamService.getDocument(authToken, s2sToken, uuid);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    @DisplayName("test case for Case document API when document id is incorrect.")
    public void testGetDocumentBinaryNotFound() {

        when(caseDocumentClient.getDocumentBinary(authToken, s2sToken, uuid)).thenReturn(new ResponseEntity<Resource>(
            HttpStatus.NOT_FOUND));

        final ResponseEntity<Resource> response = cafcassCdamService.getDocument(authToken, s2sToken, uuid);

        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }
}
