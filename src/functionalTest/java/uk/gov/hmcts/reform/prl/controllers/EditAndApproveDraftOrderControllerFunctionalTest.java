package uk.gov.hmcts.reform.prl.controllers;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class EditAndApproveDraftOrderControllerFunctionalTest {

    private final String userToken = "Bearer testToken";
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @MockBean
    private DraftAnOrderService draftAnOrderService;



    private static final String VALID_DRAFT_ORDER_REQUEST_BODY = "requests/draft-order-sdo-with-options-request.json";


    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenRequestBody_whenPopulate_draft_order_dropdown_then200Response() throws Exception {
        Map<String, Object> drafOrderMap = new HashMap<>();
        drafOrderMap.put("draftOrder1", "SDO");
        drafOrderMap.put("draftOrder2", "C21");
        Mockito
            .when(draftAnOrderService
                      .getDraftOrderDynamicList(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(drafOrderMap);
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        mockMvc.perform(post("/populate-draft-order-dropdown")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.draftOrder1").value("SDO"))
            .andExpect(jsonPath("data.draftOrder2").value("C21"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenJudge_admin_populate_draft_order_then200Response() throws Exception {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("orderName", "C21");
        caseDataMap.put("orderUploadedAsDraftFlag", "Yes");
        Mockito.when(draftAnOrderService.populateDraftOrderDocument(ArgumentMatchers.any()))
                .thenReturn(caseDataMap);
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        mockMvc.perform(post("/judge-or-admin-populate-draft-order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.orderName").value("C21"))
            .andExpect(jsonPath("data.orderUploadedAsDraftFlag").value("Yes"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenJudge_or_admin_edit_approve_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        mockMvc.perform(post("/judge-or-admin-edit-approve/about-to-submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.applicantCaseName").value("John Smith"))
            .andExpect(jsonPath("data.caseTypeOfApplication").value("FL401"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenJudge_or_admin_populate_draft_order_custom_fields_then200Response() throws Exception {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("appointedGuardianName", "John");
        caseDataMap.put("parentName", "Smith");
        Mockito.when(draftAnOrderService.populateDraftOrderCustomFields(ArgumentMatchers.any()))
            .thenReturn(caseDataMap);
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        mockMvc.perform(post("/judge-or-admin-populate-draft-order-custom-fields")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.appointedGuardianName").value("John"))
            .andExpect(jsonPath("data.parentName").value("Smith"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenJudge_or_admin_populate_draft_order_common_fields_then200Response() throws Exception {

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("orderType", "C21");
        caseDataMap.put("isTheOrderByConsent", "Yes");
        Mockito.when(draftAnOrderService
                         .populateCommonDraftOrderFields(ArgumentMatchers.any(),ArgumentMatchers.any()))
            .thenReturn(caseDataMap);
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        mockMvc.perform(post("/judge-or-admin-populate-draft-order-common-fields")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.orderType").value("C21"))
            .andExpect(jsonPath("data.isTheOrderByConsent").value("Yes"))
            .andReturn();
    }

    @Test
    public void givenRequestBodyWhenPostRequestTohandleEditAndApproveSubmitted() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        mockMvc.perform(post("/edit-and-approve/submitted")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                           .string(Matchers
                                       .containsString(EditAndApproveDraftOrderController.CONFIRMATION_HEADER)))
            .andReturn();
    }
}
