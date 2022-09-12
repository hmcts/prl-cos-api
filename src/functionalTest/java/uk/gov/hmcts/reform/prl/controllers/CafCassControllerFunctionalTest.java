package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.client.S2sClient;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassCcdDataStoreService;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
public class CafCassControllerFunctionalTest {

    @Autowired
    S2sClient s2sClient;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CafcassCcdDataStoreService cafcassCcdDataStoreService;

    @MockBean
    private CaseDataService caseDataService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    private static final String CONTENT_TYPE_JSON = "application/json";

    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:4044"
            );

    private final RequestSpecification request = given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenDatetimeWindow_whenGetRequestToSearchCasesByCafCassController_then200Response() throws Exception {
        Response response =
                request.header("serviceauthorization", s2sClient.serviceAuthTokenGenerator())
                        .queryParam("start_date",
                                "2022-08-22T10:39:43.49")
                        .queryParam("end_date",
                                "2022-08-26T18:44:54.055")
                        .when()
                        .contentType("application/json")
                        .get("/searchCases");

        response.then().assertThat().statusCode(HttpStatus.OK.value());

    }

    @Test
    public void givenDatetimeWindowSearchCasesByCafCassController_thenCheckMandatoryFields() throws Exception {
        Response response = request
                .header("Authorization", "Bearer Token")
                .contentType(CONTENT_TYPE_JSON)
                .given()
                .queryParams("start_date",
                        "2022-08-22T10:39:43.49",
                        "end_date",
                        "2022-08-26T10:44:54.055")
                .when()
                .get("/searchCases");

        CafCassResponse cafcassResponse = (CafCassResponse) response.getBody();
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        String jsonResponseStr = objectMapper.writeValueAsString(cafcassResponse);

        JSONObject json = new JSONObject(cafcassResponse);
        assertNotNull(json.getString("cases.state").toString());
        assertNotNull(json.getString("cases.case_type_id").toString());
        assertNotNull(json.getString("cases.caseTypeofApplication").toString());

    }

    @Test
    public void givenNullDateWindow_whenGetRequestToSearchCasesByCafCassController_then400Response() throws Exception {
        request
                .header("Authorization", "Bearer Token")
                .contentType(CONTENT_TYPE_JSON)
                .given()
                .pathParams("start_date",
                        null,
                        "end_date",
                        "22-02-2022 14:15")
                .when()
                .get("/searchCases")
                .then().assertThat().statusCode(400);

    }
}