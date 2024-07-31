package uk.gov.hmcts.reform.prl.clients.cdam;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.ccd.document.am.model.Classification.RESTRICTED;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.ccd.client"})
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "case-document-am-client-api", port = "5004")
@TestPropertySource(properties = {"case_document_am.url=http://localhost:5004"})
@PactFolder("pacts")
@SpringBootTest
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class DocumentUploadConsumerTest {

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String DOCUMENT_NAME = "Test.pdf";

    @Autowired
    CaseDocumentClientApi caseDocumentClientApi;

    @Pact(provider = "case_document_am_client_api", consumer = "prl_cos")
    private V4Pact uploadDocument(PactDslWithProvider builder) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return builder
                .given("A request to upload a document on cdam api")
                .uponReceiving("a request to upload a document on cdam api with valid authorization")
                .method("POST")
                .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
                .headers("Authorization", BEARER_TOKEN)
                .path("/cases/documents")
                .willRespondWith()
                .status(HttpStatus.SC_OK)
                .body(mapper.writeValueAsString(createDocumentUploadResponse()), "application/json")
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "uploadDocument")
    public void verifyUploadedDocument() throws Exception {
        UploadResponse response = caseDocumentClientApi.uploadDocuments(BEARER_TOKEN, SERVICE_AUTHORIZATION_HEADER,
                 buildDocumentUploadRequest());
        Assertions.assertNotNull(response);
        assertEquals(RESTRICTED, response.getDocuments().get(0).classification);
        assertEquals(DOCUMENT_NAME, response.getDocuments().get(0).originalDocumentName);
    }

    private DocumentUploadRequest buildDocumentUploadRequest() throws Exception {
        String filePath = "classpath:Test.pdf";
        final MultipartFile file = new InMemoryMultipartFile(filePath, filePath, MediaType.APPLICATION_PDF_VALUE,
                resourceAsBytes(filePath));
        return new DocumentUploadRequest("RESTRICTED", "C100", "PRLAPPS", List.of(file));
    }

    private UploadResponse createDocumentUploadResponse() {
        Document document = Document.builder().classification(RESTRICTED).originalDocumentName(DOCUMENT_NAME).build();
        return new UploadResponse(List.of(document));
    }

    public static byte[] resourceAsBytes(final String resourcePath) throws IOException {
        final File file = ResourceUtils.getFile(resourcePath);
        return Files.readAllBytes(file.toPath());
    }
}