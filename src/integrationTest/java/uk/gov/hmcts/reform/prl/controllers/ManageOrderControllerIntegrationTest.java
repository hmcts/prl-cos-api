package uk.gov.hmcts.reform.prl.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.services.AmendOrderService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ManageOrderControllerIntegrationTest {

    private MockMvc mockMvc;
    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String populatePreviewOrderEndpoint = "/populate-preview-order";
    private final String fetchChildDetailsEndpoint = "/fetch-child-details";
    private final String populateHeaderEndpoint = "/populate-header";
    private final String caseOrderEmailNotificationEndpoint = "/case-order-email-notification";
    private final String editAndApproveOrderSubmittedEndpoint = "/edit-and-approve/submitted";
    private final String manageOrdersEndpoint = "/manage-orders/about-to-submit";
    private final String addressValidationEndpoint = "/manage-orders/recipients-validations";

    private static final String VALID_REQUEST_BODY = "requests/call-back-controller.json";
    private static final String VALID_MANAGE_ORDER_REQUEST_BODY = "requests/manage-order-fetch-children-request.json";

    private static final String VALID_MANAGE_ORDER_AUTOMATED_HEARING_REQUEST_BODY = "requests/auto-hearing-case-data-request.json";

    private static final String VALID_MANAGE_ORDER_REQUEST_BODY_REVISED = "requests/manage-order-fetch-children-request-integration.json";

    private static final String VALID_REQUEST_RESPONDENT_LIP_WITH_ADDRESS = "requests/manage-order-fetch-children-request-integration.json";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    ManageOrderService manageOrderService;

    @MockBean
    ManageOrderEmailService manageOrderEmailService;

    @MockBean
    AmendOrderService amendOrderService;

    @MockBean
    RefDataUserService refDataUserService;

    @MockBean
    HearingDataService hearingDataService;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    AllTabServiceImpl allTabService;

    @MockBean
    RoleAssignmentService roleAssignmentService;

    @MockBean
    HearingService hearingService;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testPopulatePreviewOrder() throws Exception {
        String url = "/populate-preview-order";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testFetchOrderDetails() throws Exception {
        String url = "/fetch-order-details";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testPopulateHeader() throws Exception {
        String url = "/populate-header";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testCaseOrderEmailNotification() throws Exception {
        String url = "/case-order-email-notification";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(Yes).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            "authToken",
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap,
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testManageOrdersAboutToSubmit() throws Exception {
        String url = "/manage-orders/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("requests/judge-draft-sdo-order-request.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(any())).thenReturn(CaseData.builder()
                                                                                                 .manageOrders(
                                                                                                     ManageOrders.builder().isCaseWithdrawn(
                                                                                                             No)
                                                                                                         .build())
                                                                                                 .manageOrdersOptions(
                                                                                                     ManageOrdersOptionsEnum.createAnOrder)
                                                                                                 .build());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testShowPreviewOrder() throws Exception {
        String url = "/show-preview-order";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testAmendOrderMidEvent() throws Exception {
        String url = "/amend-order/mid-event";
        String jsonRequest = ResourceLoader.loadJson("requests/judge-draft-sdo-order-request.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(any())).thenReturn(CaseData.builder().build());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testManageOrdersWhenToServeMidEvent() throws Exception {
        String url = "/manage-orders/when-to-serve/mid-event";
        String jsonRequest = ResourceLoader.loadJson("requests/judge-draft-sdo-order-request.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(any())).thenReturn(CaseData.builder()
                                                                                                 .serveOrderData(
                                                                                                     ServeOrderData.builder()
                                                                                                         .doYouWantToServeOrder(
                                                                                                             Yes)
                                                                                                         .build())
                                                                                                 .build());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testManageOrderMidEvent() throws Exception {
        String url = "/manage-order/mid-event";
        String jsonRequest = ResourceLoader.loadJson("requests/judge-draft-sdo-order-request.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testCaseOrderEmailNotificationEndpointForAutoHearing() throws Exception {
        String url = "/case-order-email-notification";
        String jsonRequest = ResourceLoader.loadJson(VALID_MANAGE_ORDER_AUTOMATED_HEARING_REQUEST_BODY);

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testManageOrdersServeOrderMidEvent() throws Exception {
        String url = "/manage-orders/serve-order/mid-event";
        String jsonRequest = ResourceLoader.loadJson("requests/judge-draft-sdo-order-request.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testManageOrdersPrePopulateJudgeOrLaMidEvent() throws Exception {
        String url = "/manage-orders/pre-populate-judge-or-la/mid-event";
        String jsonRequest = ResourceLoader.loadJson("requests/judge-draft-sdo-order-request.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testCaseOrderSubmittedEndPointForAutoHearing() throws Exception {
        String url = "/manage-orders/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson(VALID_MANAGE_ORDER_AUTOMATED_HEARING_REQUEST_BODY);

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

    }

    @Test
    public void testValidateAndPopulateHearingData() throws Exception {
        String url = "/manage-orders/validate-populate-hearing-data";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testValidateRespondentAndOtherPersonAddress() throws Exception {
        String url = "/manage-orders/recipients-validations";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
