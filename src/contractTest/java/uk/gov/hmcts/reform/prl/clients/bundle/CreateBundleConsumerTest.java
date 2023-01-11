package uk.gov.hmcts.reform.prl.clients.bundle;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
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
import uk.gov.hmcts.reform.prl.models.dto.bundle.Bundle;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDocument;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDocumentDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleFolder;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleFolderDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleNestedSubfolder1;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleNestedSubfolder1Details;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleSubfolder;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleSubfolderDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.prl.models.dto.bundle.DocumentLink;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

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
    properties = {"bundle.api.url=http://localhost:8899", "idam.api.url=localhost:5000"}
)
@PactFolder("pacts")
public class CreateBundleConsumerTest {
    @Autowired
    BundleApiClient bundleApiClient;

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";


    @Pact(provider = "createBundleApi", consumer = "prl_cos")
    private RequestResponsePact generateCreateBundleResponse(PactDslWithProvider builder) throws JsonProcessingException {
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
            .body(new ObjectMapper().writeValueAsString(BundleCreateRequest.builder().eventId("createBundle").jurisdictionId("JURISDICTIONID").caseTypeId("CASETYPEID").caseDetails(
                BundlingCaseDetails.builder().caseData(BundlingCaseData.builder().id("CaseID").bundleConfiguration("BUNDLE_config.yaml")
                    .data(BundlingData.builder().allOtherDocuments(ElementUtils.wrapElements(bundlingRequestDocuments)).build())
                    .build()).build())), "application/json")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(createBundleResponse())
            .toPact();
    }

    private PactDslJsonBody createBundleResponse() {
        List<BundleDocument> bundleDocuments = new ArrayList<>();
        bundleDocuments.add(BundleDocument.builder().value(
            BundleDocumentDetails.builder().name("MiamCertificate").description("MiamCertificate").sortIndex(1)
                .sourceDocument(DocumentLink.builder().build()).build()).build());
        bundleDocuments.add(BundleDocument.builder().value(BundleDocumentDetails.builder().build()).build());

        List<BundleNestedSubfolder1> bundleNestedSubfolders1 = new ArrayList<>();
        bundleNestedSubfolders1.add(BundleNestedSubfolder1.builder()
            .value(BundleNestedSubfolder1Details.builder().name("MiamCertificate").documents(bundleDocuments).build()).build());
        List<BundleNestedSubfolder1> bundleNestedSubfolders2 = new ArrayList<>();
        bundleNestedSubfolders2.add(BundleNestedSubfolder1.builder()
            .value(BundleNestedSubfolder1Details.builder().build()).build());
        List<BundleFolder> bundleFolders = new ArrayList<>();
        List<BundleSubfolder> bundleSubfolders = new ArrayList<>();
        bundleSubfolders.add(BundleSubfolder.builder()
            .value(BundleSubfolderDetails.builder().name("Applicant documents").documents(bundleDocuments)
                .folders(bundleNestedSubfolders1).build()).build());
        bundleSubfolders.add(BundleSubfolder.builder()
            .value(BundleSubfolderDetails.builder().documents(bundleDocuments)
                .folders(bundleNestedSubfolders2).build()).build());
        bundleFolders.add(BundleFolder.builder().value(BundleFolderDetails.builder().name("Applications and Orders")
            .folders(bundleSubfolders).build()).build());
        List<Bundle> bundleRefreshList = new ArrayList<>();
        bundleRefreshList.add(Bundle.builder()
            .value(BundleDetails.builder().stitchedDocument(DocumentLink.builder().documentFilename("StitchedPDF").build())
            .stitchStatus("DONE").folders(bundleFolders).build()).build());
        return new PactDslJsonBody().equalTo("data",BundleData.builder().caseBundles(bundleRefreshList).build())
            .stringType("documentTaskId", "documentTaskId");

    }

    @Test
    @PactTestFor(pactMethod = "generateCreateBundleResponse")
    public void verifyCreateBundle() {
        BundleCreateResponse bundleCreateResponse = bundleApiClient.createBundleServiceRequest(BEARER_TOKEN,
            SERVICE_AUTHORIZATION_HEADER, BundleCreateRequest.builder().build()
        );
        assertNotNull(bundleCreateResponse);
        assertEquals(1, bundleCreateResponse.getData().getCaseBundles().get(0).getValue().getFolders().size());
        assertEquals("DONE", bundleCreateResponse.getData().getCaseBundles().get(0).getValue().getStitchStatus());
        assertEquals("StitchedPDF", bundleCreateResponse.getData().getCaseBundles().get(0).getValue().getStitchedDocument().documentFilename);
    }
}
