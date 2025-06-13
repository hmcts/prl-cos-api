package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_2;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuChildProtectionConcern;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuDomesticAbuse;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuOther;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuUrgency;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_3;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPolicyUpgradeChildProtectionConcernEnum.mpuChildProtectionConcern_value_1;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_2;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_1;

@ExtendWith(MockitoExtension.class)
class MiamPolicyUpgradeMapperTest {

    @InjectMocks
    MiamPolicyUpgradeMapper miamPolicyUpgradeMapper;

    @Test
    void testMapWithEmptyMiamDetails() {
        CaseData caseData = CaseData.builder().miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder().build()).build();
        assertNotNull(miamPolicyUpgradeMapper.map(caseData));
    }

    @Test
    void testMap() {
        CaseData caseData = CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                                          .builder()
                                          .mpuChildInvolvedInMiam(YesOrNo.No)
                                          .mpuApplicantAttendedMiam(YesOrNo.No)
                                          .mpuClaimingExemptionMiam(YesOrNo.Yes)
                                          .mpuExemptionReasons(List.of(
                                              mpuDomesticAbuse,
                                              mpuChildProtectionConcern,
                                              mpuUrgency,
                                              mpuPreviousMiamAttendance,
                                              mpuOther
                                          ))
                                          .mpuDomesticAbuseEvidences(List.of(
                                              miamDomesticAbuseChecklistEnum_Value_1,
                                              miamDomesticAbuseChecklistEnum_Value_2
                                          ))
                                          .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
                                          .mpuChildProtectionConcernReason(mpuChildProtectionConcern_value_1)
                                          .mpuUrgencyReason(miamPolicyUpgradeUrgencyReason_Value_1)
                                          .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
                                          .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamAttendanceDetails)
                                          .mpuOtherExemptionReasons(miamPolicyUpgradeOtherGrounds_Value_3)
                                          .build())
            .build();
        assertNotNull(miamPolicyUpgradeMapper.map(caseData));
    }
}

