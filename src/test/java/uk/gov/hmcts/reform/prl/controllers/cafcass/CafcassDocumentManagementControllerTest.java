package uk.gov.hmcts.reform.prl.controllers.cafcass;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.controllers.cafcaas.CafcassDocumentManagementController;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassCdamService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_TEST_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_CAFCASS_DOWNLOAD_FILENAME;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class CafcassDocumentManagementControllerTest {
    private static final UUID TEST_CAFCASS_FILE_ID = UUID.nameUUIDFromBytes("3254348".getBytes(StandardCharsets.UTF_8));

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private CafcassCdamService cafcassCdamService;

    @InjectMocks
    private CafcassDocumentManagementController cafcassDocumentManagementController;

    private static final int TEST_DOWNLOAD_FILE_CONTENT_LENGTH = 10000;

    @Test
    @DisplayName("Successful download of document through CDAM Service")
    public void testCdamDocumentDownloadServiceResponseStatusOk() {
        Resource documentResource = createNewResource();

        ResponseEntity<Resource> expectedResponse = ResponseEntity.status(HttpStatus.OK).body(documentResource);

        final UUID documentId = UUID.randomUUID();

        Mockito.when(cafcassCdamService.getDocument(CAFCASS_TEST_AUTHORISATION_TOKEN, CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN, documentId))
                .thenReturn(expectedResponse);
        ResponseEntity responseEntity = cafcassDocumentManagementController.downloadDocument(
                CAFCASS_TEST_AUTHORISATION_TOKEN,
                CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN,
                documentId);

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Failed download of document through CDAM Service")
    public void testGetDocumentBinary() {
        final UUID documentId = UUID.randomUUID();

        Mockito.when(cafcassCdamService.getDocument(CAFCASS_TEST_AUTHORISATION_TOKEN, CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN, documentId))
                .thenReturn(new ResponseEntity<Resource>(
                        HttpStatus.NOT_FOUND));

        ResponseEntity responseEntity = cafcassDocumentManagementController.downloadDocument(
                CAFCASS_TEST_AUTHORISATION_TOKEN,
                CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN,
                documentId);

        assertNull(responseEntity.getBody());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

    }

    private Resource createNewResource() {
        Resource documentResource = new Resource() {
            @Override
            public InputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public URL getURL() throws IOException {
                return new URL(EMPTY_STRING);
            }

            @Override
            public URI getURI() throws IOException {
                try {
                    return new URI(EMPTY_STRING);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public File getFile() throws IOException {
                return new File(EMPTY_STRING);
            }

            @Override
            public long contentLength() throws IOException {
                return TEST_DOWNLOAD_FILE_CONTENT_LENGTH;
            }

            @Override
            public long lastModified() throws IOException {
                return 0;
            }

            @Override
            public Resource createRelative(String relativePath) throws IOException {
                return null;
            }

            @Override
            public String getFilename() {
                return TEST_CAFCASS_DOWNLOAD_FILENAME;
            }

            @Override
            public String getDescription() {
                return null;
            }

        };
        return documentResource;
    }
}
