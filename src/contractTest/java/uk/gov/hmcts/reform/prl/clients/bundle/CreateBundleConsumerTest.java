package uk.gov.hmcts.reform.prl.clients.bundle;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.clients.BundleApiClient;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.prl.enums.bundle.BundlingDocGroupEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "createBundleApi", port = "8899")
@ContextConfiguration(
    classes = {BundleApiConsumerApplication.class, IdamApiConsumerApplication.class}
)
@TestPropertySource(
    properties = {"bundle.api.url=http://localhost:8899","idam.api.url=localhost:5000","commonData.api.url=localhost:5000",
        "fis_hearing.api.url=localhost:5000",
        "refdata.api.url=",
        "courtfinder.api.url=",
        "prl-dgs-api.url=",
        "fees-register.api.url=",
        "fis_hearing.api.url=",
        "judicialUsers.api.url=",
        "locationfinder.api.url=",
        "rd_professional.api.url=",
        "payments.api.url=",
        "pba.validation.service.api.baseurl=",
        "staffDetails.api.url=",
        "amRoleAssignment.api.url="
    }
)
@PactFolder("pacts")
public class CreateBundleConsumerTest {
    @Autowired
    BundleApiClient bundleApiClient;

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";

    private final BundleCreateRequest bundleCreateRequest = BundleCreateRequest.builder().build();

    private final String validResponseBody = "bundle/ValidResponseBody.json";

    @Pact(provider = "createBundleApi", consumer = "prl_cos")
    private V4Pact generateCreateBundleResponse(PactDslWithProvider builder) throws Exception {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.add(BundlingRequestDocument.builder().documentLink(Document.builder().build())
            .documentFileName("otherDocs").documentGroup(BundlingDocGroupEnum.applicantPositionStatements).build());
        BundleCreateRequest.builder().eventId("createBundle").jurisdictionId("JURISDICTIONID").caseTypeId("CASETYPEID").caseDetails(
            BundlingCaseDetails.builder().caseData(BundlingCaseData.builder().id("CaseID").bundleConfiguration("BUNDLE_config.yaml")
                    .data(BundlingData.builder().allOtherDocuments(ElementUtils.wrapElements(bundlingRequestDocuments)).build())
                .build()).build());
        return builder
            .given("A request to create a bundle in Bundling api")
            .uponReceiving("a request to create a bundle in bundling api with valid authorization")
            .method("POST")
            .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
            .headers("Authorization", BEARER_TOKEN)
            .headers("Content-Type", "application/json")
            .path("/api/new-bundle")
            .body(new ObjectMapper().writeValueAsString(bundleCreateRequest), "application/json")
            .willRespondWith()
            .status(200)
            .body(ResourceLoader.loadJson(validResponseBody),"application/json")

            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "generateCreateBundleResponse")
    public void verifyCreateBundle() {
        BundleCreateResponse bundleCreateResponse = bundleApiClient.createBundleServiceRequest(BEARER_TOKEN,
            SERVICE_AUTHORIZATION_HEADER, BundleCreateRequest.builder().build()
        );
        assertNotNull(bundleCreateResponse);
        //assertEquals(4, bundleCreateResponse.getData().getCaseBundles().get(0).getValue().getFolders().size());
        assertEquals("DONE", bundleCreateResponse.getData().getCaseBundles().get(0).getValue().getStitchStatus());
        assertEquals("StitchedPDF", bundleCreateResponse.getData().getCaseBundles().get(0).getValue().getStitchedDocument().getDocumentFilename());
    }
}
