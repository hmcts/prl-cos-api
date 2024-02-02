package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WARNING_TEXT_DIV;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ServiceOfApplicationControllerFunctionalTest {

    private static final String VALID_REQUEST_BODY = "requests/service-of-application.json";

    private static final String VALID_REQUEST_BODY_WITHOUT_OTHER_PEOPLE = "requests/soa-with-out-other-people.json";

    private static final String VALID_REQUEST_BODY_WITH_OTHER_PEOPLE = "requests/soa-with-other-people.json";

    private static final String VALID_REQUEST_BODY_WITH_OUT_C6A_ORDERS = "requests/soa-with-out-c6a-orders.json";

    private static final String ADDRESS_MISSED_FOR_OTHER_PARTIES = WARNING_TEXT_DIV
        + "</span><strong class='govuk-warning-text__text'>There is no postal address for other people in the "
        + "case</strong></div>";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private SendAndReplyService sendAndReplyService;

    @Test
    public void givenRequestWithCaseData_ResponseContainsHeaderAndCollapsable() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        DynamicListElement dynamicListElement = DynamicListElement.builder().label("xxxx").build();
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(dynamicListElement);
        when(sendAndReplyService.getCategoriesAndDocuments(anyString(), anyString())).thenReturn(DynamicList.builder().listItems(listItems).build());
        mockMvc.perform(post("/service-of-application/about-to-start")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestWithCaseData_Response_AboutToSubmit() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/service-of-application/about-to-submit")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestWithCaseData_Response_Submitted() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/service-of-application/submitted")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestWithCaseData_WithMissingRespondentAddress() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        DynamicListElement dynamicListElement = DynamicListElement.builder().label("xxxx").build();
        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(dynamicListElement);
        when(sendAndReplyService.getCategoriesAndDocuments(anyString(), anyString())).thenReturn(DynamicList.builder().listItems(listItems).build());
        mockMvc.perform(post("/service-of-application/about-to-start")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.missingAddressWarningText").value(ADDRESS_MISSED_FOR_OTHER_PARTIES))
            .andReturn();
    }

    /**
     * When Other people not selected.
     * then error should not appear at all during the service of application submission.
     *
     */
    @Test
    public void givenRequestWithCaseData_MidEvent_whenOtherpeopleNotSelected_then_c6A_isNotRequired() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WITHOUT_OTHER_PEOPLE);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/soa-validation")
            .then()
            .body("errors", equalTo(null))
            .assertThat().statusCode(200);
    }


    /**
     * When Other people selected, but C6a Order not selected.
     * then error should appear during Service of application submission.
     *
     */
    @Test
    public void givenCaseData_whenOtherpeopleSelectedButC6A_NotSelected_then_ValidationError() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WITH_OTHER_PEOPLE);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/soa-validation")
            .then()
            .body("errors[0]", equalTo(OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR))
            .assertThat().statusCode(200);
    }


    /**
     * When Other people selected, but C6a Order not even present in the order collection.
     * then error should appear during Service of application submission.
     *
     */
    @Test
    public void givenCaseData_whenOtherpeopleSelectedButC6A_NotEvenPresent_then_ValidationError() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WITH_OUT_C6A_ORDERS);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/soa-validation")
            .then()
            .body("errors[0]", equalTo(OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR))
            .assertThat().statusCode(200);
    }

}
