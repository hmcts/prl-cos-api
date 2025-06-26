package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CafcassCdamServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @InjectMocks
    private CafcassCdamService cafcassCdamService;

    private UUID uuid;
    private String auth;
    private String s2s;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        auth = "auth-token";
        s2s = "s2s-token";
        when(authTokenGenerator.generate()).thenReturn(s2s);
    }

    @Test
    void when2xxAndNoContentType_thenGuessPdf() {
        Resource pdf = new ByteArrayResource(new byte[] {}) {
            @Override
            public String getFilename() {
                return "file.pdf";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.add("originalfilename", "file.pdf");
        ResponseEntity<Resource> upstream =
            new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        when(caseDocumentClient.getDocumentBinary(auth, s2s, uuid))
            .thenReturn(upstream);

        ResponseEntity<Resource> resp = cafcassCdamService.getDocument(auth, s2s, uuid);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("application/pdf", resp.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void when2xxAndHasContentType_thenPreserve() {
        Resource img = new ByteArrayResource(new byte[] {}) {
            @Override
            public String getFilename() {
                return "pic.png";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.add("originalfilename", "pic.png");
        ResponseEntity<Resource> upstream =
            new ResponseEntity<>(img, headers, HttpStatus.OK);

        when(caseDocumentClient.getDocumentBinary(auth, s2s, uuid))
            .thenReturn(upstream);

        ResponseEntity<Resource> resp = cafcassCdamService.getDocument(auth, s2s, uuid);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(MediaType.IMAGE_PNG, resp.getHeaders().getContentType());
    }

    @Test
    void whenNon2xx_thenNoGuessingOrOverride() {
        ResponseEntity<Resource> upstream =
            new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.BAD_REQUEST);
        when(caseDocumentClient.getDocumentBinary(auth, s2s, uuid))
            .thenReturn(upstream);

        ResponseEntity<Resource> resp = cafcassCdamService.getDocument(auth, s2s, uuid);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertFalse(resp.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE));
        assertNull(resp.getBody());
    }

    @Test
    void when2xxAndUnknownExtension_thenOctetStream() {
        Resource unknown = new ByteArrayResource(new byte[] {}) {
            @Override
            public String getFilename() {
                return "file.unknownext";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.add("originalfilename", "file.unknownext");
        ResponseEntity<Resource> upstream =
            new ResponseEntity<>(unknown, headers, HttpStatus.OK);

        when(caseDocumentClient.getDocumentBinary(auth, s2s, uuid))
            .thenReturn(upstream);

        ResponseEntity<Resource> resp = cafcassCdamService.getDocument(auth, s2s, uuid);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, resp.getHeaders().getContentType());
    }

    @Test
    void whenFilenameHeaderMissing_thenUseResourceFilename() {
        Resource pdf = new ByteArrayResource(new byte[] {}) {
            @Override
            public String getFilename() {
                return "other.pdf";
            }
        };
        ResponseEntity<Resource> upstream =
            new ResponseEntity<>(pdf, new HttpHeaders(), HttpStatus.OK);

        when(caseDocumentClient.getDocumentBinary(auth, s2s, uuid))
            .thenReturn(upstream);

        ResponseEntity<Resource> resp = cafcassCdamService.getDocument(auth, s2s, uuid);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("application/pdf", resp.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }
}
