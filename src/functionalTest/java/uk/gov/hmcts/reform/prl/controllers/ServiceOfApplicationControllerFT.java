package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationEmailService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.ADDRESS_MISSED_FOR_OTHER_PARTIES;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServiceOfApplicationControllerFT {

    private static final String VALID_REQUEST_BODY = "requests/service-of-application.json";

    private static final String VALID_REQUEST_BODY_WITHOUT_OTHER_PEOPLE = "requests/soa-with-out-other-people.json";

    private static final String VALID_REQUEST_BODY_WITH_OTHER_PEOPLE = "requests/soa-with-other-people.json";

    private static final String VALID_REQUEST_BODY_WITH_OUT_C6A_ORDERS = "requests/soa-with-out-c6a-orders.json";

    private static final String FL401_VALID_REQUEST_BODY_PERSONAL_SERVICE_CA_CB = "requests/fl401-service-of-application-personal-service-ca.json";

    private static final String FL401_VALID_REQUEST_BODY_PERSONAL_SERVICE_LR = "requests/fl401-service-of-application-personal-service-lr.json";

    private static final String VALID_CAFCASS_REQUEST_JSON = "requests/cafcass-cymru-send-email-request.json";
    private static final String VALID_CAFCASS_REQUEST_JSON_FL401 = "requests/soa-fl401-cafcass-cymru-send-email-request.json";


    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static CaseDetails caseDetails;

    private static CaseDetails caseDetails1;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        RestAssured.registerParser("text/html", Parser.JSON);
    }

    @MockBean
    private SendAndReplyService sendAndReplyService;

    @MockBean
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Test
    public void createCcdTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);
        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBody)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        caseDetails = mapper.readValue(json, CaseDetails.class);

        Assert.assertNotNull(caseDetails);
        Assert.assertNotNull(caseDetails.getId());



    }

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
        String requestBodyRevised = requestBody.replace("1711105989241323", String.valueOf(caseDetails.getId()));
        mockMvc.perform(post("/service-of-application/submitted")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBodyRevised)
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
        String requestBodyRevised = requestBody.replace("1711117454766387", String.valueOf(caseDetails.getId()));

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
                            .content(requestBodyRevised)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = res.getResponse().getContentAsString();
        assertTrue(json.contains("confirmation_header"));
    }

    @Test
    public void givenRequestWithFl401CaseData_Perosnal_Service_lr_Submitted() throws Exception {

        String requestBody = ResourceLoader.loadJson(FL401_VALID_REQUEST_BODY_PERSONAL_SERVICE_LR);
        String requestBodyRevised = requestBody.replace("1711117454766387", String.valueOf(caseDetails.getId()));
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
                                            .content(requestBodyRevised)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = res.getResponse().getContentAsString();
        assertTrue(json.contains("confirmation_header"));
    }

}
