package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseEvidenceDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MiamPolicyUpgradeServiceTest {

    @InjectMocks
    MiamPolicyUpgradeService miamPolicyUpgradeService;

    @Mock
    ObjectMapper objectMapper;

    @Test
    public void testPopulateMiamPolicyUpgradeDetailsEmptyCasedata() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder().build()).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateMiamPolicyUpgradeDetails(callbackRequest);
        Assert.assertNotNull(objectMap);
        Assert.assertNull(objectMap.get("mpuDomesticAbuseEvidenceDocument"));
    }

    @Test
    public void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamDomesticAbuseEvidenceProvided() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.Yes)
            .mpuApplicantAttendedMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mediatorRegistrationNumber("123")
            .familyMediatorServiceName("test")
            .soleTraderName("test")
            .miamCertificationDocumentUpload(Document.builder().build())
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.domesticAbuse))
            .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
            .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
            .mpuDomesticAbuseEvidenceDocument(List.of(Element.<DomesticAbuseEvidenceDocument>builder().build()))
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateMiamPolicyUpgradeDetails(callbackRequest);
        Assert.assertNotNull(objectMap);
        Assert.assertNotNull(objectMap.get("mpuDomesticAbuseEvidenceDocument"));
    }

    @Test
    public void testPopulateMiamPolicyUpgradeDetailsClaimingExcemptionFromMiamDomesticAbuseEvidenceNotProvided() {
        uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.Yes)
            .mpuApplicantAttendedMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mediatorRegistrationNumber("123")
            .familyMediatorServiceName("test")
            .soleTraderName("test")
            .miamCertificationDocumentUpload(Document.builder().build())
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.domesticAbuse))
            .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
            .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.No)
            .mpuNoDomesticAbuseEvidenceReason("test")
            .build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class))
            .thenReturn(CaseData.builder()
                .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails).build());
        Map<String, Object> objectMap = miamPolicyUpgradeService.populateMiamPolicyUpgradeDetails(callbackRequest);
        Assert.assertNotNull(objectMap);
        Assert.assertEquals("test", objectMap.get("mpuNoDomesticAbuseEvidenceReason"));
    }
}
