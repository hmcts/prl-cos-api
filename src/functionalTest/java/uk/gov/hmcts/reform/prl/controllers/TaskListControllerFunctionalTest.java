package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static io.restassured.RestAssured.given;

public class TaskListControllerFunctionalTest {


    private final String taskListControllerEndPoint = "/update-task-list/submitted";

    private final String validBody = "controller/valid-request-casedata-body.json";

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @BeforeClass
    public static void setup() throws Exception {
        RestAssured.port = 4044;


    }

    @Test
    public void whenInvalidRequestFormat_Return400() throws Exception {
        Header headers  =  new Header("Authorization", "TestAuth");
        given().when().contentType(ContentType.JSON).header(headers).post(taskListControllerEndPoint)
            .then().assertThat().statusCode(400);

    }

    @Test
    public void whenValidRequestFormat_Return200() throws Exception {
        Header headers  =  new Header("Authorization", "TestAuth");
        String requestBody = ResourceLoader.loadJson(validBody);
        given().when().contentType(ContentType.JSON).header(headers).body(requestBody).post(taskListControllerEndPoint)
            .then().assertThat().statusCode(200);

    }
}
