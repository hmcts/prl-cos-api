package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationEmailService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.ADDRESS_MISSED_FOR_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.CONFIRMATION_HEADER_PERSONAL;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ServiceOfApplicationControllerFT {

    private static final String VALID_REQUEST_BODY = "requests/service-of-application.json";

    private static final String VALID_REQUEST_BODY_BOTH = "requests/service-of-application-mix.json";

    private static final String VALID_REQUEST_BODY_WITHOUT_OTHER_PEOPLE = "requests/soa-with-out-other-people.json";

    private static final String BODY_FOR_CITIZEN_CREATED_CASE = "requests/soa-for-citizen-case.json";

    private static final String BODY_FOR_CITIZEN_CREATED_CASE_FROM_SECOND_TIME = "requests/soa-for-citizen-case-from-second-time.json";

    private static final String VALID_REQUEST_BODY_WITH_OTHER_PEOPLE = "requests/soa-with-other-people.json";

    private static final String VALID_REQUEST_BODY_WITH_OUT_C6A_ORDERS = "requests/soa-with-out-c6a-orders.json";

    private static final String FL401_VALID_REQUEST_BODY_PERSONAL_SERVICE_CA_CB = "requests/fl401-service-of-application-personal-service-ca.json";

    private static final String FL401_VALID_REQUEST_BODY_PERSONAL_SERVICE_LR = "requests/fl401-service-of-application-personal-service-lr.json";

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

    @MockBean
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

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

    @Test
    public void givenRequestWithFl401CaseData_Perosnal_Service_ca_cb_Submitted() throws Exception {

        String requestBody = ResourceLoader.loadJson(FL401_VALID_REQUEST_BODY_PERSONAL_SERVICE_CA_CB);
        EmailNotificationDetails emailNotificationDetails = EmailNotificationDetails.builder()
            .servedParty("ApplicantSolicitor")
            .build();
        when(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(Mockito.anyString(), Mockito.anyString(),
                                                                                    Mockito.any(), Mockito.any(), Mockito.any(),
                                                                                    Mockito.anyString()))
            .thenReturn(emailNotificationDetails);
        MvcResult res = mockMvc.perform(post("/service-of-application/submitted")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = res.getResponse().getContentAsString();
        assertTrue(json.contains("confirmation_header"));
        assertTrue(json.contains(CONFIRMATION_HEADER_PERSONAL));
        verify(serviceOfApplicationEmailService,times(1)).sendEmailUsingTemplateWithAttachments(Mockito.anyString(), Mockito.anyString(),
                                                               Mockito.any(), Mockito.any(), Mockito.any(),
                                                               Mockito.anyString());
    }

    @Test
    public void givenRequestWithFl401CaseData_Perosnal_Service_lr_Submitted() throws Exception {

        String requestBody = ResourceLoader.loadJson(FL401_VALID_REQUEST_BODY_PERSONAL_SERVICE_LR);
        EmailNotificationDetails emailNotificationDetails = EmailNotificationDetails.builder()
            .servedParty("ApplicantSolicitor")
            .build();
        when(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(Mockito.anyString(), Mockito.anyString(),
                                                                                    Mockito.any(), Mockito.any(), Mockito.any(),
                                                                                    Mockito.anyString()))
            .thenReturn(emailNotificationDetails);
        MvcResult res = mockMvc.perform(post("/service-of-application/submitted")
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(requestBody)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = res.getResponse().getContentAsString();
        assertTrue(json.contains("confirmation_header"));
        assertTrue(json.contains(CONFIRMATION_HEADER_PERSONAL));
        verify(serviceOfApplicationEmailService,times(1)).sendEmailUsingTemplateWithAttachments(Mockito.anyString(), Mockito.anyString(),
                                                                                                Mockito.any(), Mockito.any(), Mockito.any(),
                                                                                                Mockito.anyString());
    }


    /**
     * Service of Application journey.
     * When Soa being done for first time with any of the applicant has Representative
     * Then isApplicantRepresented should be 'Yes'.
     */
    @Test
    public void givenRequestWithCaseData_Response_isApplicantRepresented_firstTimeWithRep() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/about-to-submit")
            .then()
            .body("data.isApplicantRepresented", equalTo("Yes"))
            .extract().as(AboutToStartOrSubmitCallbackResponse.class);

    }

    /**
     * Service of Application journey.
     * When Soa being done for first time with none of the applicant has representative
     * Then isApplicantRepresented should be 'No'.
     */
    @Test
    public void givenRequestWithCaseData_Response_isApplicantRepresented_firstTime_withOutAnyRep() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WITHOUT_OTHER_PEOPLE);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/about-to-submit")
            .then()
            .body("data.isApplicantRepresented", equalTo("No"))
            .extract().as(AboutToStartOrSubmitCallbackResponse.class);

    }

    /**
     * Service of Application journey.
     * When Soa being done for second time onwards with or without  Representative
     * Then isApplicantRepresented should be an empty string value.
     */
    @Test
    public void givenRequestWithCaseData_Response_isApplicantRepresented_secondTimeOnwards() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WITH_OTHER_PEOPLE);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/about-to-submit")
            .then()
            .body("data.isApplicantRepresented", equalTo(EMPTY_STRING))
            .extract().as(AboutToStartOrSubmitCallbackResponse.class);

    }


    /**
     * Service of Application journey.
     * When Soa being done for first time for the citizen created case.
     * Then isApplicantRepresented should be 'No'.
     */
    @Test
    public void givenRequestWithCaseData_Response_isApplicantRepresented_firstTime_citizenCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(BODY_FOR_CITIZEN_CREATED_CASE);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/about-to-submit")
            .then()
            .body("data.isApplicantRepresented", equalTo("No"))
            .extract().as(AboutToStartOrSubmitCallbackResponse.class);

    }


    /**
     * Service of Application journey.
     * When Soa being done for second time onwards for citizen created case
     * Then isApplicantRepresented should be an empty string value.
     */
    @Test
    public void givenRequestWithCaseData_Response_isApplicantRepresented_citizen_secondTimeOnwards() throws Exception {

        String requestBody = ResourceLoader.loadJson(BODY_FOR_CITIZEN_CREATED_CASE_FROM_SECOND_TIME);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/about-to-submit")
            .then()
            .body("data.isApplicantRepresented", equalTo(EMPTY_STRING))
            .extract().as(AboutToStartOrSubmitCallbackResponse.class);

    }

    /**
     * Service of Application journey.
     * When Soa being done for first time with any of the applicant has Representative
     * Then isApplicantRepresented should be 'Yes'.
     */
    @Test
    public void givenRequestWithCaseData_Response_isApplicantRepresented_firstTimeWithRep1111() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_BOTH);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/about-to-submit")
            .then()
            .body("data.isApplicantRepresented", equalTo("Yes"))
            .extract().as(AboutToStartOrSubmitCallbackResponse.class);

    }



}
