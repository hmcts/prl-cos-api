package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;

import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamChildProtectionConcernChecklistEnum.MIAMChildProtectionConcernChecklistEnum_value_1;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_2;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.childProtectionConcern;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.domesticAbuse;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.other;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.previousMiamAttendance;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.urgency;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_3;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_2;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_1;

@RunWith(MockitoJUnitRunner.class)
public class MiamPolicyUpgradeMapperTest {

    @InjectMocks
    MiamPolicyUpgradeMapper miamPolicyUpgradeMapper;

    @Test
    public void testMapWithEmptyMiamDetails() {
        CaseData caseData = CaseData.builder().miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder().build()).build();
        Assert.assertNotNull(miamPolicyUpgradeMapper.map(caseData));
    }

    @Test
    public void testMap() {
        CaseData caseData = CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                                          .builder()
                                          .mpuChildInvolvedInMiam(YesOrNo.No)
                                          .mpuApplicantAttendedMiam(YesOrNo.No)
                                          .mpuClaimingExemptionMiam(YesOrNo.Yes)
                                          .mpuExemptionReasons(List.of(
                                              domesticAbuse,
                                              childProtectionConcern,
                                              urgency,
                                              previousMiamAttendance,
                                              other
                                          ))
                                          .mpuDomesticAbuseEvidences(List.of(
                                              miamDomesticAbuseChecklistEnum_Value_1,
                                              miamDomesticAbuseChecklistEnum_Value_2
                                          ))
                                          .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
                                          .mpuChildProtectionConcernReason(MIAMChildProtectionConcernChecklistEnum_value_1)
                                          .mpuUrgencyReason(miamPolicyUpgradeUrgencyReason_Value_1)
                                          .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
                                          .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamAttendanceDetails)
                                          .mpuOtherExemptionReasons(miamPolicyUpgradeOtherGrounds_Value_3)
                                          .build())
            .build();
        Assert.assertNotNull(miamPolicyUpgradeMapper.map(caseData));
    }
}

