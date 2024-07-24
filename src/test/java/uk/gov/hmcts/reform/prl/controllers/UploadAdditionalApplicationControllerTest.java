package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.workflows.ApplicationConsiderationTimetableValidationWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class UploadAdditionalApplicationControllerTest {
    @Mock
    private ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;

    @Mock
    private ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;

    @InjectMocks
    private UploadAdditionalApplicationController uploadAdditionalApplicationController;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;
    @Mock
    private UploadAdditionalApplicationService uploadAdditionalApplicationService;

    @Mock
    private PaymentRequestService paymentRequestService;
    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private SystemUserService systemUserService;

    private static DynamicMultiSelectList dynamicMultiselectList;
    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Before
    public void setUp() {
        Map<String, List<DynamicMultiselectListElement>> listItems = new HashMap<>();
        listItems.put("applicants", List.of(DynamicMultiselectListElement.EMPTY));
        listItems.put("respondents", List.of(DynamicMultiselectListElement.EMPTY));
        dynamicMultiselectList = DynamicMultiSelectList.builder().build();
        when(dynamicMultiSelectListService.getApplicantsMultiSelectList(Mockito.any(CaseData.class))).thenReturn(
            listItems);
        when(dynamicMultiSelectListService.getRespondentsMultiSelectList(Mockito.any(CaseData.class))).thenReturn(
            listItems);
        when(dynamicMultiSelectListService.getOtherPeopleMultiSelectList(Mockito.any(CaseData.class)))
            .thenReturn(listItems.get("applicants"));
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
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

        when(systemUserService.getSysUserToken()).thenReturn("testAuth");
        when(uploadAdditionalApplicationService.prePopulateApplicants(callbackRequest, "testAuth")).thenReturn(
            caseDataUpdated);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            uploadAdditionalApplicationController.prePopulateApplicants("testAuth",
                                                                        callbackRequest, s2sToken
            );

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertNotNull(caseDetailsRespnse.get("additionalApplicantsList"));
    }

    @Test
    public void testExceptionForPrePopulateApplicants() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("temporaryOtherApplicationsBundle", "test");
        caseDataUpdated.put("temporaryC2Document", "test");
        caseDataUpdated.put("additionalApplicantsList", "test");
        caseDataUpdated.put("typeOfC2Application", "test");
        caseDataUpdated.put("additionalApplicationsApplyingFor", "test");
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataUpdated).build()).build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            uploadAdditionalApplicationController.prePopulateApplicants(authToken,callbackRequest, s2sToken);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testPrePopulateApplicantsNotFound() throws NotFoundException {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(caseDataUpdated).build()).build();

        when(uploadAdditionalApplicationService.prePopulateApplicants(callbackRequest, "testAuth")).thenReturn(
            caseDataUpdated);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            uploadAdditionalApplicationController.prePopulateApplicants("testAuth",
                                                                        callbackRequest, s2sToken
            );

        Map<String, Object> caseDetailsRespnse = aboutToStartOrSubmitCallbackResponse.getData();
        assertNull(caseDetailsRespnse.get("additionalApplicantsList"));
    }

    @Test
    public void testcreateUploadAdditionalApplicationBundle() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.otherOrder)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .additionalApplicationFeesToPay("£232.00")
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().urgencyTimeFrameType(
                UrgencyTimeFrameType.WITHIN_2_DAYS).build())
            .build();
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("caseTypeOfApplication", "C100");

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        FeeResponse feeResponse = FeeResponse.builder().build();
        Mockito.doNothing().when(uploadAdditionalApplicationService).getAdditionalApplicationElements(
            "test",
            "test",
            caseData,
            additionalApplicationsBundle
        );
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataUpdated).build()).build();
        assertNotNull(uploadAdditionalApplicationController.createUploadAdditionalApplicationBundle(
            "test",
            callbackRequest, s2sToken
        ));
    }

    @Test
    public void testExceptionForUploadAdditionalApplicationBundle() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("temporaryOtherApplicationsBundle", "test");
        caseDataUpdated.put("temporaryC2Document", "test");
        caseDataUpdated.put("additionalApplicantsList", "test");
        caseDataUpdated.put("typeOfC2Application", "test");
        caseDataUpdated.put("additionalApplicationsApplyingFor", "test");
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataUpdated).build()).build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            uploadAdditionalApplicationController.createUploadAdditionalApplicationBundle(authToken,callbackRequest, s2sToken);
        }, RuntimeException.class, "Invalid Client");
    }


    @Test
    public void testcreateUploadAdditionalApplicationBundleWithCaseDataMap() throws Exception {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("temporaryOtherApplicationsBundle", "test");
        caseDataUpdated.put("temporaryC2Document", "test");
        caseDataUpdated.put("additionalApplicantsList", "test");
        caseDataUpdated.put("typeOfC2Application", "test");
        caseDataUpdated.put("additionalApplicationsApplyingFor", "test");
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        FeeResponse feeResponse = FeeResponse.builder().build();
        Mockito.doNothing().when(uploadAdditionalApplicationService).getAdditionalApplicationElements(
            "test",
            "test",
            caseData,
            additionalApplicationsBundle
        );
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataUpdated).build()).build();

        assertNotNull(uploadAdditionalApplicationController.createUploadAdditionalApplicationBundle(
            "test",
            callbackRequest, s2sToken
        ));
    }

    @Test
    public void testExceptionForUploadAdditionalApplicationBundleWithCaseDataMap() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("temporaryOtherApplicationsBundle", "test");
        caseDataUpdated.put("temporaryC2Document", "test");
        caseDataUpdated.put("additionalApplicantsList", "test");
        caseDataUpdated.put("typeOfC2Application", "test");
        caseDataUpdated.put("additionalApplicationsApplyingFor", "test");
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataUpdated).build()).build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            uploadAdditionalApplicationController.createUploadAdditionalApplicationBundle(authToken,callbackRequest, s2sToken);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testcalculateAdditionalApplicationsFee() throws Exception {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("temporaryOtherApplicationsBundle", "test");
        caseDataUpdated.put("temporaryC2Document", "test");
        caseDataUpdated.put("additionalApplicantsList", "test");
        caseDataUpdated.put("typeOfC2Application", "test");
        caseDataUpdated.put("additionalApplicationsApplyingFor", "test");
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        FeeResponse feeResponse = FeeResponse.builder().build();
        Mockito.doNothing().when(uploadAdditionalApplicationService).getAdditionalApplicationElements(
            "test",
            "test",
            caseData,
            additionalApplicationsBundle
        );
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataUpdated).build()).build();

        assertNotNull(uploadAdditionalApplicationController.calculateAdditionalApplicationsFee(
            "test",
            callbackRequest, s2sToken
        ));
    }

    @Test
    public void testExceptionForCalculateAdditionalApplicationsFee() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("temporaryOtherApplicationsBundle", "test");
        caseDataUpdated.put("temporaryC2Document", "test");
        caseDataUpdated.put("additionalApplicantsList", "test");
        caseDataUpdated.put("typeOfC2Application", "test");
        caseDataUpdated.put("additionalApplicationsApplyingFor", "test");
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataUpdated).build()).build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            uploadAdditionalApplicationController.calculateAdditionalApplicationsFee(authToken,callbackRequest, s2sToken);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testUploadAdditionalApplicationSubmitted() throws Exception {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("temporaryOtherApplicationsBundle", "test");
        caseDataUpdated.put("temporaryC2Document", "test");
        caseDataUpdated.put("additionalApplicantsList", "test");
        caseDataUpdated.put("typeOfC2Application", "test");
        caseDataUpdated.put("additionalApplicationsApplyingFor", "test");
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        Mockito.doNothing().when(uploadAdditionalApplicationService).getAdditionalApplicationElements(
            "test",
            "test",
            caseData,
            additionalApplicationsBundle
        );
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataUpdated).build()).build();

        assertNotNull(uploadAdditionalApplicationController.uploadAdditionalApplicationSubmittedEvent(
            "test",
            callbackRequest, s2sToken
        ));
    }

    @Test
    public void testExceptionForUploadAdditionalApplicationSubmitted() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("temporaryOtherApplicationsBundle", "test");
        caseDataUpdated.put("temporaryC2Document", "test");
        caseDataUpdated.put("additionalApplicantsList", "test");
        caseDataUpdated.put("typeOfC2Application", "test");
        caseDataUpdated.put("additionalApplicationsApplyingFor", "test");
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataUpdated).build()).build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            uploadAdditionalApplicationController.uploadAdditionalApplicationSubmittedEvent(authToken,callbackRequest, s2sToken);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testUploadAdditionalApplicationMidEvent() throws Exception {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("temporaryOtherApplicationsBundle", "test");
        caseDataUpdated.put("temporaryC2Document", "test");
        caseDataUpdated.put("additionalApplicantsList", "test");
        caseDataUpdated.put("typeOfC2Application", "test");
        caseDataUpdated.put("additionalApplicationsApplyingFor", "test");
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        Mockito.doNothing().when(uploadAdditionalApplicationService).getAdditionalApplicationElements(
            "test",
            "test",
            caseData,
            additionalApplicationsBundle
        );
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataUpdated).build()).build();

        assertNotNull(uploadAdditionalApplicationController.populateHearingList(
            "test",
            callbackRequest, s2sToken
        ));
    }

    @Test
    public void testExceptionForUploadAdditionalApplicationMidEvent() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("temporaryOtherApplicationsBundle", "test");
        caseDataUpdated.put("temporaryC2Document", "test");
        caseDataUpdated.put("additionalApplicantsList", "test");
        caseDataUpdated.put("typeOfC2Application", "test");
        caseDataUpdated.put("additionalApplicationsApplyingFor", "test");
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataUpdated).build()).build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            uploadAdditionalApplicationController.populateHearingList(authToken,callbackRequest, s2sToken);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

}
