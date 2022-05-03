package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.sql.SQLOutput;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
public class CallbackControllerFT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CaseEventService caseEventService;

    @MockBean
    private SolicitorEmailService solicitorEmailService;

    @MockBean
    private CaseWorkerEmailService caseWorkerEmailService;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private DgsService dgsService;

    @MockBean
    private SendgridService sendgridService;



    @MockBean
    private AllTabServiceImpl allTabService;

    private static final String MIAM_VALIDATION_REQUEST_ERROR = "requests/call-back-controller-miam-request-error.json";
    private static final String MIAM_VALIDATION_REQUEST_NO_ERROR = "requests/call-back-controller-miam-request-no-error.json";
    private static final String C100_GENERATE_DRAFT_DOC = "requests/call-back-controller-generate-save-doc.json";

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenMiamAttendance_whenPostRequestToMiamValidatation_then200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(MIAM_VALIDATION_REQUEST_NO_ERROR);

        mockMvc.perform(post("/validate-miam-application-or-exemption")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();

    }

    @Test
    public void givenNoMiamAttendance_whenPostRequestToMiamValidatation_then200ResponseAndMiamError() throws Exception {
        String requestBody = ResourceLoader.loadJson(MIAM_VALIDATION_REQUEST_ERROR);

        mockMvc.perform(post("/validate-miam-application-or-exemption")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors")
                           .value("You cannot make this application unless the applicant has either attended, or is exempt from attending a MIAM"))
            .andReturn();

    }

    @Test
    public void givenC100EnglishCase_whenPostRequestToGenerateDraftDoc_then200ResponseAndDocumentSaved() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_GENERATE_DRAFT_DOC);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(organisationService.getApplicantOrganisationDetails(any(CaseData.class))).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(any(CaseData.class))).thenReturn(caseData);
        when(dgsService.generateDocument(any(String.class), any(CaseDetails.class), any(String.class))).thenReturn(generatedDocumentInfo);

        mockMvc.perform(post("/generate-save-draft-document")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.draftOrderDoc.document_filename").value("Draft_C100_application.pdf"))
            .andExpect(jsonPath("data.isEngDocGen").value("Yes"))
            .andReturn();
    }

    @Test
    public void givenC100WelshCase_whenPostRequestToIssueAndSend_then200ResponseAndFinalDocsSaved() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_GENERATE_DRAFT_DOC);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicantsConfidentialDetails(List.of(element(ApplicantConfidentialityDetails.builder().build())))
            .allegationsOfHarmYesNo(YesOrNo.Yes)
            .welshLanguageRequirement(YesOrNo.Yes)
            .welshLanguageRequirementApplication(LanguagePreference.welsh)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(organisationService.getApplicantOrganisationDetails(any(CaseData.class))).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(any(CaseData.class))).thenReturn(caseData);
        when(dgsService.generateDocument(any(String.class), any(CaseDetails.class), any(String.class))).thenReturn(generatedDocumentInfo);

        MvcResult m = mockMvc.perform(post("/issue-and-send-to-local-court")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
//            .andExpect(jsonPath("data.draftOrderDoc.document_filename").value("Draft_C100_application.pdf"))
//            .andExpect(jsonPath("data.isWelshDocGen").value("Yes"))
            .andReturn();

        System.out.println(m.getResponse().getContentAsString());
    }




//    @Test
//    public void givenReturnedFromIssuedState_shouldReturnIssuedState() throws Exception {
//        String requestBody = ResourceLoader.loadJson(RESUBMIT_REQUEST);
//
//        List<CaseEventDetail> caseEvents = List.of(
//            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
//            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
//            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
//            CaseEventDetail.builder().stateId(State.CASE_ISSUE.getValue()).build(),
//            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
//        );
//
//        when(caseEventService.findEventsForCase(any(String.class))).thenReturn(caseEvents);
//
//        mockMvc.perform(post("/resubmit-application")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "auth")
//                            .content(requestBody)
//                            .accept(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("data.state").value(State.CASE_ISSUE.getValue()))
//            .andReturn();
//
//    }

}
