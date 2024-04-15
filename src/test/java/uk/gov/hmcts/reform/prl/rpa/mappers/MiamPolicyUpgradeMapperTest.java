package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamDetails;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MiamPolicyUpgradeMapperTest {

    @InjectMocks
    MiamPolicyUpgradeMapper miamPolicyUpgradeMapper;

    @Test
    public void testMapWithEmptyMiamDetails() {
        CaseData caseData = CaseData.builder().miamDetails(MiamDetails.builder().build()).build();
        Assert.assertNotNull(miamPolicyUpgradeMapper.map(caseData));
    }

    @Test
    public void testMap() {
        CaseData caseData = CaseData
            .builder()
            .miamDetails(MiamDetails
                .builder()
                .miamDomesticViolenceChecklist(List.of(MiamDomesticViolenceChecklistEnum
                    .miamDomesticViolenceChecklistEnum_Value_1))
                .miamUrgencyReasonChecklist(List.of(MiamUrgencyReasonChecklistEnum
                    .miamUrgencyReasonChecklistEnum_Value_1))
                .miamPreviousAttendanceChecklist(MiamPreviousAttendanceChecklistEnum
                    .miamPreviousAttendanceChecklistEnum_Value_1)
                .miamOtherGroundsChecklist(MiamOtherGroundsChecklistEnum
                    .miamOtherGroundsChecklistEnum_Value_1)
                .build())
            .build();
        Assert.assertNotNull(miamPolicyUpgradeMapper.map(caseData));
    }
}
