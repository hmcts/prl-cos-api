package uk.gov.hmcts.reform.prl.controllers;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.services.reviewdocument.ReviewDocumentService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ReviewDocumentsControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );


    private static final String REVIEW_DOCUMENT_REQUEST = "requests/review-doc-body.json";

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Test
    public void givenReviewDocuments_ShouldSegregateDocAccordingly() throws Exception {
        ReviewDocumentService stock = mock(ReviewDocumentService.class);
        String requestBody = ResourceLoader.loadJson(REVIEW_DOCUMENT_REQUEST);
        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .contentType("application/json")
            .post("/review-documents/about-to-submit")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        log.info("RESULTTTTTT {}", response);

        List<Element<QuarantineLegalDoc>> restrictedDocuments
            = (List<Element<QuarantineLegalDoc>>) response.getData().get("restrictedDocuments");

        log.info("NNNNNN {}", restrictedDocuments);

    }

    @Test
    public void givenReviewDocuments_RestrictedAnConfidential_thenMoveToRestrictedDocuments() throws Exception {
        String requestBody = ResourceLoader.loadJson(REVIEW_DOCUMENT_REQUEST);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/review-documents/about-to-submit")
            .then()
            .body("data.restrictedDocuments[0].value.isConfidential", equalTo("Yes"),
                  "data.restrictedDocuments[0].value.isConfidential", equalTo("Yes"),
                  "data.restrictedDocuments[0].value.isRestricted", equalTo("Yes"),
                  "data.restrictedDocuments[0].value.documentFileName", equalTo("Confidential_Testdoc2.pdf"))
            .assertThat().statusCode(200);

    }
}
