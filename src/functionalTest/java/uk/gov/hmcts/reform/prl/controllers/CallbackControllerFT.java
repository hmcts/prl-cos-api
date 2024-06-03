package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.SendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.GatekeepingDetails;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.GatekeepingDetailsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TRUE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
@SuppressWarnings("unchecked")
public class CallbackControllerFT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @MockBean
    private CaseEventService caseEventService;

    @MockBean
    private RoleAssignmentApi roleAssignmentApi;

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

    @MockBean
    private GatekeepingDetailsService gatekeepingDetailsService;

    @MockBean
    private AuthorisationService authorisationService;

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
    private static final String C100_SEND_TO_GATEKEEPERJUDGE = "requests/call-back-controller-send-to-gatekeeperForJudge.json";

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

    @Test
    public void givenMiamAttendance_whenPostRequestToMiamValidatation_then200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(MIAM_VALIDATION_REQUEST_NO_ERROR);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        mockMvc.perform(post("/validate-miam-application-or-exemption")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    }

    @Test
    public void givenNoMiamAttendance_whenPostRequestToMiamValidatation_then200ResponseAndMiamError() throws Exception {
        String requestBody = ResourceLoader.loadJson(MIAM_VALIDATION_REQUEST_ERROR);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        mockMvc.perform(post("/validate-miam-application-or-exemption")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
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
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        mockMvc.perform(post("/generate-save-draft-document")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.submitAndPayDownloadApplicationLink.document_filename").value("Draft_C100_application.pdf"))
            .andExpect(jsonPath("data.isEngDocGen").value("Yes"))
            .andReturn();
    }

    @Test
    public void givenC100Case_whenPostRequestToIssueAndSend_then200ResponseAndFinalDocsSaved() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_ISSUE_AND_SEND);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .state(State.CASE_ISSUED)
            .applicantsConfidentialDetails(List.of(element(ApplicantConfidentialityDetails.builder().build())))
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes).build())
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(organisationService.getApplicantOrganisationDetails(any(CaseData.class))).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(any(CaseData.class))).thenReturn(caseData);
        when(dgsService.generateDocument(any(String.class), any(CaseDetails.class), any(String.class))).thenReturn(generatedDocumentInfo);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        mockMvc.perform(post("/issue-and-send-to-local-court")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
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
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        mockMvc.perform(post("/update-application")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
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
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        MvcResult res = mockMvc.perform(post("/case-withdrawn-about-to-submit")
                                          .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
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
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(userService.getUserByEmailId(anyString(), anyString())).thenReturn(List.of(UserDetails.builder().build()));
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().roles(List.of("caseworker-privatelaw-solicitor")).build());
        when(roleAssignmentApi.updateRoleAssignment(any(), any(), any(), any(RoleAssignmentRequest.class)))
            .thenReturn(RoleAssignmentResponse.builder().build());
        mockMvc.perform(post("/send-to-gatekeeper")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    }

    @Test
    public void givenC100Case_whenRpaResent_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_RESEND_RPA);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        mockMvc.perform(post("/update-party-details")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    }

    @Test
    public void givenFl401Case_whenAboutToSubmitCaseCreation_then200ResponseAndApplicantNameUpdated() throws Exception {
        UserDetails userDetails = UserDetails.builder().forename("test").build();

        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        Optional<Organisations> organisation = Optional.of(Organisations.builder().name("testName").build());

        when(organisationService.findUserOrganisation(any(String.class))).thenReturn(organisation);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse roleAssignmentResponse =
            new uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName("allocated-magistrate");
        when(roleAssignmentApi.getRoleAssignments(any(),any(),any(),any()))
            .thenReturn(RoleAssignmentServiceResponse.builder()
                            .roleAssignmentResponse(List.of(roleAssignmentResponse))
                            .build());
        String requestBody = ResourceLoader.loadJson(FL401_ABOUT_TO_SUBMIT_CREATION);
        mockMvc.perform(post("/about-to-submit-case-creation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
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
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        String requestBody = ResourceLoader.loadJson(C100_RESEND_RPA);
        mockMvc.perform(post("/pre-populate-court-details")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
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
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        String requestBody = ResourceLoader.loadJson(C100_RESEND_RPA);
        mockMvc.perform(post("/pre-populate-court-details")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .contentType(MediaType.APPLICATION_JSON).content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.localCourtAdmin").doesNotHaveJsonPath()).andReturn();
    }

    @Test
    public void testGatekeepingDetailsWhenLegalAdvisorOptionSelected_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_SEND_TO_GATEKEEPER);

        when(gatekeepingDetailsService.getGatekeepingDetails(Mockito.any(Map.class), Mockito.any(DynamicList.class), Mockito.any(
            RefDataUserService.class))).thenReturn(
            GatekeepingDetails.builder().isJudgeOrLegalAdviserGatekeeping(SendToGatekeeperTypeEnum.legalAdviser).build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(userService.getUserByEmailId(anyString(), anyString())).thenReturn(List.of(UserDetails.builder().build()));
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().roles(List.of("caseworker-privatelaw-solicitor")).build());
        mockMvc.perform(post("/send-to-gatekeeper")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .contentType(MediaType.APPLICATION_JSON).content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.gatekeepingDetails.isJudgeOrLegalAdviserGatekeeping").value("legalAdviser")).andReturn();
    }

    @Test
    public void testGatekeepingDetailsWhenJudgeOptionSelected_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_SEND_TO_GATEKEEPERJUDGE);
        when(gatekeepingDetailsService.getGatekeepingDetails(Mockito.any(Map.class), Mockito.any(DynamicList.class), Mockito.any(
            RefDataUserService.class))).thenReturn(
            GatekeepingDetails.builder().isJudgeOrLegalAdviserGatekeeping(SendToGatekeeperTypeEnum.judge).build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(userService.getUserByEmailId(anyString(), anyString())).thenReturn(List.of(UserDetails.builder().build()));
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().roles(List.of("caseworker-privatelaw-solicitor")).build());
        mockMvc.perform(post("/send-to-gatekeeper")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .contentType(MediaType.APPLICATION_JSON).content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.gatekeepingDetails.isJudgeOrLegalAdviserGatekeeping").value("judge")).andReturn();
    }

    @Test
    public void testAttachScanDocsWaChange() throws Exception {

        String requestBody = ResourceLoader.loadJson(C100_SEND_TO_GATEKEEPERJUDGE);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/attach-scan-docs/about-to-submit")
            .then()
            .body("data.manageDocumentsRestrictedFlag", equalTo(TRUE),
                  "data.manageDocumentsTriggeredBy", equalTo(STAFF))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }


}
