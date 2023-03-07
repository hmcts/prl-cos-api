package uk.gov.hmcts.reform.prl.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AllegationOfHarmServiceTest {

    @InjectMocks
    AllegationOfHarmService allegationOfHarmService;


    @Test
    public void testUpdateChildAbuses() {
        AllegationOfHarmRevised data = AllegationOfHarmRevised.builder().childAbuseBehaviours(List.of(ChildAbuseEnum.physicalAbuse))
                .newAllegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
                        .childPhysicalAbuse(ChildAbuse.builder().abuseNatureDescription("test")
                                .allChildrenAreRisk(YesOrNo.Yes).build()).build();
        CaseData caseData = CaseData.builder().allegationOfHarmRevised(data).build();

        caseData = allegationOfHarmService.updateChildAbuses(caseData);
        Assert.assertFalse(caseData.getAllegationOfHarmRevised().getChildAbuses().isEmpty());

    }


}