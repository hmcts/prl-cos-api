package uk.gov.hmcts.reform.prl.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class AllegationOfHarmRevisedServiceTest {

    @InjectMocks
    AllegationOfHarmRevisedService allegationOfHarmService;


    @Test
    public void testUpdateChildAbuses() {
        ChildAbuse childAbuse = ChildAbuse.builder().abuseNatureDescription("test")
                //.allChildrenAreRisk(YesOrNo.Yes)
            .build();
        AllegationOfHarmRevised data = AllegationOfHarmRevised.builder()
                        .childAbuseBehaviours(List.of(ChildAbuseEnum.physicalAbuse,ChildAbuseEnum.emotionalAbuse,ChildAbuseEnum
                        .psychologicalAbuse,ChildAbuseEnum.sexualAbuse,ChildAbuseEnum
                        .financialAbuse))
                .newAllegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
                        .childPhysicalAbuse(childAbuse)
                .childPsychologicalAbuse(childAbuse)
                .childEmotionalAbuse(childAbuse)
                .childFinancialAbuse(childAbuse
                        ).childSexualAbuse(childAbuse).build();
        CaseData caseData = CaseData.builder().allegationOfHarmRevised(data).build();

        caseData = allegationOfHarmService.updateChildAbuses(caseData);
        Assert.assertFalse(caseData.getAllegationOfHarmRevised().getChildAbuses().isEmpty());

    }


    @Test
    public void testPrePopulateChildData() {
        ChildDetailsRevised childDetailsRevised = ChildDetailsRevised.builder().firstName("child 1").lastName("last").build();
        Element<ChildDetailsRevised> childDetailsRevisedElement = Element.<ChildDetailsRevised>builder()
                .value(childDetailsRevised).id(UUID.randomUUID()).build();
        Map<String, Object> response = allegationOfHarmService.getPrePopulatedChildData(CaseData.builder()
                .newChildDetails(List.of(childDetailsRevisedElement)).build());
        Assert.assertFalse(response.isEmpty());

    }


}
