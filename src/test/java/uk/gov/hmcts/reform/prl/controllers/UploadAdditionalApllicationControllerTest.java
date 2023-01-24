package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.ApplicantsListGenerator;
import uk.gov.hmcts.reform.prl.workflows.ApplicationConsiderationTimetableValidationWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;


@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class UploadAdditionalApllicationControllerTest {
    public static final String SOLICITOR_EMAIL = "unknown@test.com";
    @Mock
    private ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;

    @Mock
    private ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;

    @InjectMocks
    private UploadAdditionalApplicationController uploadAdditionalApplicationController;

    @Mock
    private UserService userService;

    @Mock
    private WorkflowResult workflowResult;

    @Mock
    private CaseSummaryTabService caseSummaryTab;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private SolicitorEmailService solicitorEmailService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicantsListGenerator applicantsListGenerator;
    @Mock
    private LocationRefDataService locationRefDataService;


    public static final String authToken = "Bearer TestAuthToken";

    private static final Map<String, Object> c100DraftMap = new HashMap<>();
    private static final Map<String, Object> c100DocsMap = new HashMap<>();

    private static final Map<String, Object> fl401DraftMap = new HashMap<>();
    private static final Map<String, Object> fl401DocsMap = new HashMap<>();

    @Before
    public void setUp() {

        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .roles(ROLES)
            .build();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        c100DraftMap.put(PrlAppsConstants.DRAFT_DOCUMENT_FIELD, "test");
        c100DraftMap.put(PrlAppsConstants.DRAFT_DOCUMENT_WELSH_FIELD, "test");

        c100DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_C8, "test");
        c100DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_C1A, "test");
        c100DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_FINAL, "test");
        c100DocsMap.put(DOCUMENT_FIELD_C8_WELSH, "test");
        c100DocsMap.put(DOCUMENT_FIELD_C1A_WELSH, "test");
        c100DocsMap.put(DOCUMENT_FIELD_FINAL_WELSH, "test");

        fl401DraftMap.put(PrlAppsConstants.DRAFT_DOCUMENT_FIELD, "test");
        fl401DraftMap.put(PrlAppsConstants.DRAFT_DOCUMENT_WELSH_FIELD, "test");

        fl401DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_C8, "test");
        fl401DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_FINAL, "test");
        fl401DocsMap.put(DOCUMENT_FIELD_C8_WELSH, "test");
        fl401DocsMap.put(DOCUMENT_FIELD_FINAL_WELSH, "test");
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(),Mockito.anyString()))
            .thenReturn("test-test-test-test-test-test");
    }

    @Test
    public void testPrePopulateApplicants() throws NotFoundException {

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant1).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .children(listOfChildren)
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("additionalApplicantsList", "test1 test22");

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(caseDataUpdated).build()).build();


        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            uploadAdditionalApplicationController.prePopulateApplicants(
                callbackRequest);

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertNull(caseDetailsRespnse.get("additionalApplicantsList"));
    }

    @Test
    public void testPrePopulateApplicantsNotFound() throws NotFoundException {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(caseDataUpdated).build()).build();


        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            uploadAdditionalApplicationController.prePopulateApplicants(
                callbackRequest);

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertNull(caseDetailsRespnse.get("additionalApplicantsList"));
    }
}
