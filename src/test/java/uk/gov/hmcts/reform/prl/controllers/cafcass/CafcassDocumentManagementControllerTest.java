package uk.gov.hmcts.reform.prl.controllers.cafcass;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassCdamService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_TEST_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_AUTHORIZATION;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_CAFCASS_DOWNLOAD_FILENAME;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_SERVICE_AUTHORIZATION;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class CafcassDocumentManagementControllerTest {

    @Mock
    private CafcassCdamService cafcassCdamService;

    @InjectMocks
    private CafcassDocumentManagementController cafcassDocumentManagementController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private UserInfo userInfo;

    private static final int TEST_DOWNLOAD_FILE_CONTENT_LENGTH = 10000;

    private final String userToken = "Bearer testToken";

    private UUID documentId;

    @BeforeEach
    public void setUp() {
        documentId = randomUUID();
    }

    @Test
    @DisplayName("Successful download of document through CDAM Service")
    public void testCdamDocumentDownloadServiceResponseStatusOk() {
        Resource documentResource = createNewResource();

        ResponseEntity<Resource> expectedResponse = ResponseEntity.status(OK).contentType(MediaType.APPLICATION_PDF).body(
            documentResource);
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(authorisationService.getUserInfo()).thenReturn(userInfo);
        when(authorisationService.getUserInfo().getRoles()).thenReturn(Arrays.asList("caseworker-privatelaw-cafcass"));
        when(systemUserService.getSysUserToken()).thenReturn(CAFCASS_TEST_AUTHORISATION_TOKEN);

        Mockito.when(cafcassCdamService.getDocument(
                CAFCASS_TEST_AUTHORISATION_TOKEN,
                CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN,
                documentId
            ))
            .thenReturn(expectedResponse);
        ResponseEntity responseEntity = cafcassDocumentManagementController.downloadDocument(
            CAFCASS_TEST_AUTHORISATION_TOKEN,
            CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN,
            documentId
        );

        assertNotNull(responseEntity.getBody());
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Failed download of document through CDAM Service")
    public void testGetDocumentBinary() {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(authorisationService.getUserInfo()).thenReturn(userInfo);
        when(authorisationService.getUserInfo().getRoles()).thenReturn(Arrays.asList("caseworker-privatelaw-cafcass"));
        when(systemUserService.getSysUserToken()).thenReturn(CAFCASS_TEST_AUTHORISATION_TOKEN);

        Mockito.when(cafcassCdamService.getDocument(
                CAFCASS_TEST_AUTHORISATION_TOKEN,
                CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN,
                documentId
            ))
            .thenReturn(new ResponseEntity<Resource>(
                BAD_REQUEST));

        ResponseEntity responseEntity = cafcassDocumentManagementController.downloadDocument(
            CAFCASS_TEST_AUTHORISATION_TOKEN,
            CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN,
            documentId
        );

        assertNull(responseEntity.getBody());
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());

    }

    @Test
    public void testInvalidServicAuth_401UnAuthorized() {
        when(authorisationService.authoriseService(any())).thenReturn(false);
        when(authorisationService.authoriseUser(any())).thenReturn(false);
        final ResponseEntity response = cafcassDocumentManagementController.downloadDocument(
            CAFCASS_TEST_AUTHORISATION_TOKEN,
            CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN,
            documentId
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    @Test
    public void testFeignExceptionBadRequest() throws IOException {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(authorisationService.getUserInfo()).thenReturn(userInfo);
        when(authorisationService.getUserInfo().getRoles()).thenReturn(Arrays.asList("caseworker-privatelaw-cafcass"));
        when(systemUserService.getSysUserToken()).thenReturn(CAFCASS_TEST_AUTHORISATION_TOKEN);
        when(cafcassCdamService.getDocument(CAFCASS_TEST_AUTHORISATION_TOKEN, TEST_SERVICE_AUTHORIZATION, documentId)).thenThrow(
            feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));
        final ResponseEntity response = cafcassDocumentManagementController.downloadDocument(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            documentId
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testFeignExceptionUnAuthorised() throws IOException {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(authorisationService.getUserInfo()).thenReturn(userInfo);
        when(authorisationService.getUserInfo().getRoles()).thenReturn(Arrays.asList("caseworker-privatelaw-cafcass"));
        when(systemUserService.getSysUserToken()).thenReturn(CAFCASS_TEST_AUTHORISATION_TOKEN);

        when(cafcassCdamService.getDocument(CAFCASS_TEST_AUTHORISATION_TOKEN, TEST_SERVICE_AUTHORIZATION, documentId)).thenThrow(
            feignException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"));
        final ResponseEntity response = cafcassDocumentManagementController.downloadDocument(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            documentId
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testExceptionInternalServerError() throws IOException {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(cafcassCdamService.getDocument(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            documentId
        )).thenThrow(new RuntimeException());
        final ResponseEntity response = cafcassDocumentManagementController.downloadDocument(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            documentId
        );
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    public static FeignException feignException(int status, String message) {
        return FeignException.errorStatus(message, Response.builder()
            .status(status)
            .request(Request.create(GET, EMPTY, Map.of(), new byte[]{}, UTF_8, null))
            .build());
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
