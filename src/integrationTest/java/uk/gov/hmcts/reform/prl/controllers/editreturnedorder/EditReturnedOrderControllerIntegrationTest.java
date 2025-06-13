package uk.gov.hmcts.reform.prl.controllers.editreturnedorder;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EditReturnedOrderService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.AUTHORIZATION;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class EditReturnedOrderControllerIntegrationTest {

    private MockMvc mockMvc;

    @MockitoBean
    EditReturnedOrderService editReturnedOrderService;

    @MockitoBean
    AuthorisationService authorisationService;

    @MockitoBean
    AllTabServiceImpl allTabService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

    }

    @Test
    public void testHandleAboutToStart() throws Exception {
        String url = "/edit-returned-order/about-to-start";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

        mockMvc.perform(
                        post(url)
                                .header(AUTHORIZATION, "Bearer testAuthToken")
                                .header("ServiceAuthorization", "testServiceAuthToken")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testPopulateInstructionsToSolicitor() throws Exception {
        String url = "/edit-returned-order/mid-event/populate-instructions";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

        mockMvc.perform(
                        post(url)
                                .header(AUTHORIZATION, "Bearer testAuthToken")
                                .header("ServiceAuthorization", "testServiceAuthToken")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testHandleEditAndReturnedSubmitted() throws Exception {

        Map<String, Object> caseDatMap = new HashMap<>();
        caseDatMap.put("caseId", 123L);
        caseDatMap.put("manageOrdersOptions", "amendOrderUnderSlipRule");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        Mockito.when(allTabService.getStartAllTabsUpdate(any())).thenReturn(new StartAllTabsUpdateDataContent(
                "testAuthToken",
                null,
                null,
                caseDatMap,
                null,
                null
        ));

        String url = "/edit-returned-order/submitted";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        mockMvc.perform(
                        post(url)
                                .header(AUTHORIZATION, "Bearer testAuthToken")
                                .header("ServiceAuthorization", "testServiceAuthToken")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();
    }
}
