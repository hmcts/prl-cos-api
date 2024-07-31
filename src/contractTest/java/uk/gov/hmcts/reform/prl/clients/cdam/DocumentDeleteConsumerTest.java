package uk.gov.hmcts.reform.prl.clients.cdam;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;

import java.util.UUID;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.ccd.client"})
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "case-document-am-client-api", port = "5003")
@TestPropertySource(properties = {"case_document_am.url=http://localhost:5003"})
@PactFolder("pacts")
@SpringBootTest
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class DocumentDeleteConsumerTest {

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";

    @Autowired
    CaseDocumentClientApi caseDocumentClientApi;

    @Pact(provider = "case_document_am_client_api", consumer = "prl_cos")
    private V4Pact deleteDocument(PactDslWithProvider builder) throws JsonProcessingException {
        return builder
                .given("A request to delete a document from cdam api")
                .uponReceiving("a request to delete a document from cdam api with valid authorization")
                .method("DELETE")
                .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
                .headers("Authorization", BEARER_TOKEN)
                .path("/cases/documents/4f854707-91bf-4fa0-98ec-893ae0025cae")
                .matchQuery("permanent", "true")
                .willRespondWith()
                .status(HttpStatus.SC_OK)
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "deleteDocument")
    public void verifyDeleteDocument() {
        caseDocumentClientApi.deleteDocument(BEARER_TOKEN,
                SERVICE_AUTHORIZATION_HEADER, UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0025cae"), true
        );
    }
}