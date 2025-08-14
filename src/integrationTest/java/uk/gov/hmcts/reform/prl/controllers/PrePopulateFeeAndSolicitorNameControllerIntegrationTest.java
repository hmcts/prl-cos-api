package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.C100DocumentTemplateFinderService;
import uk.gov.hmcts.reform.prl.services.validators.SubmitAndPayChecker;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class PrePopulateFeeAndSolicitorNameControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    FeeService feeService;

    @MockBean
    UserService userService;

    @MockBean
    CourtFinderService courtLocatorService;

    @MockBean
    SubmitAndPayChecker submitAndPayChecker;

    @MockBean
    DgsService dgsService;

    @MockBean
    C100DocumentTemplateFinderService c100DocumentTemplateFinderService;

    @MockBean
    OrganisationService organisationService;

    @MockBean
    DocumentLanguageService documentLanguageService;

    @MockBean
    AuthorisationService authorisationService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testPrePopulateSolicitorAndFees() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(submitAndPayChecker.hasMandatoryCompleted(any(CaseData.class))).thenReturn(true);
        when(feeService.fetchFeeDetails(any(FeeType.class))).thenReturn(FeeResponse.builder()
                                                                            .amount(BigDecimal.valueOf(215))
                                                                            .build());
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .email("solicitor@example.com")
                                                                     .forename("Solicitor Name")
                                                                     .build());
        when(courtLocatorService.getNearestFamilyCourt(any(CaseData.class))).thenReturn(Court.builder()
                                                                                            .courtName("Court Name")
                                                                                            .build());
        when(organisationService.getApplicantOrganisationDetails(any(CaseData.class))).thenReturn(CaseData.builder().build());
        when(organisationService.getRespondentOrganisationDetails(any(CaseData.class))).thenReturn(CaseData.builder().build());

        when(c100DocumentTemplateFinderService.findFinalDraftDocumentTemplate(
            any(CaseData.class),
            anyBoolean()
        )).thenReturn("template");
        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(DocumentLanguage.builder()
                                                                                          .isGenEng(true)
                                                                                          .isGenWelsh(true)
                                                                                          .build());
        when(dgsService.generateDocument(anyString(), any(CaseDetails.class), anyString())).thenReturn(
            GeneratedDocumentInfo.builder()
                .createdOn("2021-01-01")
                .url("url")
                .mimeType("application/pdf")
                .build());
        when(dgsService.generateWelshDocument(anyString(), any(CaseDetails.class), anyString())).thenReturn(
            GeneratedDocumentInfo.builder()
                .createdOn("2021-01-01")
                .url("url")
                .mimeType("application/pdf")
                .build());

        CallbackRequest callbackRequest = new CallbackRequest();
        callbackRequest.setCaseDetails(CaseDetails.builder()
                                           .caseData(CaseData.builder().build())
                                           .build());

        mockMvc.perform(post("/getSolicitorAndFeeDetails")
                            .header("Authorization", "Bearer token")
                            .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "s2sToken")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(callbackRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.solicitorName").value("Solicitor Name"))
            .andExpect(jsonPath("$.data.feeAmount").value("£215"))
            .andExpect(jsonPath("$.data.courtName").value("Court Name"));
    }
}
