package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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


    @Test
    public void testGetAdditionalApplicationElementsForBothC2AndOther() throws Exception {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com").build());
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
                .additionalApplicantsList(DynamicMultiSelectList.builder().build())
                .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.c2Order,
                                                           AdditionalApplicationTypeEnum.otherOrder))
                .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
                .temporaryC2Document(C2DocumentBundle.builder().build())
                .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
                .build();
        when(applicationsFeeCalculator.getFeeTypes(any(UploadAdditionalApplicationData.class), anyBoolean())).thenReturn(List.of(
            FeeType.C2_WITH_NOTICE));
        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());
        when(paymentRequestService.createServiceRequestForAdditionalApplications(any(CaseData.class), anyString(), any(FeeResponse.class),
                                                                                 anyString())).thenReturn(PaymentServiceResponse.builder()
                                                                                                                      .build());
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsElementList = new ArrayList<>();
        uploadAdditionalApplicationService.getAdditionalApplicationElements("auth", caseData, additionalApplicationsElementList);
        assertNotNull(additionalApplicationsElementList);
    }

    @Test
    public void testGetAdditionalApplicationElementsForC2() throws Exception {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com").build());
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(Document.builder().build())
            .urgencyTimeFrameType(UrgencyTimeFrameType.WITHIN_2_DAYS)
            .reasonsForC2Application(List.of(C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT))
            .supplementsBundle(List.of(element(Supplement.builder().build())))
            .additionalDraftOrdersBundle(List.of(element(UploadApplicationDraftOrder.builder().build())))
            .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
            .build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.c2Order))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .build();
        when(applicationsFeeCalculator.getFeeTypes(any(UploadAdditionalApplicationData.class), anyBoolean())).thenReturn(List.of(
            FeeType.C2_WITH_NOTICE));
        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(null);
        uploadAdditionalApplicationService.getAdditionalApplicationElements("auth", caseData, additionalApplicationsBundle);

        assertNotNull(additionalApplicationsBundle);
        assertEquals(2, additionalApplicationsBundle.size());
    }

    @Test
    public void testGetAdditionalApplicationElementsForOthe() throws Exception {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com").build());
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.otherOrder))
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsElementList = new ArrayList<>();
        uploadAdditionalApplicationService.getAdditionalApplicationElements("auth", caseData, additionalApplicationsElementList);

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
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        assertEquals(objectMap, uploadAdditionalApplicationService.calculateAdditionalApplicationsFee(callbackRequest));
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
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com").build());
        assertEquals(objectMap, uploadAdditionalApplicationService.createUploadAdditionalApplicationBundle("testAuth",callbackRequest));
    }

    @Test
    public void testPrePopulateApplicants() throws Exception {
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
        when(idamClient.getUserDetails("testAuth")).thenReturn(UserDetails.builder().roles(List.of("citizen")).build());
        assertEquals(objectMap, uploadAdditionalApplicationService.prePopulateApplicants(callbackRequest,"testAuth"));
    }

}
