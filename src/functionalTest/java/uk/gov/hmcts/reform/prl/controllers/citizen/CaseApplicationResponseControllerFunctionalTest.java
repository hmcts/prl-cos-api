package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseNotificationEmailService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseApplicationResponseControllerFunctionalTest {

    private final String userToken = "Bearer testToken";
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private CoreCaseDataApi coreCaseDataApi;
    @MockBean
    private CaseService caseService;
    @MockBean
    private CitizenResponseNotificationEmailService citizenResponseNotificationEmailService;

    @MockBean
    private DocumentGenService documentGenService;


    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenRequestBody_whenGenerate_c7document_then200Response() throws Exception {
        Element<PartyDetails> partyDetailsElement = element(PartyDetails.builder().firstName("test").build());
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .respondents(List.of(partyDetailsElement))
            .build();

        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());

        when(coreCaseDataApi.getCase(anyString(), anyString(), anyString())).thenReturn(CaseDetails.builder().data(
                caseDataMap).state(State.JUDICIAL_REVIEW.getValue())
                                                                                            .id(123488888L).createdDate(
                LocalDateTime.now()).lastModified(LocalDateTime.now()).build());
        mockMvc.perform(post("/1234/1234/generate-c7document")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .header("serviceAuthorization", "auth")
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    public void givenRequestBody_whenGenerate_c7document_final_then200Response() throws Exception {
        Element<PartyDetails> partyDetailsElement = element(PartyDetails.builder().firstName("test").build());
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .respondents(List.of(partyDetailsElement))
            .build();

        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());

        when(coreCaseDataApi.getCase(anyString(), anyString(), anyString())).thenReturn(CaseDetails.builder().data(
                caseDataMap).state(State.JUDICIAL_REVIEW.getValue())
                                                                                            .id(123488888L).createdDate(
                LocalDateTime.now()).lastModified(LocalDateTime.now()).build());
        mockMvc.perform(post("/1234/1234/generate-c7document-final")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .header("serviceAuthorization", "auth")
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

}
