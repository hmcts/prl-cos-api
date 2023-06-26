package uk.gov.hmcts.reform.prl.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
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
            .build();
        AllegationOfHarmRevised data = AllegationOfHarmRevised.builder()
            .childAbuseBehaviours(List.of(ChildAbuseEnum.physicalAbuse, ChildAbuseEnum.emotionalAbuse, ChildAbuseEnum
                .psychologicalAbuse, ChildAbuseEnum.sexualAbuse, ChildAbuseEnum
                                              .financialAbuse))
            .newAllegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
            .allChildrenAreRiskPhysicalAbuse(YesOrNo.Yes)
            .allChildrenAreRiskPsychologicalAbuse(YesOrNo.Yes)
            .allChildrenAreRiskEmotionalAbuse(YesOrNo.Yes)
            .allChildrenAreRiskFinancialAbuse(YesOrNo.Yes)
            .allChildrenAreRiskSexualAbuse(YesOrNo.Yes)
            .whichChildrenAreRiskPhysicalAbuse(DynamicMultiSelectList.builder()
                                                   .value(List.of(DynamicMultiselectListElement.builder()
                                                                      .label("John (Child 1)")
                                                                      .code("00000000-0000-0000-0000-000000000000")
                                                                      .build())).build())
            .whichChildrenAreRiskPsychologicalAbuse(DynamicMultiSelectList.builder()
                                                        .value(List.of(DynamicMultiselectListElement.builder()
                                                                           .label("John (Child 1)")
                                                                           .code("00000000-0000-0000-0000-000000000000")
                                                                           .build())).build())
            .whichChildrenAreRiskSexualAbuse(DynamicMultiSelectList.builder()
                                                 .value(List.of(DynamicMultiselectListElement.builder()
                                                                    .label("John (Child 1)")
                                                                    .code("00000000-0000-0000-0000-000000000000")
                                                                    .build())).build())
            .whichChildrenAreRiskEmotionalAbuse(DynamicMultiSelectList.builder()
                                                    .value(List.of(DynamicMultiselectListElement.builder()
                                                                       .label("John (Child 1)")
                                                                       .code("00000000-0000-0000-0000-000000000000")
                                                                       .build())).build())
            .whichChildrenAreRiskFinancialAbuse(DynamicMultiSelectList.builder()
                                                    .value(List.of(DynamicMultiselectListElement.builder()
                                                                       .label("John (Child 1)")
                                                                       .code("00000000-0000-0000-0000-000000000000")
                                                                       .build())).build())
            .childPhysicalAbuse(childAbuse)
            .childPsychologicalAbuse(childAbuse)
            .childEmotionalAbuse(childAbuse)
            .childFinancialAbuse(childAbuse)
            .childSexualAbuse(childAbuse).build();
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
                                                                                            .newChildDetails(List.of(
                                                                                                childDetailsRevisedElement)).build());
        Assert.assertFalse(response.isEmpty());

    }

    @Test
    public void testGetWhichChildrenAreInRiskPhysicalAbuse() {
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
            .whichChildrenAreRiskPhysicalAbuse(DynamicMultiSelectList.builder()
                                                   .value(List.of(DynamicMultiselectListElement.builder()
                                                                      .label("John (Child 1)")
                                                                      .code("00000000-0000-0000-0000-000000000000")
                                                                      .build())).build())
                .build();
        DynamicMultiSelectList childrenInRisk = allegationOfHarmService
            .getWhichChildrenAreInRisk(ChildAbuseEnum.physicalAbuse,allegationOfHarmRevised);
        Assert.assertNotNull(childrenInRisk);

    }

    @Test
    public void testGetWhichChildrenAreInRiskPhsychologicalAbuse() {
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
            .whichChildrenAreRiskPsychologicalAbuse(DynamicMultiSelectList.builder()
                                                   .value(List.of(DynamicMultiselectListElement.builder()
                                                                      .label("John (Child 1)")
                                                                      .code("00000000-0000-0000-0000-000000000000")
                                                                      .build())).build())
            .build();
        DynamicMultiSelectList childrenInRisk = allegationOfHarmService
            .getWhichChildrenAreInRisk(ChildAbuseEnum.psychologicalAbuse,allegationOfHarmRevised);
        Assert.assertNotNull(childrenInRisk);

    }

    @Test
    public void testGetWhichChildrenAreInRiskSexualAbuse() {
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
            .whichChildrenAreRiskSexualAbuse(DynamicMultiSelectList.builder()
                                                        .value(List.of(DynamicMultiselectListElement.builder()
                                                                           .label("John (Child 1)")
                                                                           .code("00000000-0000-0000-0000-000000000000")
                                                                           .build())).build())
            .build();
        DynamicMultiSelectList childrenInRisk = allegationOfHarmService
            .getWhichChildrenAreInRisk(ChildAbuseEnum.sexualAbuse,allegationOfHarmRevised);
        Assert.assertNotNull(childrenInRisk);

    }

    @Test
    public void testGetWhichChildrenAreInRiskEmotionalAbuse() {
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
            .whichChildrenAreRiskEmotionalAbuse(DynamicMultiSelectList.builder()
                                                 .value(List.of(DynamicMultiselectListElement.builder()
                                                                    .label("John (Child 1)")
                                                                    .code("00000000-0000-0000-0000-000000000000")
                                                                    .build())).build())
            .build();
        DynamicMultiSelectList childrenInRisk = allegationOfHarmService
            .getWhichChildrenAreInRisk(ChildAbuseEnum.emotionalAbuse,allegationOfHarmRevised);
        Assert.assertNotNull(childrenInRisk);

    }

    @Test
    public void testGetWhichChildrenAreInRiskFinancialAbuse() {
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
            .whichChildrenAreRiskFinancialAbuse(DynamicMultiSelectList.builder()
                                                    .value(List.of(DynamicMultiselectListElement.builder()
                                                                       .label("John (Child 1)")
                                                                       .code("00000000-0000-0000-0000-000000000000")
                                                                       .build())).build())
            .build();
        DynamicMultiSelectList childrenInRisk = allegationOfHarmService
            .getWhichChildrenAreInRisk(ChildAbuseEnum.financialAbuse,allegationOfHarmRevised);
        Assert.assertNotNull(childrenInRisk);

    }


}
