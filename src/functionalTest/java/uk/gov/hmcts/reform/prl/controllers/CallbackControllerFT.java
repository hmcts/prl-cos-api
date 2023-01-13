package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
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
    private UserService userService;

    @MockBean
    private AllTabServiceImpl allTabService;

    @MockBean
    private CourtFinderService courtLocatorService;

    @MockBean
    private LocationRefDataService locationRefDataService;

    private static final String MIAM_VALIDATION_REQUEST_ERROR = "requests/call-back-controller-miam-request-error.json";
    private static final String MIAM_VALIDATION_REQUEST_NO_ERROR = "requests/call-back-controller-miam-request-no-error.json";
    private static final String C100_GENERATE_DRAFT_DOC = "requests/call-back-controller-generate-save-doc.json";
    private static final String C100_ISSUE_AND_SEND = "requests/call-back-controller-issue-and-send-to-local-court.json";
    private static final String C100_UPDATE_APPLICATION = "requests/call-back-controller-update-application.json";
    private static final String C100_WITHDRAW_APPLICATION = "requests/call-back-controller-withdraw-application.json";
    private static final String C100_SEND_TO_GATEKEEPER = "requests/call-back-controller-send-to-gatekeeper.json";
    private static final String C100_RESEND_RPA = "requests/call-back-controller-resend-rpa.json";
    private static final String FL401_ABOUT_TO_SUBMIT_CREATION = "requests/call-back-controller-about-to-submit-case-creation.json";
    private static final String FL401_CASE_DATA = "requests/call-back-controller-fl401-case-data.json";

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
    public void givenC100Case_whenPostRequestToIssueAndSend_then200ResponseAndFinalDocsSaved() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_ISSUE_AND_SEND);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .state(State.CASE_ISSUE)
            .applicantsConfidentialDetails(List.of(element(ApplicantConfidentialityDetails.builder().build())))
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes).build())
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(organisationService.getApplicantOrganisationDetails(any(CaseData.class))).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(any(CaseData.class))).thenReturn(caseData);
        when(dgsService.generateDocument(any(String.class), any(CaseDetails.class), any(String.class))).thenReturn(generatedDocumentInfo);

        mockMvc.perform(post("/issue-and-send-to-local-court")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.finalDocument.document_filename").value("C100FinalDocument.pdf"))
            .andExpect(jsonPath("data.c1ADocument.document_filename").value("C1A_Document.pdf"))
            .andReturn();

    }

    @Test
    public void givenC100Case_whenCaseUpdateEndpoint_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_UPDATE_APPLICATION);

        mockMvc.perform(post("/update-application")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    }

    @Test
    public void givenC100Case_whenCaseWithdrawnEndpoint_then200ResponseAndDataContainsUpdatedTabData() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_WITHDRAW_APPLICATION);

        UserDetails userDetails = UserDetails.builder().forename("test").build();

        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        Map<String, Object> caseDataMap = Map.of(
            "welshLanguageRequirementsTable", "value",
            "otherProceedingsDetailsTable", "value",
            "allegationsOfHarmDomesticAbuseTable", "value",
            "summaryTabForOrderAppliedFor", "value",
            "miamTable", "value"
        );

        when(allTabService.getAllTabsFields(any(CaseData.class))).thenReturn(caseDataMap);

        MvcResult res = mockMvc.perform(post("/case-withdrawn-email-notification")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .header("Authorization", "auth")
                                          .content(requestBody)
                                          .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = res.getResponse().getContentAsString();

        assertTrue(json.contains("welshLanguageRequirementsTable"));
        assertTrue(json.contains("otherProceedingsDetailsTable"));
        assertTrue(json.contains("allegationsOfHarmDomesticAbuseTable"));
        assertTrue(json.contains("summaryTabForOrderAppliedFor"));
        assertTrue(json.contains("miamTable"));

    }

    @Test
    public void givenC100Case_whenSendToGateKeeperEndpoint_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_SEND_TO_GATEKEEPER);

        mockMvc.perform(post("/send-to-gatekeeper")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    }

    @Test
    public void givenC100Case_whenRpaResent_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_RESEND_RPA);

        mockMvc.perform(post("/update-party-details")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    }

    @Test
    public void updateApplicantAndChildName() throws Exception {
        String requestBody = ResourceLoader.loadJson(FL401_CASE_DATA);

        mockMvc.perform(post("/send-to-gatekeeper")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    }

    @Test
    public void givenFl401Case_whenAboutToSubmitCaseCreation_then200ResponseAndApplicantNameUpdated() throws Exception {
        String requestBody = ResourceLoader.loadJson(FL401_ABOUT_TO_SUBMIT_CREATION);

        UserDetails userDetails = UserDetails.builder().forename("test").build();

        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        Optional<Organisations> organisation = Optional.of(Organisations.builder().name("testName").build());

        when(organisationService.findUserOrganisation(any(String.class))).thenReturn(organisation);

        mockMvc.perform(post("/about-to-submit-case-creation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.applicantCaseName").value("thisIsATestName"))
            .andExpect(jsonPath("data.caseSolicitorName").value("test"))
            .andExpect(jsonPath("data.caseSolicitorOrgName").value("testName"))
            .andReturn();

    }

    @Test
    public void givenC100CasePrePopulateCourtDetailsWithValidCourt() throws Exception {
        when(courtLocatorService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(Court.builder().build());
        when(courtLocatorService.getEmailAddress(Mockito.any(Court.class))).thenReturn(Optional.of(CourtEmailAddress.builder()
            .address("123@gamil.com").build()));
        when(locationRefDataService.getCourtLocations(Mockito.anyString())).thenReturn(List.of(DynamicListElement
                                                                                                   .builder().build()));
        String requestBody = ResourceLoader.loadJson(C100_RESEND_RPA);
        mockMvc.perform(post("/pre-populate-court-details")
                            .header("Authorization", "auth")
                            .contentType(MediaType.APPLICATION_JSON).content(requestBody)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
                .andExpect(jsonPath("data.localCourtAdmin[0].value.email").value("123@gamil.com")).andReturn();
    }

    @Test
    public void givenC100CasePrePopulateCourtDetailsWithoutValidCourt() throws Exception {
        when(courtLocatorService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(null);
        when(locationRefDataService.getCourtLocations(Mockito.anyString())).thenReturn(List.of(DynamicListElement
                                                                                                   .builder().build()));
        String requestBody = ResourceLoader.loadJson(C100_RESEND_RPA);
        mockMvc.perform(post("/pre-populate-court-details")
                            .header("Authorization", "auth")
                            .contentType(MediaType.APPLICATION_JSON).content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.localCourtAdmin").doesNotHaveJsonPath()).andReturn();
    }
}
