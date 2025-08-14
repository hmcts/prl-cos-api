package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2AdditionalOrdersRequestedCa;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Supplement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.UploadApplicationDraftOrder;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.UploadAdditionalApplicationUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService.TEMPORARY_C_2_DOCUMENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@ExtendWith(MockitoExtension.class)
class UploadAdditionalApplicationServiceTest {

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private UploadAdditionalApplicationService uploadAdditionalApplicationService;

    @Mock
    private ApplicationsFeeCalculator applicationsFeeCalculator;
    @Mock
    private FeeService feeService;
    @Mock
    private PaymentRequestService paymentRequestService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;
    @Mock
    private CcdDataStoreService userDataStoreService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private SendAndReplyService sendAndReplyService;

    @Mock
    private UploadAdditionalApplicationUtils uploadAdditionalApplicationUtils;

    DynamicMultiSelectList partyDynamicMultiSelectList;

    List<Element<PartyDetails>> partyDetails;

    PartyDetails party;

    @BeforeEach
    public void setUp() throws Exception {
        List<DynamicMultiselectListElement> dynamicMultiselectListElements = new ArrayList<>();
        DynamicMultiselectListElement partyDynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("f2847b15-dbb8-4df0-868a-420d9de11d29")
            .label("John Doe")
            .build();
        dynamicMultiselectListElements.add(partyDynamicMultiselectListElement);
        partyDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .listItems(dynamicMultiselectListElements)
            .value(dynamicMultiselectListElements)
            .build();

        party = PartyDetails.builder()
            .firstName("John")
            .lastName("Doe")
            .partyId(UUID.fromString("f2847b15-dbb8-4df0-868a-420d9de11d29"))
            .build();
        Element<PartyDetails> partyDetailsElement = element(
            UUID.fromString("f2847b15-dbb8-4df0-868a-420d9de11d29"),
            party
        );
        partyDetails = new ArrayList<>();
        partyDetails.add(partyDetailsElement);
    }

    @Test
    void testGetAdditionalApplicationElementsForBothC2AndOther() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(partyDynamicMultiSelectList)
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.otherOrder
            )
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(C2DocumentBundle.builder().build())
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .representedPartyType(CA_APPLICANT)
            .build();
        when(applicationsFeeCalculator.getFeeTypes(any(CaseData.class))).thenReturn(List.of(
            FeeType.C2_WITH_NOTICE));
        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());
        when(paymentRequestService.getPaymentServiceResponse(anyString(), any(CaseData.class), any(FeeResponse.class)))
            .thenReturn(PaymentServiceResponse.builder()
                          .build());
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(partyDetails)
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsElementList = new ArrayList<>();
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com")
                .roles(List.of(Roles.SOLICITOR.getValue()))
                .build());
        uploadAdditionalApplicationService.getAdditionalApplicationElements(
            "auth",
            "testAuth",
            caseData,
            additionalApplicationsElementList
        );
        assertNotNull(additionalApplicationsElementList);
    }

    @Test
    void testCalculateAdditionalApplicationsFeeSolicitorDetailsNotEmpty() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
                .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.otherOrder)
                .representedPartyType("test")
                .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
                .build();
        CaseData caseData = CaseData.builder()
                .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
                .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(applicationsFeeCalculator.calculateAdditionalApplicationsFee(any(CaseData.class))).thenReturn(objectMap);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        CaseUser caseUser = CaseUser.builder().caseRole("caseworker-privatelaw-solicitor").build();
        findUserCaseRolesResponse.setCaseUsers(List.of(caseUser));
        when(CaseUtils.getCaseData(
                callbackRequest.getCaseDetails(),
                objectMapper
        )).thenReturn(caseData);
        assertEquals(
                objectMap,
                uploadAdditionalApplicationService.calculateAdditionalApplicationsFee("testAuth", callbackRequest)
        );
    }

    @Test
    void testCalculateAdditionalApplicationsFeeApplicantSolicitor() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.otherOrder)
            .representedPartyType("test")
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(applicationsFeeCalculator.calculateAdditionalApplicationsFee(any(CaseData.class))).thenReturn(objectMap);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        CaseUser caseUser = CaseUser.builder().caseRole("caseworker-privatelaw-solicitor").build();
        findUserCaseRolesResponse.setCaseUsers(List.of(caseUser));
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        assertEquals(
            objectMap,
            uploadAdditionalApplicationService.calculateAdditionalApplicationsFee("testAuth", callbackRequest)
        );
    }


    @Test
    void testupdateAwpApplicationStatus() throws Exception {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle
                .builder()
                .otherApplicationsBundle(OtherApplicationsBundle
                        .builder()
                        .uploadedDateTime("01")
                        .build())
                .build()));

        assertNotNull(
                uploadAdditionalApplicationService
                        .updateAwpApplicationStatus("OT_01_02", additionalApplicationsBundle, "OT")
        );
    }

    @Test
    void testupdateAwpApplicationStatusC2() throws Exception {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle
                .builder()
                .c2DocumentBundle(C2DocumentBundle
                        .builder()
                        .uploadedDateTime("01")
                        .build())
                .build()));

        assertNotNull(
                uploadAdditionalApplicationService
                        .updateAwpApplicationStatus("C2_01_02", additionalApplicationsBundle, "C2")
        );
    }

    @Test
    void testGetAdditionalApplicationElementsForC2() throws Exception {
        when(applicationsFeeCalculator.getFeeTypes(any(CaseData.class))).thenReturn(List.of(
                FeeType.C2_WITH_NOTICE));
        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(null);
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com")
                .roles(List.of(Roles.SOLICITOR.getValue()))
                .build());
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(Document.builder().build())
            .urgencyTimeFrameType(UrgencyTimeFrameType.WITHIN_2_DAYS)
            .caReasonsForC2Application(List.of(C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT))
            .supplementsBundle(List.of(element(Supplement.builder().build())))
            .additionalDraftOrdersBundle(List.of(element(UploadApplicationDraftOrder.builder().build())))
            .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
                .additionalApplicantsList(partyDynamicMultiSelectList)
                .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.c2Order)
                .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
                .temporaryC2Document(c2DocumentBundle)
                .representedPartyType(CA_RESPONDENT)
                .build();
        CaseData caseData = CaseData.builder()
                .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
                .additionalApplicationsBundle(additionalApplicationsBundle)
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .respondents(partyDetails)
                .build();
        uploadAdditionalApplicationService.getAdditionalApplicationElements(
            "auth",
            "testAuth",
            caseData,
            additionalApplicationsBundle
        );

        assertNotNull(additionalApplicationsBundle);
        assertEquals(2, additionalApplicationsBundle.size());
    }

    @Test
    void testGetAdditionalApplicationElementsWithoutAuthor() throws Exception {
        when(applicationsFeeCalculator.getFeeTypes(any(CaseData.class))).thenReturn(List.of(
            FeeType.C2_WITH_NOTICE));
        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(null);
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com")
                                                                    .forename("test")
                                                                    .surname("test")
                                                                    .roles(List.of(Roles.SOLICITOR.getValue()))
                                                                    .build());
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(Document.builder().build())
            .urgencyTimeFrameType(UrgencyTimeFrameType.WITHIN_2_DAYS)
            .caReasonsForC2Application(List.of(C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT))
            .supplementsBundle(List.of(element(Supplement.builder().build())))
            .additionalDraftOrdersBundle(List.of(element(UploadApplicationDraftOrder.builder().build())))
            .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(partyDynamicMultiSelectList)
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.c2Order)
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .representedPartyType("DUMMY_PARTY")
            .build();
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(partyDetails)
            .build();
        uploadAdditionalApplicationService.getAdditionalApplicationElements(
            "auth",
            "testAuth",
            caseData,
            additionalApplicationsBundle
        );

        assertNotNull(additionalApplicationsBundle);
        assertEquals(2, additionalApplicationsBundle.size());
        assertEquals("test test", additionalApplicationsBundle.get(1).getValue().getAuthor());
    }

    @Test
    void testGetAdditionalApplicationElementsWhenNull() throws Exception {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com")
                                                                    .roles(List.of(Roles.SOLICITOR.getValue()))
                                                                    .build());
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = null;
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(null)
            .build();
        uploadAdditionalApplicationService.getAdditionalApplicationElements(
            "auth",
            "testAuth",
            caseData,
            additionalApplicationsBundle
        );

        assertNull(additionalApplicationsBundle);
    }

    @Test
    void testGetAdditionalApplicationElementsForC2ForOtherParties() throws Exception {
        when(applicationsFeeCalculator.getFeeTypes(any(CaseData.class))).thenReturn(List.of(
                FeeType.C2_WITH_NOTICE));
        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(null);
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com")
                .roles(List.of(Roles.SOLICITOR.getValue()))
                .build());
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(Document.builder().build())
            .urgencyTimeFrameType(UrgencyTimeFrameType.WITHIN_2_DAYS)
            .caReasonsForC2Application(List.of(C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT))
            .supplementsBundle(List.of(element(Supplement.builder().build())))
            .additionalDraftOrdersBundle(List.of(element(UploadApplicationDraftOrder.builder().build())))
            .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
                .additionalApplicantsList(partyDynamicMultiSelectList)
                .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.c2Order)
                .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
                .temporaryC2Document(c2DocumentBundle)
                .representedPartyType(CA_RESPONDENT)
                .build();
        CaseData caseData = CaseData.builder()
                .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
                .additionalApplicationsBundle(additionalApplicationsBundle)
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .othersToNotify(partyDetails)
                .build();
        uploadAdditionalApplicationService.getAdditionalApplicationElements(
            "auth",
            "testAuth",
            caseData,
            additionalApplicationsBundle
        );

        assertNotNull(additionalApplicationsBundle);
        assertEquals(2, additionalApplicationsBundle.size());
    }

    @Test
    void testGetAdditionalApplicationElementsForOther() throws Exception {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .email("test@abc.com")
                                                                    .roles(List.of(Roles.CITIZEN.getValue()))
                                                                    .build());
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.otherOrder)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .additionalApplicantsList(partyDynamicMultiSelectList)
            .build();
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder().build())
            .respondentsFL401(party)
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsElementList = new ArrayList<>();
        uploadAdditionalApplicationService.getAdditionalApplicationElements(
            "auth",
            "testAuth",
            caseData,
            additionalApplicationsElementList
        );

        assertNotNull(additionalApplicationsElementList);
        assertEquals(1, additionalApplicationsElementList.size());
    }

    @Test
    void testCalculateAdditionalApplicationsFee() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.otherOrder)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(applicationsFeeCalculator.calculateAdditionalApplicationsFee(any(CaseData.class))).thenReturn(objectMap);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        CaseUser caseUser = CaseUser.builder().caseRole("caseworker-privatelaw-solicitor").build();
        findUserCaseRolesResponse.setCaseUsers(List.of(caseUser));
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com")
                .roles(List.of(Roles.SOLICITOR.getValue()))
                .build());
        when(userDataStoreService.findUserCaseRoles(
            anyString(),
            anyString()
        )).thenReturn(findUserCaseRolesResponse);
        assertEquals(
            objectMap,
            uploadAdditionalApplicationService.calculateAdditionalApplicationsFee("testAuth", callbackRequest)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"[APPLICANTSOLICITOR]",
        "[C100APPLICANTSOLICITOR1]",
        "[FL401APPLICANTSOLICITOR]",
        "[C100RESPONDENTSOLICITOR1]",
        "[FL401RESPONDENTSOLICITOR]",
        "TEST"})
    void testCalculateAdditionalApplicationsFeeForApplicantSolicitor(String roles) throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.otherOrder)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(applicationsFeeCalculator.calculateAdditionalApplicationsFee(any(CaseData.class))).thenReturn(objectMap);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        CaseUser caseUser = CaseUser.builder().caseRole(roles).build();
        findUserCaseRolesResponse.setCaseUsers(List.of(caseUser));
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com")
                                                                    .roles(List.of(Roles.SOLICITOR.getValue()))
                                                                    .build());
        when(userDataStoreService.findUserCaseRoles(
            anyString(),
            anyString()
        )).thenReturn(findUserCaseRolesResponse);
        assertEquals(
            objectMap,
            uploadAdditionalApplicationService.calculateAdditionalApplicationsFee("testAuth", callbackRequest)
        );
    }

    @Test
    void testCalculateAdditionalApplicationsFeeIsEmpty() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
                .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.otherOrder)
                .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
                .build();
        CaseData caseData = CaseData.builder()
                .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(applicationsFeeCalculator.calculateAdditionalApplicationsFee(any(CaseData.class))).thenReturn(objectMap);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        CaseUser caseUser = CaseUser.builder().caseRole("caseworker-privatelaw-solicitor").build();
        findUserCaseRolesResponse.setCaseUsers(List.of(caseUser));
        when(CaseUtils.getCaseData(
                callbackRequest.getCaseDetails(),
                objectMapper
        )).thenReturn(caseData);
        assertEquals(
                objectMap,
                uploadAdditionalApplicationService.calculateAdditionalApplicationsFee("testAuth", callbackRequest)
        );
    }

    @Test
    void testCreateUploadAdditionalApplicationBundle() throws Exception {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(Document.builder().build())
            .urgencyTimeFrameType(UrgencyTimeFrameType.SAME_DAY)
            .caReasonsForC2Application(List.of(C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT))
            .supplementsBundle(List.of(element(Supplement.builder().build())))
            .additionalDraftOrdersBundle(List.of(element(UploadApplicationDraftOrder.builder().build())))
            .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
            .build();

        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.otherOrder)
            .additionalApplicationFeesToPay("£232.00")
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().urgencyTimeFrameType(UrgencyTimeFrameType.WITHIN_2_DAYS).build())
            .build();
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com")
                .roles(List.of(Roles.SOLICITOR.getValue()))
                .build());
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        assertEquals(
            objectMap,
            uploadAdditionalApplicationService.createUploadAdditionalApplicationBundle("testAuth",
                                                                                       "testAuth",
                                                                                       callbackRequest)
        );
    }

    @Test
    void testPrePopulateApplicantsForCaApplicant() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.otherOrder)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        Map<String, List<DynamicMultiselectListElement>> stringListMap = new HashMap<>();
        stringListMap.put("applicants", List.of(DynamicMultiselectListElement.EMPTY));
        stringListMap.put("respondents", List.of(DynamicMultiselectListElement.EMPTY));
        when(dynamicMultiSelectListService.getApplicantsMultiSelectList(any(CaseData.class))).thenReturn(stringListMap);
        when(dynamicMultiSelectListService.getRespondentsMultiSelectList(any(CaseData.class))).thenReturn(stringListMap);
        when(dynamicMultiSelectListService.getOtherPeopleMultiSelectList(any(CaseData.class)))
            .thenReturn(List.of(DynamicMultiselectListElement.EMPTY));
        when(idamClient.getUserDetails("testAuth")).thenReturn(UserDetails.builder().roles(List.of(
            "caseworker-privatelaw-solicitor")).build());
        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        String[] roles = {"[C100APPLICANTSOLICITOR1]", "[C100RESPONDENTSOLICITOR1]", "[APPLICANTSOLICITOR]",
            "[FL401RESPONDENTSOLICITOR]", "[CREATOR]"};
        for (String role : roles) {
            CaseUser caseUser = CaseUser.builder().caseRole(role).build();
            findUserCaseRolesResponse.setCaseUsers(List.of(caseUser));
            when(userDataStoreService.findUserCaseRoles(
                anyString(),
                anyString()
            )).thenReturn(findUserCaseRolesResponse);
            assertEquals(objectMap, uploadAdditionalApplicationService.prePopulateApplicants(callbackRequest, "testAuth"));
        }
    }

    @Test
    void testUploadAdditionalApplicationSubmitted() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.c2Order)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        assertNotNull(uploadAdditionalApplicationService.uploadAdditionalApplicationSubmitted(callbackRequest));
    }

    @Test
    void testUploadAdditionalApplicationSubmittedWithHwfYes() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.c2Order)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .hwfRequestedForAdditionalApplications(YesOrNo.Yes)
            .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        assertNotNull(uploadAdditionalApplicationService.uploadAdditionalApplicationSubmitted(callbackRequest));
    }

    @Test
    void testUploadAdditionalApplicationSubmittedWithHwfNo() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.c2Order)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .hwfRequestedForAdditionalApplications(YesOrNo.No)
            .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        assertNotNull(uploadAdditionalApplicationService.uploadAdditionalApplicationSubmitted(callbackRequest));
    }

    @Test
    void testPopulateHearingList() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.c2Order)
            .temporaryC2Document(C2DocumentBundle.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .hwfRequestedForAdditionalApplications(YesOrNo.No)
            .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(authTokenGenerator.generate()).thenReturn("s2sToken");
        when(sendAndReplyService.getFutureHearingDynamicList(
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(DynamicList.builder().build());
        assertTrue(uploadAdditionalApplicationService.populateHearingList("testAuth", callbackRequest).containsKey(
            TEMPORARY_C_2_DOCUMENT));
    }


    @Test
    void testPopulateHearingListOtherApplication() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.otherOrder)
            .temporaryC2Document(C2DocumentBundle.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .hwfRequestedForAdditionalApplications(YesOrNo.No)
            .build();
        Map<String, Object> objectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(objectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        assertTrue(uploadAdditionalApplicationService.populateHearingList("testAuth", callbackRequest).containsKey(
            TEMPORARY_C_2_DOCUMENT));
    }
}
