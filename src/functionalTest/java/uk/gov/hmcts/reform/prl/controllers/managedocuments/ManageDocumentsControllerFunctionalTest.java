package uk.gov.hmcts.reform.prl.controllers.managedocuments;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;

import static org.hamcrest.CoreMatchers.containsString;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ManageDocumentsControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static final String MANAGE_DOCUMENT_REQUESTTT = "requests/send-and-reply-request.json";

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Test
    @Ignore
    public void givenCaseId_whenAboutToStartEndPoint_thenRespWithDocumentCategories() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUESTTT);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/about-to-start")
            .then()
            .body("data.messageObject.senderEmail", containsString("fprl_caseworker_solicitor@mailinator.com"))
            .assertThat().statusCode(200);
    }


}
