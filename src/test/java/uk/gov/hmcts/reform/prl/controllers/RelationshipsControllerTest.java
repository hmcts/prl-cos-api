package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonRelationshipToChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Relations;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;
import uk.gov.hmcts.reform.prl.utils.ApplicantsListGenerator;
import uk.gov.hmcts.reform.prl.workflows.ApplicationConsiderationTimetableValidationWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class RelationshipsControllerTest {
    public static final String SOLICITOR_EMAIL = "unknown@test.com";
    @Mock
    private ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;

    @Mock
    private ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;

    @InjectMocks
    private RelationshipsController relationshipsController;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicantsListGenerator applicantsListGenerator;
    @Mock
    private UploadAdditionalApplicationService uploadAdditionalApplicationService;


    public static final String authToken = "Bearer TestAuthToken";


    @Test
    public void testPrePopulateApplicantsToChildRelation() throws NotFoundException {

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant1).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .newChildDetails(listOfChildren)
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantChildRelationsList", "test1 test22");

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateApplicantToChildRelation("test",
                                                                        callbackRequest);

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertNotNull(caseDetailsRespnse.get("applicantChildRelationsList"));
    }

    @Test
    public void testPrePopulateAmendApplicantsToChildRelation() throws NotFoundException {

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant1).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        List<Element<ChildrenAndApplicantRelation>> childrenApplicantRelations = new ArrayList<Element<ChildrenAndApplicantRelation>>();
        childrenApplicantRelations.add(Element.<ChildrenAndApplicantRelation>builder().value(
            ChildrenAndApplicantRelation.builder().build()).build());
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().childAndApplicantRelations(childrenApplicantRelations).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantChildRelationsList", "test1 test22");

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateAmendApplicantToChildRelation("test",
                                                                        callbackRequest);

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertNotNull(caseDetailsRespnse.get("applicantChildRelationsList"));
    }


    @Test
    public void testPrePopulateAmendApplicantsToChildRelation_scenario2() throws NotFoundException {

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant1).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        List<Element<ChildrenAndApplicantRelation>> childrenApplicantRelations = new ArrayList<Element<ChildrenAndApplicantRelation>>();
        childrenApplicantRelations.add(Element.<ChildrenAndApplicantRelation>builder().value(
            ChildrenAndApplicantRelation.builder().applicantId(String.valueOf(wrappedApplicant.getId())).childId(String.valueOf(
                wrappedChildren.getId())).applicantFullName("test").childFullName("test").build()).build());
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().childAndApplicantRelations(childrenApplicantRelations).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantChildRelationsList", "test1 test22");

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateAmendApplicantToChildRelation("test",
                                                                        callbackRequest);

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertNotNull(caseDetailsRespnse.get("applicantChildRelationsList"));
    }

    @Test
    public void testPopulateApplicantsToChildRelation() throws NotFoundException {

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant1).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        ChildrenAndApplicantRelation childrenAndApplicantRelation = ChildrenAndApplicantRelation.builder()
            .applicantFullName("Test")
            .childFullName("Name").childAndApplicantRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndApplicantRelation> wrappedChildrenAndApplicantRelation =
            Element.<ChildrenAndApplicantRelation>builder().value(childrenAndApplicantRelation).build();
        List<Element<ChildrenAndApplicantRelation>> listOfwrappedChildrenAndApplicantRelation =
            Collections.singletonList(wrappedChildrenAndApplicantRelation);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().childAndApplicantRelations(listOfwrappedChildrenAndApplicantRelation)
                           .buffChildAndApplicantRelations(listOfwrappedChildrenAndApplicantRelation).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("childAndApplicantRelations", "test1 test22");

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();


        assertNotNull(relationshipsController.populateApplicantToChildRelation("test",
                                                                                 callbackRequest));
    }

    @Test
    public void testPrePopulateRespondentsToChildRelation() throws NotFoundException {

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant1).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(applicantList)
            .newChildDetails(listOfChildren)
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantChildRelationsList", "test1 test22");

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateRespondentToChildRelation("test",
                                                                        callbackRequest);

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertNotNull(caseDetailsRespnse.get("applicantChildRelationsList"));
    }

    @Test
    public void testPrePopulateAmendRespondentsToChildRelation() throws NotFoundException {

        PartyDetails respondent = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondent);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();
        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        ChildrenAndRespondentRelation relation = ChildrenAndRespondentRelation
            .builder()
            .childId(wrappedChildren.getId().toString())
            .respondentId(wrappedRespondent.getId().toString())
            .childAndRespondentRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .build();
        Element<ChildrenAndRespondentRelation> wrappedRelation =
            Element.<ChildrenAndRespondentRelation>builder().value(relation).id(UUID.randomUUID()).build();
        List<Element<ChildrenAndRespondentRelation>> relationList = Collections.singletonList(wrappedRelation);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().childAndRespondentRelations(relationList).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateAmendRespondentToChildRelation("test",
                                                                              callbackRequest);

        ChildrenAndRespondentRelation expectedRelation = ChildrenAndRespondentRelation
            .builder()
            .childFullName("Test Name")
            .childId(wrappedChildren.getId().toString())
            .respondentId(wrappedRespondent.getId().toString())
            .respondentFullName("test1 test22")
            .childAndRespondentRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .build();
        List<Element<ChildrenAndRespondentRelation>> expectedRelations = Collections
            .singletonList(Element.<ChildrenAndRespondentRelation>builder().value(expectedRelation).build());

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertEquals(expectedRelations, caseDetailsRespnse.get("buffChildAndRespondentRelations"));
    }

    @Test
    public void testPrePopulateAmendRespondentsToChildRelationWhenNoExistingRelations() throws NotFoundException {

        PartyDetails respondent = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondent);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();
        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateAmendRespondentToChildRelation("test",
                                                                              callbackRequest);

        ChildrenAndRespondentRelation expectedRelation = ChildrenAndRespondentRelation
            .builder()
            .childFullName("Test Name")
            .childId(wrappedChildren.getId().toString())
            .respondentId(wrappedRespondent.getId().toString())
            .respondentFullName("test1 test22")
            .childAndRespondentRelation(null)
            .childLivesWith(null)
            .build();
        List<Element<ChildrenAndRespondentRelation>> expectedRelations = Collections
            .singletonList(Element.<ChildrenAndRespondentRelation>builder().value(expectedRelation).build());

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertEquals(expectedRelations, caseDetailsRespnse.get("buffChildAndRespondentRelations"));
    }

    @Test
    public void testPrePopulateAmendRespondentsToChildRelationWhenIdsDoNotMatch() throws NotFoundException {

        PartyDetails respondent = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondent);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();
        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        ChildrenAndRespondentRelation relation = ChildrenAndRespondentRelation
            .builder()
            .childId(UUID.randomUUID().toString())
            .respondentId(UUID.randomUUID().toString())
            .childAndRespondentRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .build();
        Element<ChildrenAndRespondentRelation> wrappedRelation =
            Element.<ChildrenAndRespondentRelation>builder().value(relation).id(UUID.randomUUID()).build();
        List<Element<ChildrenAndRespondentRelation>> relationList = Collections.singletonList(wrappedRelation);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().childAndRespondentRelations(relationList).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateAmendRespondentToChildRelation("test",
                                                                              callbackRequest);

        ChildrenAndRespondentRelation expectedRelation = ChildrenAndRespondentRelation
            .builder()
            .childFullName("Test Name")
            .childId(wrappedChildren.getId().toString())
            .respondentId(wrappedRespondent.getId().toString())
            .respondentFullName("test1 test22")
            .childAndRespondentRelation(null)
            .childLivesWith(null)
            .build();
        List<Element<ChildrenAndRespondentRelation>> expectedRelations = Collections
            .singletonList(Element.<ChildrenAndRespondentRelation>builder().value(expectedRelation).build());

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertEquals(expectedRelations, caseDetailsRespnse.get("buffChildAndRespondentRelations"));
    }

    @Test
    public void testPrePopulateAmendRespondentsToChildRelationWhenChildIdDoesNotMatch() throws NotFoundException {

        PartyDetails respondent = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondent);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        ChildrenAndRespondentRelation relation = ChildrenAndRespondentRelation
            .builder()
            .childId(UUID.randomUUID().toString())
            .respondentId(wrappedRespondent.getId().toString())
            .childAndRespondentRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .build();
        Element<ChildrenAndRespondentRelation> wrappedRelation =
            Element.<ChildrenAndRespondentRelation>builder().value(relation).id(UUID.randomUUID()).build();
        List<Element<ChildrenAndRespondentRelation>> relationList = Collections.singletonList(wrappedRelation);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().childAndRespondentRelations(relationList).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateAmendRespondentToChildRelation("test",
                                                                              callbackRequest);

        ChildrenAndRespondentRelation expectedRelation = ChildrenAndRespondentRelation
            .builder()
            .childFullName("Test Name")
            .childId(wrappedChildren.getId().toString())
            .respondentId(wrappedRespondent.getId().toString())
            .respondentFullName("test1 test22")
            .childAndRespondentRelation(null)
            .childLivesWith(null)
            .build();
        List<Element<ChildrenAndRespondentRelation>> expectedRelations = Collections
            .singletonList(Element.<ChildrenAndRespondentRelation>builder().value(expectedRelation).build());

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertEquals(expectedRelations, caseDetailsRespnse.get("buffChildAndRespondentRelations"));
    }

    @Test
    public void testPopulateRespondentToChildRelation() throws NotFoundException {

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().id(UUID.randomUUID()).value(applicant1).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().id(UUID.randomUUID()).value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        ChildrenAndRespondentRelation childrenAndRespondentRelation = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name").childAndRespondentRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .childAndRespondentRelationOtherDetails("test")
            .build();

        Element<ChildrenAndRespondentRelation> wrappedChildrenAndApplicantRelation =
            Element.<ChildrenAndRespondentRelation>builder().value(childrenAndRespondentRelation).build();
        List<Element<ChildrenAndRespondentRelation>> listOfwrappedChildrenAndApplicantRelation =
            Collections.singletonList(wrappedChildrenAndApplicantRelation);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().buffChildAndRespondentRelations(listOfwrappedChildrenAndApplicantRelation).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        ChildrenAndRespondentRelation expectedRelation = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name")
            .childAndRespondentRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .childAndRespondentRelationOtherDetails(null)
            .build();
        List<Element<ChildrenAndRespondentRelation>> expectedRelationList = Collections.singletonList(Element.<ChildrenAndRespondentRelation>builder()
                                                                                                          .value(expectedRelation).build());

        AboutToStartOrSubmitCallbackResponse response = relationshipsController.populateRespondentToChildRelation(
            "test",
            callbackRequest
        );
        assertEquals(expectedRelationList, response.getData().get("childAndRespondentRelations"));
    }


    @Test
    public void testPopulateRespondentToChildRelationWhenRelationIsOther() throws NotFoundException {

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().id(UUID.randomUUID()).value(applicant1).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().id(UUID.randomUUID()).value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        ChildrenAndRespondentRelation childrenAndRespondentRelation = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Test")
            .childFullName("Name").childAndRespondentRelation(RelationshipsEnum.other)
            .childAndRespondentRelationOtherDetails("test")
            .childLivesWith(YesOrNo.Yes)
            .build();

        Element<ChildrenAndRespondentRelation> wrappedChildrenAndApplicantRelation =
            Element.<ChildrenAndRespondentRelation>builder().value(childrenAndRespondentRelation).build();
        List<Element<ChildrenAndRespondentRelation>> listOfwrappedChildrenAndApplicantRelation =
            Collections.singletonList(wrappedChildrenAndApplicantRelation);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().buffChildAndRespondentRelations(listOfwrappedChildrenAndApplicantRelation).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        List<Element<ChildrenAndRespondentRelation>> expectedRelation = Collections.singletonList(
            Element.<ChildrenAndRespondentRelation>builder().value(childrenAndRespondentRelation).build());
        AboutToStartOrSubmitCallbackResponse response = relationshipsController.populateRespondentToChildRelation(
            "test",
            callbackRequest
        );
        assertEquals(listOfwrappedChildrenAndApplicantRelation, response.getData().get("childAndRespondentRelations"));
    }


    @Test
    public void testPrePopulateOtherPeopleToChildRelation() throws NotFoundException {

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        OtherPersonRelationshipToChild personRelationshipToChild = OtherPersonRelationshipToChild.builder()
            .personRelationshipToChild("Test relationship")
            .build();

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("firstName")
            .lastName("lastName")
            .gender(Gender.male)
            .isDateOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1989, 10, 20))
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .placeOfBirth("London")
            .isCurrentAddressKnown(YesOrNo.Yes)
            .address(Address.builder()
                         .addressLine1("add1")
                         .postCode("postcode")
                         .build())
            .isAddressConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("email@email.com")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("02086656656")
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .otherPersonRelationshipToChildren(List.of(element(personRelationshipToChild)))
            .build();


        Element<PartyDetails> wrappedPartyDetails =
            Element.<PartyDetails>builder().id(UUID.randomUUID()).value(partyDetails).build();
        List<Element<PartyDetails>> listOfPartyDetails = Collections.singletonList(wrappedPartyDetails);
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .otherPartyInTheCaseRevised(listOfPartyDetails)
            .newChildDetails(listOfChildren)
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("buffChildAndOtherPeopleRelations", "test1 test22");

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                       .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateOtherPeopleToChildRelation("test",
                callbackRequest);

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertNotNull(caseDetailsRespnse.get("buffChildAndOtherPeopleRelations"));
    }

    @Test
    public void testPrePopulateAmendOtherPeopleToChildRelation() throws NotFoundException {

        PartyDetails otherPeople = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedOtherPeople = Element.<PartyDetails>builder().value(otherPeople).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> otherPeopleList = Collections.singletonList(wrappedOtherPeople);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();
        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        ChildrenAndOtherPeopleRelation relation = ChildrenAndOtherPeopleRelation
            .builder()
            .childId(wrappedChildren.getId().toString())
            .otherPeopleId(wrappedOtherPeople.getId().toString())
            .childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();
        Element<ChildrenAndOtherPeopleRelation> wrappedRelation =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(relation).id(UUID.randomUUID()).build();
        List<Element<ChildrenAndOtherPeopleRelation>> relationList = Collections.singletonList(wrappedRelation);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .otherPartyInTheCaseRevised(otherPeopleList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().childAndOtherPeopleRelations(relationList).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateAmendOtherPeopleToChildRelation("test",
                                                                              callbackRequest);

        ChildrenAndOtherPeopleRelation expectedRelation = ChildrenAndOtherPeopleRelation
            .builder()
            .childFullName("Test Name")
            .childId(wrappedChildren.getId().toString())
            .otherPeopleId(wrappedOtherPeople.getId().toString())
            .otherPeopleFullName("test1 test22")
            .childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();
        List<Element<ChildrenAndOtherPeopleRelation>> expectedRelations = Collections
            .singletonList(Element.<ChildrenAndOtherPeopleRelation>builder().value(expectedRelation).build());

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertEquals(expectedRelations, caseDetailsRespnse.get("buffChildAndOtherPeopleRelations"));
    }

    @Test
    public void testPrePopulateAmendOtherPeopleToChildRelationWhenNoExistingRelations() throws NotFoundException {

        PartyDetails otherPeople = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedOtherPeople = Element.<PartyDetails>builder().value(otherPeople).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> otherPeopleList = Collections.singletonList(wrappedOtherPeople);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();
        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .otherPartyInTheCaseRevised(otherPeopleList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateAmendOtherPeopleToChildRelation("test",
                                                                              callbackRequest);

        ChildrenAndOtherPeopleRelation expectedRelation = ChildrenAndOtherPeopleRelation
            .builder()
            .childFullName("Test Name")
            .childId(wrappedChildren.getId().toString())
            .otherPeopleId(wrappedOtherPeople.getId().toString())
            .otherPeopleFullName("test1 test22")
            .childAndOtherPeopleRelation(null)
            .childLivesWith(null)
            .isChildLivesWithPersonConfidential(null)
            .build();
        List<Element<ChildrenAndOtherPeopleRelation>> expectedRelations = Collections
            .singletonList(Element.<ChildrenAndOtherPeopleRelation>builder().value(expectedRelation).build());

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertEquals(expectedRelations, caseDetailsRespnse.get("buffChildAndOtherPeopleRelations"));
    }

    @Test
    public void testPrePopulateAmendOtherPeopleToChildRelationWhenIdsDoNotMatch() throws NotFoundException {

        PartyDetails otherPeople = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedOtherPeople = Element.<PartyDetails>builder().value(otherPeople).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> otherPeopleList = Collections.singletonList(wrappedOtherPeople);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();
        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        ChildrenAndOtherPeopleRelation relation = ChildrenAndOtherPeopleRelation
            .builder()
            .childId(UUID.randomUUID().toString())
            .otherPeopleId(UUID.randomUUID().toString())
            .childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();
        Element<ChildrenAndOtherPeopleRelation> wrappedRelation =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(relation).id(UUID.randomUUID()).build();
        List<Element<ChildrenAndOtherPeopleRelation>> relationList = Collections.singletonList(wrappedRelation);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .otherPartyInTheCaseRevised(otherPeopleList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().childAndOtherPeopleRelations(relationList).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateAmendOtherPeopleToChildRelation("test",
                                                                              callbackRequest);

        ChildrenAndOtherPeopleRelation expectedRelation = ChildrenAndOtherPeopleRelation
            .builder()
            .childFullName("Test Name")
            .childId(wrappedChildren.getId().toString())
            .otherPeopleId(wrappedOtherPeople.getId().toString())
            .otherPeopleFullName("test1 test22")
            .childAndOtherPeopleRelation(null)
            .childLivesWith(null)
            .build();
        List<Element<ChildrenAndOtherPeopleRelation>> expectedRelations = Collections
            .singletonList(Element.<ChildrenAndOtherPeopleRelation>builder().value(expectedRelation).build());

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertEquals(expectedRelations, caseDetailsRespnse.get("buffChildAndOtherPeopleRelations"));
    }

    @Test
    public void testPrePopulateAmendOtherPeopleToChildRelationWhenChildIdDoesNotMatch() throws NotFoundException {

        PartyDetails otherPeople = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedOtherPeople = Element.<PartyDetails>builder().value(otherPeople).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> otherPeopleList = Collections.singletonList(wrappedOtherPeople);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().value(child).id(UUID.randomUUID()).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        ChildrenAndOtherPeopleRelation relation = ChildrenAndOtherPeopleRelation
            .builder()
            .childId(UUID.randomUUID().toString())
            .otherPeopleId(wrappedOtherPeople.getId().toString())
            .childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();
        Element<ChildrenAndOtherPeopleRelation> wrappedRelation =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(relation).id(UUID.randomUUID()).build();
        List<Element<ChildrenAndOtherPeopleRelation>> relationList = Collections.singletonList(wrappedRelation);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .otherPartyInTheCaseRevised(otherPeopleList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().childAndOtherPeopleRelations(relationList).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            relationshipsController.prePopulateAmendOtherPeopleToChildRelation("test",
                                                                              callbackRequest);

        ChildrenAndOtherPeopleRelation expectedRelation = ChildrenAndOtherPeopleRelation
            .builder()
            .childFullName("Test Name")
            .childId(wrappedChildren.getId().toString())
            .otherPeopleId(wrappedOtherPeople.getId().toString())
            .otherPeopleFullName("test1 test22")
            .childAndOtherPeopleRelation(null)
            .childLivesWith(null)
            .build();
        List<Element<ChildrenAndOtherPeopleRelation>> expectedRelations = Collections
            .singletonList(Element.<ChildrenAndOtherPeopleRelation>builder().value(expectedRelation).build());

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertEquals(expectedRelations, caseDetailsRespnse.get("buffChildAndOtherPeopleRelations"));
    }

    @Test
    public void testPopulateOtherPeopleToChildRelation() throws NotFoundException {

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().id(UUID.randomUUID()).value(applicant1).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().id(UUID.randomUUID()).value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        OtherPersonRelationshipToChild personRelationshipToChild = OtherPersonRelationshipToChild.builder()
            .personRelationshipToChild("Test relationship")
            .build();

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("firstName")
            .lastName("lastName")
            .gender(Gender.male)
            .isDateOfBirthKnown(YesOrNo.Yes)
            .dateOfBirth(LocalDate.of(1989, 10, 20))
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .placeOfBirth("London")
            .isCurrentAddressKnown(YesOrNo.Yes)
            .address(Address.builder()
                         .addressLine1("add1")
                         .postCode("postcode")
                         .build())
            .isAddressConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("email@email.com")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("02086656656")
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .otherPersonRelationshipToChildren(List.of(element(personRelationshipToChild)))
            .build();


        Element<PartyDetails> wrappedPartyDetails =
            Element.<PartyDetails>builder().value(partyDetails).id(UUID.randomUUID()).build();
        List<Element<PartyDetails>> listOfPartyDetails = Collections.singletonList(wrappedPartyDetails);

        ChildrenAndOtherPeopleRelation childrenAndOtherPeopleRelation = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.No)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .childAndOtherPeopleRelationOtherDetails("test")
            .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildrenAndOtherPeopleRelation =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(childrenAndOtherPeopleRelation).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfChildrenAndOtherPeopleRelation =
            Collections.singletonList(wrappedChildrenAndOtherPeopleRelation);


        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .othersToNotify(listOfPartyDetails)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().buffChildAndOtherPeopleRelations(listOfChildrenAndOtherPeopleRelation).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        ChildrenAndOtherPeopleRelation expectedRelation = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name")
            .childAndOtherPeopleRelation(RelationshipsEnum.father)
            .childLivesWith(YesOrNo.No)
            .childAndOtherPeopleRelationOtherDetails(null)
            .isChildLivesWithPersonConfidential(null)
            .build();
        List<Element<ChildrenAndOtherPeopleRelation>> expectedRelationList = Collections.singletonList(
            Element.<ChildrenAndOtherPeopleRelation>builder().value(expectedRelation).build());

        AboutToStartOrSubmitCallbackResponse response = relationshipsController.populateOtherPeopleToChildRelation(
            "test",
            callbackRequest
        );
        assertEquals(expectedRelationList, response.getData().get("childAndOtherPeopleRelations"));
    }

    @Test
    public void testPopulateOtherPeopleToChildRelationWhenRelationIsOtherAndChildLivesWithIsYes() throws NotFoundException {

        PartyDetails otherPerson = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedOtherPeople = Element.<PartyDetails>builder().id(UUID.randomUUID()).value(otherPerson).build();
        List<Element<PartyDetails>> otherPeopleList = Collections.singletonList(wrappedOtherPeople);

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChildren =
            Element.<ChildDetailsRevised>builder().id(UUID.randomUUID()).value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        ChildrenAndOtherPeopleRelation childrenAndOtherPeopleRelation = ChildrenAndOtherPeopleRelation.builder()
            .otherPeopleFullName("Test")
            .childFullName("Name").childAndOtherPeopleRelation(RelationshipsEnum.other)
            .childAndOtherPeopleRelationOtherDetails("test")
            .childLivesWith(YesOrNo.Yes)
            .isChildLivesWithPersonConfidential(YesOrNo.Yes)
            .build();

        Element<ChildrenAndOtherPeopleRelation> wrappedChildrenAndOtherPeopleRelation =
            Element.<ChildrenAndOtherPeopleRelation>builder().value(childrenAndOtherPeopleRelation).build();
        List<Element<ChildrenAndOtherPeopleRelation>> listOfWrappedChildrenAndOtherPeopleRelation =
            Collections.singletonList(wrappedChildrenAndOtherPeopleRelation);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(otherPeopleList)
            .newChildDetails(listOfChildren)
            .relations(Relations.builder().buffChildAndOtherPeopleRelations(listOfWrappedChildrenAndOtherPeopleRelation).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(CaseDetails.builder().id(123L)
                                                      .data(caseDataUpdated).build()).build();

        AboutToStartOrSubmitCallbackResponse response = relationshipsController.populateOtherPeopleToChildRelation(
            "test",
            callbackRequest
        );
        assertEquals(listOfWrappedChildrenAndOtherPeopleRelation, response.getData().get("childAndOtherPeopleRelations"));
    }

}
