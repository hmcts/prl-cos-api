package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LinkCitizenCaseControllerFunctionalTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static final String CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY = "requests/create-case-with-access-coderequest.json";

    private static CaseDetails caseDetails;


    private final RequestSpecification request1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    private static final String CITIZEN_REQUEST_BODY
        = "requests/link-citizen-case-access-code.json";


    @Test
    public void createCcdTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);
        caseDetails =  request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/testing-support/create-ccd-case-data")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);

        Assert.assertNotNull(caseDetails);
        Assert.assertNotNull(caseDetails.getId());
    }

    @Test
    public void givenRequestBody_linkCaseToAccount_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_REQUEST_BODY);
        String requestBodyRevised = requestBody
            .replace("1711626009844770", caseDetails.getId().toString());
        CaseData caseDataResp = request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/citizen/link-case-to-account")
            .then()
            .body("caseInvites[0].id", equalTo("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                  "caseInvites[0].value.accessCode", equalTo("FVJKGHF"))
            .body("caseInvites[0].value.hasLinked", equalTo("Yes"))
            .extract()
            .as(CaseData.class);

    }

}
