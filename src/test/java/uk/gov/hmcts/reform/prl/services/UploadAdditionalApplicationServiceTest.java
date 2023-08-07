package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2AdditionalOrdersRequested;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService.TEMPORARY_C_2_DOCUMENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@RunWith(MockitoJUnitRunner.class)
public class UploadAdditionalApplicationServiceTest {

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

    DynamicMultiSelectList partyDynamicMultiSelectList;

    List<Element<PartyDetails>> partyDetails;

    PartyDetails party;

    @Before
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

        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com")
                                                                    .roles(List.of(Roles.SOLICITOR.getValue()))
                                                                    .build());
    }

    @Test
    public void testGetAdditionalApplicationElementsForBothC2AndOther() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(partyDynamicMultiSelectList)
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationTypeEnum.c2Order,
                AdditionalApplicationTypeEnum.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(C2DocumentBundle.builder().build())
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .representedPartyType(CA_APPLICANT)
            .build();
        when(applicationsFeeCalculator.getFeeTypes(any(CaseData.class))).thenReturn(List.of(
            FeeType.C2_WITH_NOTICE));
        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());
        when(paymentRequestService.createServiceRequestForAdditionalApplications(any(CaseData.class),
                                                                                 anyString(),
                                                                                 any(FeeResponse.class),
                                                                                 anyString()
        )).thenReturn(PaymentServiceResponse.builder()
                          .build());
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(partyDetails)
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsElementList = new ArrayList<>();
        uploadAdditionalApplicationService.getAdditionalApplicationElements(
            "auth",
            "testAuth",
            caseData,
            additionalApplicationsElementList
        );
        assertNotNull(additionalApplicationsElementList);
    }

    @Test
    public void testGetAdditionalApplicationElementsForC2() throws Exception {

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(List.of(element(Document.builder().build())))
            .urgencyTimeFrameType(UrgencyTimeFrameType.WITHIN_2_DAYS)
            .reasonsForC2Application(List.of(C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT))
            .supplementsBundle(List.of(element(Supplement.builder().build())))
            .additionalDraftOrdersBundle(List.of(element(UploadApplicationDraftOrder.builder().build())))
            .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
            .build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(partyDynamicMultiSelectList)
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.c2Order))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .representedPartyType(CA_RESPONDENT)
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(partyDetails)
            .build();
        when(applicationsFeeCalculator.getFeeTypes(any(CaseData.class))).thenReturn(List.of(
            FeeType.C2_WITH_NOTICE));
        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(null);
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
    public void testGetAdditionalApplicationElementsForC2ForOtherParties() throws Exception {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(List.of(element(Document.builder().build())))
            .urgencyTimeFrameType(UrgencyTimeFrameType.WITHIN_2_DAYS)
            .reasonsForC2Application(List.of(C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT))
            .supplementsBundle(List.of(element(Supplement.builder().build())))
            .additionalDraftOrdersBundle(List.of(element(UploadApplicationDraftOrder.builder().build())))
            .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
            .build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(partyDynamicMultiSelectList)
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.c2Order))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .representedPartyType(CA_RESPONDENT)
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .othersToNotify(partyDetails)
            .build();
        when(applicationsFeeCalculator.getFeeTypes(any(CaseData.class))).thenReturn(List.of(
            FeeType.C2_WITH_NOTICE));
        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(null);
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
    public void testGetAdditionalApplicationElementsForOther() throws Exception {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .email("test@abc.com")
                                                                    .roles(List.of(Roles.CITIZEN.getValue()))
                                                                    .build());
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.otherOrder))
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
    public void testCalculateAdditionalApplicationsFee() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.otherOrder))
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
    public void testCreateUploadAdditionalApplicationBundle() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.otherOrder))
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
        assertEquals(
            objectMap,
            uploadAdditionalApplicationService.createUploadAdditionalApplicationBundle("testAuth",
                                                                                       "testAuth",
                                                                                       callbackRequest)
        );
    }

    @Test
    public void testPrePopulateApplicantsForCaApplicant() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.otherOrder))
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
        CaseUser caseUser = CaseUser.builder().caseRole("[C100APPLICANTSOLICITOR1]").build();
        findUserCaseRolesResponse.setCaseUsers(List.of(caseUser));
        when(userDataStoreService.findUserCaseRoles(
            anyString(),
            anyString()
        )).thenReturn(findUserCaseRolesResponse);
        assertEquals(objectMap, uploadAdditionalApplicationService.prePopulateApplicants(callbackRequest, "testAuth"));
    }

    @Test
    public void testPrePopulateApplicantsForCaRespondent() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.otherOrder))
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
        CaseUser caseUser = CaseUser.builder().caseRole("[C100RESPONDENTSOLICITOR1]").build();
        findUserCaseRolesResponse.setCaseUsers(List.of(caseUser));
        when(userDataStoreService.findUserCaseRoles(
            anyString(),
            anyString()
        )).thenReturn(findUserCaseRolesResponse);
        assertEquals(objectMap, uploadAdditionalApplicationService.prePopulateApplicants(callbackRequest, "testAuth"));
    }

    @Test
    public void testPrePopulateApplicantsForDaApplicant() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.otherOrder))
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
        CaseUser caseUser = CaseUser.builder().caseRole("[APPLICANTSOLICITOR]").build();
        findUserCaseRolesResponse.setCaseUsers(List.of(caseUser));
        when(userDataStoreService.findUserCaseRoles(
            anyString(),
            anyString()
        )).thenReturn(findUserCaseRolesResponse);
        assertEquals(objectMap, uploadAdditionalApplicationService.prePopulateApplicants(callbackRequest, "testAuth"));
    }

    @Test
    public void testPrePopulateApplicantsForDaRespondent() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.otherOrder))
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
        CaseUser caseUser = CaseUser.builder().caseRole("[FL401RESPONDENTSOLICITOR]").build();
        findUserCaseRolesResponse.setCaseUsers(List.of(caseUser));
        when(userDataStoreService.findUserCaseRoles(
            anyString(),
            anyString()
        )).thenReturn(findUserCaseRolesResponse);
        assertEquals(objectMap, uploadAdditionalApplicationService.prePopulateApplicants(callbackRequest, "testAuth"));
    }

    @Test
    public void testPrePopulateApplicantsForApplicantSolicitor() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.otherOrder))
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
        CaseUser caseUser = CaseUser.builder().caseRole("[CREATOR]").build();
        findUserCaseRolesResponse.setCaseUsers(List.of(caseUser));
        when(userDataStoreService.findUserCaseRoles(
            anyString(),
            anyString()
        )).thenReturn(findUserCaseRolesResponse);
        assertEquals(objectMap, uploadAdditionalApplicationService.prePopulateApplicants(callbackRequest, "testAuth"));
    }

    @Test
    public void testUploadAdditionalApplicationSubmitted() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.c2Order))
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
    public void testUploadAdditionalApplicationSubmittedWithHwfYes() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.c2Order))
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
    public void testUploadAdditionalApplicationSubmittedWithHwfNo() throws Exception {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.c2Order))
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
    public void testPopulateHearingList() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.c2Order))
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

}
