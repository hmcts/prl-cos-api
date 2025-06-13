package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPolicyUpgradeChildProtectionConcernEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseEvidenceDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiamPolicyUpgradeServiceTest {

    @InjectMocks
    MiamPolicyUpgradeService miamPolicyUpgradeService;

    @Mock
    ObjectMapper objectMapper;

    @Test
    void testPopulateMiamPolicyUpgradeDetailsEmptyCasedata() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder().build()).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertNull(objectMap.get("mpuDomesticAbuseEvidenceDocument"));
    }

    @Test
    void testPopulateMiamPolicyUpgradeDetailsEmptyListOfExcemptions() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder()
                                                          .mpuChildInvolvedInMiam(YesOrNo.No)
                                                          .mpuApplicantAttendedMiam(YesOrNo.No)
                                                          .mpuClaimingExemptionMiam(YesOrNo.Yes)
                                                          .mpuExemptionReasons(new ArrayList<>()).build()).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertNotNull(objectMap.get("mpuClaimingExemptionMiam"));
        assertNull(objectMap.get("mpuDomesticAbuseEvidenceDocument"));
    }

    @Test
    void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamDomesticAbuseEvidenceProvided() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.No)
            .mpuApplicantAttendedMiam(YesOrNo.No)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mediatorRegistrationNumber("123")
            .familyMediatorServiceName("test")
            .soleTraderName("test")
            .miamCertificationDocumentUpload(Document.builder().build())
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
            .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
            .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
            .mpuDomesticAbuseEvidenceDocument(List.of(Element.<DomesticAbuseEvidenceDocument>builder().build()))
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertNotNull(objectMap.get("mpuDomesticAbuseEvidenceDocument"));
    }

    @Test
    void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamDomesticAbuseEvidenceNotProvided() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.No)
            .mpuApplicantAttendedMiam(YesOrNo.No)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
            .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
            .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.No)
            .mpuNoDomesticAbuseEvidenceReason("test")
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertEquals("test", objectMap.get("mpuNoDomesticAbuseEvidenceReason"));
    }

    @Test
    void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamChildProtection() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.No)
            .mpuApplicantAttendedMiam(YesOrNo.No)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuChildProtectionConcern))
            .mpuChildProtectionConcernReason(MiamPolicyUpgradeChildProtectionConcernEnum.mpuChildProtectionConcern_value_1)
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertEquals(MiamPolicyUpgradeChildProtectionConcernEnum
            .mpuChildProtectionConcern_value_1, objectMap.get("mpuChildProtectionConcernReason"));
    }

    @Test
    void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamUrgency() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.No)
            .mpuApplicantAttendedMiam(YesOrNo.No)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuUrgency))
            .mpuUrgencyReason(MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_1)
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertEquals(MiamUrgencyReasonChecklistEnum
            .miamPolicyUpgradeUrgencyReason_Value_1, objectMap.get("mpuUrgencyReason"));
    }

    @Test
    void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamPreviousReason1() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.No)
            .mpuApplicantAttendedMiam(YesOrNo.No)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
            .mpuPreviousMiamAttendanceReason(MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_1)
            .mpuDocFromDisputeResolutionProvider(Document.builder().build())
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertEquals(Document.builder().build(), objectMap.get("mpuDocFromDisputeResolutionProvider"));
    }

    @Test
    void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamPreviousReason2() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.No)
            .mpuApplicantAttendedMiam(YesOrNo.No)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
            .mpuPreviousMiamAttendanceReason(MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_2)
            .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamAttendanceDetails)
            .mpuMediatorDetails("test")
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertEquals("test", objectMap.get("mpuMediatorDetails"));
    }

    @Test
    void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamPreviousReason2Certificate() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.No)
            .mpuApplicantAttendedMiam(YesOrNo.No)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
            .mpuPreviousMiamAttendanceReason(MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_2)
            .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
            .mpuCertificateByMediator(Document.builder().build())
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertEquals(Document.builder().build(), objectMap.get("mpuCertificateByMediator"));
    }

    @Test
    void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamOtherExcemption3() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.No)
            .mpuApplicantAttendedMiam(YesOrNo.No)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuOther))
            .mpuOtherExemptionReasons(MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_3)
            .mpuApplicantUnableToAttendMiamReason1("test")
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertEquals("test", objectMap.get("mpuApplicantUnableToAttendMiamReason1"));
    }

    @Test
    void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamOtherExcemption4() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.No)
            .mpuApplicantAttendedMiam(YesOrNo.No)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuOther))
            .mpuOtherExemptionReasons(MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_4)
            .mpuApplicantUnableToAttendMiamReason1("test")
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertEquals("test", objectMap.get("mpuApplicantUnableToAttendMiamReason1"));
    }

    @Test
    void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamOtherExcemption5() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.No)
            .mpuApplicantAttendedMiam(YesOrNo.No)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuOther))
            .mpuOtherExemptionReasons(MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_5)
            .mpuApplicantUnableToAttendMiamReason2("test")
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateAmendedMiamPolicyUpgradeDetails(callbackRequest);
        assertNotNull(objectMap);
        assertEquals("test", objectMap.get("mpuApplicantUnableToAttendMiamReason2"));
    }

    @Test
    void testUpdateMiamPolicyUpgradeDetails() {
        CaseData caseData = CaseData.builder().miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder().build()).build();
        Map<String, Object> objectMap = new HashMap<>();
        CaseData returnedCaseData = miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(caseData, objectMap);
        assertNotNull(returnedCaseData);
    }
}
