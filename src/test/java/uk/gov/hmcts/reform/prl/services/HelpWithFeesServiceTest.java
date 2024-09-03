package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.PaymentStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.FM5ReminderNotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ProcessUrgentHelpWithFees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.services.HelpWithFeesService.APPLICATION_UPDATED;
import static uk.gov.hmcts.reform.prl.services.HelpWithFeesService.CONFIRMATION_BODY;
import static uk.gov.hmcts.reform.prl.services.HelpWithFeesService.HWF_APPLICATION_DYNAMIC_DATA_LABEL;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HelpWithFeesServiceTest {

    @InjectMocks
    private HelpWithFeesService helpWithFeesService;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    AddCaseNoteService addCaseNoteService;
    @Mock
    UserService userService;

    CaseData casedata;

    CaseDetails caseDetails;

    @Before
    public void init() {

        casedata = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseSubmittedTimeStamp("2024-06-24T10:46:55.972994696+01:00")
            .id(123L)
            .applicantCaseName("test")
            .helpWithFeesNumber("123")
            .helpWithFees(YesOrNo.Yes)
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                                            .firstName("firstName")
                                            .lastName("LastName")
                                            .build())))
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .state(State.SUBMITTED_NOT_PAID.getValue())
            .build();
    }

    @Test
    public void testAboutToStart() {

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService.handleAboutToStart(caseDetails);
        assertNotNull(response);
        DynamicList dynamicList = (DynamicList) response.get("hwfAppList");
        assertEquals("Child arrangements application C100 - 24/06/2024 10:46:55", dynamicList.getListItems().get(0).getLabel());
        assertEquals("C100",response.get("caseTypeOfApplication"));
    }

    @Test
    public void testAboutToSubmit() {
        casedata = casedata.toBuilder()
                .state(State.SUBMITTED_PAID)
                    .build();

        caseDetails = caseDetails.toBuilder()
            .state(State.SUBMITTED_NOT_PAID.getValue())
            .data(new HashMap<>())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService
            .setCaseStatus(CallbackRequest.builder().caseDetails(caseDetails).build(), "testAuth");
        assertNotNull(response);
        CaseStatus caseStatus = (CaseStatus) response.get("caseStatus");
        assertEquals("Submitted", caseStatus.getState());
    }

    @Test
    public void testAboutToSubmitApplicationsWithinProceedings() {
        casedata = casedata.toBuilder()
            .state(State.SUBMITTED_PAID)
            .build();

        caseDetails = caseDetails.toBuilder()
            .state(State.SUBMITTED_PAID.getValue())
            .data(new HashMap<>())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService
            .setCaseStatus(CallbackRequest.builder().caseDetails(caseDetails).build(), "testAuth");
        assertNotNull(response);
    }

    @Test
    public void testAboutToSubmitApplicationsWithinProceedingsProcessUrgentFeesIsNull() {
        casedata = casedata.toBuilder()
            .state(State.SUBMITTED_PAID)
            .fm5ReminderNotificationDetails(FM5ReminderNotificationDetails.builder()
                .build())
            .build();

        caseDetails = caseDetails.toBuilder()
            .state(State.SUBMITTED_PAID.getValue())
            .data(new HashMap<>())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService
            .setCaseStatus(CallbackRequest.builder().caseDetails(caseDetails).build(),"testAuth");
        assertNotNull(response);
    }

    @Test
    public void testAboutToSubmitApplicationsWithinProceedingsDynamicListIsNull() {
        casedata = casedata.toBuilder()
            .state(State.SUBMITTED_PAID)
            .processUrgentHelpWithFees(ProcessUrgentHelpWithFees.builder().build())
            .build();

        caseDetails = caseDetails.toBuilder()
            .state(State.SUBMITTED_PAID.getValue())
            .data(new HashMap<>())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService
            .setCaseStatus(CallbackRequest.builder().caseDetails(caseDetails).build(), "testAuth");
        assertNotNull(response);
    }

    @Test
    public void testAboutToSubmitApplicationsWithinProceedingsDynamicListIsEmpty() {
        casedata = casedata.toBuilder()
            .state(State.SUBMITTED_PAID)
            .processUrgentHelpWithFees(ProcessUrgentHelpWithFees.builder().build())
            .build();

        caseDetails = caseDetails.toBuilder()
            .state(State.SUBMITTED_PAID.getValue())
            .data(new HashMap<>())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService
            .setCaseStatus(CallbackRequest.builder().caseDetails(caseDetails).build(), "testAuth");
        assertNotNull(response);
    }

    @Test
    public void testAboutToSubmitApplicationsWithinProceedingsDynamicListIsEmptyWithAdditionalApplications() {
        UUID applicationId = UUID.randomUUID();
        List<Element<AdditionalApplicationsBundle>> additionalApplications = new ArrayList<>();
        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder().build())
            .otherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .payment(Payment.builder().status(PaymentStatus.HWF.getDisplayedValue()).hwfReferenceNumber("HWF-1BC-AF").build())
            .build();
        additionalApplications.add(element(applicationId,additionalApplicationsBundle));

        casedata = casedata.toBuilder()
            .state(State.SUBMITTED_PAID)
            .additionalApplicationsBundle(additionalApplications)
            .processUrgentHelpWithFees(ProcessUrgentHelpWithFees.builder()
                                           .addHwfCaseNoteShort("test")
                                           .hwfAppList(DynamicList.builder()
                                                           .value(DynamicListElement.builder().code(applicationId).build())
                                                           .build()).build())
            .build();

        caseDetails = caseDetails.toBuilder()
            .state(State.SUBMITTED_PAID.getValue())
            .data(new HashMap<>())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService
            .setCaseStatus(CallbackRequest.builder().caseDetails(caseDetails).build(), "testAuth");
        assertNotNull(response);
    }

    @Test
    public void testSubmitted() {
        ResponseEntity<SubmittedCallbackResponse> submittedResponse = helpWithFeesService.handleSubmitted();
        assertEquals(APPLICATION_UPDATED, submittedResponse.getBody().getConfirmationHeader());
        assertEquals(CONFIRMATION_BODY, submittedResponse.getBody().getConfirmationBody());
    }

    @Test
    public void testAboutToStart_c2Awp() {
        caseDetails = caseDetails.toBuilder()
            .state(State.SUBMITTED_PAID.getValue())
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplications = new ArrayList<>();
        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder().build())
            .payment(Payment.builder().status(PaymentStatus.HWF.getDisplayedValue()).hwfReferenceNumber("HWF-1BC-AF").build())
            .build();
        additionalApplications.add(element(additionalApplicationsBundle));
        casedata = casedata.toBuilder()
            .additionalApplicationsBundle(additionalApplications)
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService.handleAboutToStart(caseDetails);
        assertNotNull(response);
        DynamicList dynamicList = (DynamicList) response.get("hwfAppList");
        assertNotNull(dynamicList);
    }

    @Test
    public void testAboutToStart_otherAwp() {
        caseDetails = caseDetails.toBuilder()
            .state(State.SUBMITTED_PAID.getValue())
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplications = new ArrayList<>();
        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicationType(OtherApplicationType.C1_CHILD_ORDER)
                                         .build())
            .payment(Payment.builder().status(PaymentStatus.HWF.getDisplayedValue()).hwfReferenceNumber("HWF-1BC-AF").build())
            .build();
        additionalApplications.add(element(additionalApplicationsBundle));
        casedata = casedata.toBuilder()
            .additionalApplicationsBundle(additionalApplications)
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService.handleAboutToStart(caseDetails);
        assertNotNull(response);
        DynamicList dynamicList = (DynamicList) response.get("hwfAppList");
        assertNotNull(dynamicList);
    }

    @Test
    public void testCheckForManagerApproval() {
        casedata = casedata.toBuilder()
            .processUrgentHelpWithFees(ProcessUrgentHelpWithFees.builder()
                                           .outstandingBalance(YesOrNo.Yes)
                                           .managerAgreedApplicationBeforePayment(YesOrNo.No).build())
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        List<String> errorList = helpWithFeesService.checkForManagerApproval(caseDetails);
        assertNotNull(errorList);
    }

    @Test
    public void testPopulateHwfDynamicData() {
        caseDetails = caseDetails.toBuilder()
            .data(casedata.toMap(new ObjectMapper()))
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService.populateHwfDynamicData(caseDetails);
        assertNotNull(response);
        String hwfApplicationDynamicData = (String) response.get(HWF_APPLICATION_DYNAMIC_DATA_LABEL);
        assertNotNull(hwfApplicationDynamicData);
    }

    @Test
    public void testPopulateHwfDynamicData_c2Awp() {
        UUID applicationId = UUID.randomUUID();
        caseDetails = caseDetails.toBuilder()
            .state(State.SUBMITTED_PAID.getValue())
            .data(casedata.toMap(new ObjectMapper()))
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplications = new ArrayList<>();
        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder().build())
            .payment(Payment.builder().status(PaymentStatus.HWF.getDisplayedValue()).hwfReferenceNumber("HWF-1BC-AF").build())
            .build();
        additionalApplications.add(element(applicationId,additionalApplicationsBundle));
        casedata = casedata.toBuilder()
            .additionalApplicationsBundle(additionalApplications)
            .processUrgentHelpWithFees(ProcessUrgentHelpWithFees.builder()
                                           .hwfAppList(DynamicList.builder()
                                                           .value(DynamicListElement.builder().code(applicationId).build())
                                                           .build()).build())
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService.populateHwfDynamicData(caseDetails);
        assertNotNull(response);
        String hwfApplicationDynamicData = (String) response.get(HWF_APPLICATION_DYNAMIC_DATA_LABEL);
        assertNotNull(hwfApplicationDynamicData);
    }

    @Test
    public void testPopulateHwfDynamicData_otherAwp() {
        UUID applicationId = UUID.randomUUID();
        caseDetails = caseDetails.toBuilder()
            .state(State.SUBMITTED_PAID.getValue())
            .data(casedata.toMap(new ObjectMapper()))
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplications = new ArrayList<>();
        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicationType(OtherApplicationType.C1_CHILD_ORDER)
                                         .build())
            .payment(Payment.builder().status(PaymentStatus.HWF.getDisplayedValue()).hwfReferenceNumber("HWF-1BC-AF").build())
            .build();
        additionalApplications.add(element(applicationId, additionalApplicationsBundle));
        casedata = casedata.toBuilder()
            .additionalApplicationsBundle(additionalApplications)
            .processUrgentHelpWithFees(ProcessUrgentHelpWithFees.builder()
                                           .hwfAppList(DynamicList.builder()
                                                           .value(DynamicListElement.builder().code(applicationId).build())
                                                           .build()).build())
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService.populateHwfDynamicData(caseDetails);
        assertNotNull(response);
        String hwfApplicationDynamicData = (String) response.get(HWF_APPLICATION_DYNAMIC_DATA_LABEL);
        assertNotNull(hwfApplicationDynamicData);
    }
}
