package uk.gov.hmcts.reform.prl.services;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class AllegationOfHarmRevisedServiceTest {

    @InjectMocks
    AllegationOfHarmRevisedService allegationOfHarmService;


    @Test
    void testUpdateChildAbuses() {
        ChildAbuse childAbuse = ChildAbuse.builder().abuseNatureDescription("test")
                .typeOfAbuse(ChildAbuseEnum.physicalAbuse)
            .build();
        AllegationOfHarmRevised data = AllegationOfHarmRevised.builder()
            .childAbuses(List.of(ChildAbuseEnum.physicalAbuse, ChildAbuseEnum.emotionalAbuse, ChildAbuseEnum
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

        caseData = allegationOfHarmService.updateChildAbusesForDocmosis(caseData);
        assertFalse(caseData.getAllegationOfHarmRevised().getChildAbuses().isEmpty());

    }


    @Test
    void testPrePopulateChildData() {
        List<DynamicMultiselectListElement> valueElements = new ArrayList<>();
        valueElements.add(DynamicMultiselectListElement.builder().code("test").label("test name").build());

        List<DynamicMultiselectListElement> listItemsElements = new ArrayList<>();
        listItemsElements.add(DynamicMultiselectListElement.builder().code("test1").label("test1 name").build());
        listItemsElements.add(DynamicMultiselectListElement.builder().code("test2").label("test2 name").build());

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().value(valueElements).listItems(listItemsElements).build();

        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
            .whichChildrenAreRiskPhysicalAbuse(dynamicMultiSelectList)
            .whichChildrenAreRiskPsychologicalAbuse(dynamicMultiSelectList)
            .whichChildrenAreRiskSexualAbuse(dynamicMultiSelectList)
            .whichChildrenAreRiskEmotionalAbuse(dynamicMultiSelectList)
            .whichChildrenAreRiskFinancialAbuse(dynamicMultiSelectList)
            .build();

        ChildDetailsRevised childDetailsRevised = ChildDetailsRevised.builder().firstName("child 1").lastName("last").build();
        Element<ChildDetailsRevised> childDetailsRevisedElement = Element.<ChildDetailsRevised>builder()
            .value(childDetailsRevised).id(UUID.randomUUID()).build();

        Map<String, Object> response = allegationOfHarmService
            .getPrePopulatedChildData(CaseData.builder()
                                          .allegationOfHarmRevised(allegationOfHarmRevised)
                                          .newChildDetails(List.of(childDetailsRevisedElement)).build());
        assertFalse(response.isEmpty());

    }

    @Test
    void testPrePopulateChildDataWithoutAnyAbuses() {

        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
            .build();

        ChildDetailsRevised childDetailsRevised = ChildDetailsRevised.builder().firstName("child 1").lastName("last").build();
        Element<ChildDetailsRevised> childDetailsRevisedElement = Element.<ChildDetailsRevised>builder()
            .value(childDetailsRevised).id(UUID.randomUUID()).build();

        Map<String, Object> response = allegationOfHarmService
            .getPrePopulatedChildData(CaseData.builder().allegationOfHarmRevised(allegationOfHarmRevised)
                                          .newChildDetails(List.of(childDetailsRevisedElement)).build());
        assertFalse(response.isEmpty());

    }

    @Test
    void testGetWhichChildrenAreInRiskPhysicalAbuse() {
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
                .allChildrenAreRiskPhysicalAbuse(YesOrNo.No)
            .whichChildrenAreRiskPhysicalAbuse(DynamicMultiSelectList.builder()
                                                   .value(List.of(DynamicMultiselectListElement.builder()
                                                                      .label("John (Child 1)")
                                                                      .code("00000000-0000-0000-0000-000000000000")
                                                                      .build())).build())
                .build();
        DynamicMultiSelectList childrenInRisk = allegationOfHarmService
            .getWhichChildrenAreInRisk(ChildAbuseEnum.physicalAbuse,allegationOfHarmRevised);
        assertNotNull(childrenInRisk);

    }

    @Test
    void testGetWhichChildrenAreInRiskPsychologicalAbuse() {
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
                .allChildrenAreRiskPsychologicalAbuse(YesOrNo.No)
            .whichChildrenAreRiskPsychologicalAbuse(DynamicMultiSelectList.builder()
                                                   .value(List.of(DynamicMultiselectListElement.builder()
                                                                      .label("John (Child 1)")
                                                                      .code("00000000-0000-0000-0000-000000000000")
                                                                      .build())).build())
            .build();
        DynamicMultiSelectList childrenInRisk = allegationOfHarmService
            .getWhichChildrenAreInRisk(ChildAbuseEnum.psychologicalAbuse,allegationOfHarmRevised);
        assertNotNull(childrenInRisk);

    }

    @Test
    void testGetWhichChildrenAreInRiskSexualAbuse() {
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
            .whichChildrenAreRiskSexualAbuse(DynamicMultiSelectList.builder()
                                                        .value(List.of(DynamicMultiselectListElement.builder()
                                                                           .label("John (Child 1)")
                                                                           .code("00000000-0000-0000-0000-000000000000")
                                                                           .build())).build())
                .allChildrenAreRiskSexualAbuse(YesOrNo.No)
            .build();
        DynamicMultiSelectList childrenInRisk = allegationOfHarmService
            .getWhichChildrenAreInRisk(ChildAbuseEnum.sexualAbuse,allegationOfHarmRevised);
        assertNotNull(childrenInRisk);

    }

    @Test
    void testGetWhichChildrenAreInRiskEmotionalAbuse() {
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
                .allChildrenAreRiskEmotionalAbuse(YesOrNo.No)
            .whichChildrenAreRiskEmotionalAbuse(DynamicMultiSelectList.builder()
                                                 .value(List.of(DynamicMultiselectListElement.builder()
                                                                    .label("John (Child 1)")
                                                                    .code("00000000-0000-0000-0000-000000000000")
                                                                    .build())).build())
            .build();
        DynamicMultiSelectList childrenInRisk = allegationOfHarmService
            .getWhichChildrenAreInRisk(ChildAbuseEnum.emotionalAbuse,allegationOfHarmRevised);
        assertNotNull(childrenInRisk);

    }

    @Test
    void testGetWhichChildrenAreInRiskFinancialAbuse() {
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
                .allChildrenAreRiskFinancialAbuse(YesOrNo.No)
            .whichChildrenAreRiskFinancialAbuse(DynamicMultiSelectList.builder()
                                                    .value(List.of(DynamicMultiselectListElement.builder()
                                                                       .label("John (Child 1)")
                                                                       .code("00000000-0000-0000-0000-000000000000")
                                                                       .build())).build())
            .build();
        DynamicMultiSelectList childrenInRisk = allegationOfHarmService
            .getWhichChildrenAreInRisk(ChildAbuseEnum.financialAbuse,allegationOfHarmRevised);
        assertNotNull(childrenInRisk);

    }

    @Test
    void testResetFields1() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("newAllegationsOfHarmSubstanceAbuseDetails", "test");
        caseDataMap.put("newAllegationsOfHarmOtherConcernsDetails", "test");
        caseDataMap.put("childAbuses", List.of(ChildAbuseEnum.physicalAbuse, ChildAbuseEnum.emotionalAbuse));
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
            .allChildrenAreRiskFinancialAbuse(YesOrNo.No)
            .newAllegationsOfHarmChildAbuseYesNo(YesOrNo.No)
            .newAllegationsOfHarmSubstanceAbuseYesNo(YesOrNo.No)
            .newAllegationsOfHarmOtherConcerns(YesOrNo.No)
            .build();
        allegationOfHarmService
            .resetFields(CaseData.builder().allegationOfHarmRevised(allegationOfHarmRevised).build(), caseDataMap);
        assertEquals(StringUtils.EMPTY, caseDataMap.get("newAllegationsOfHarmSubstanceAbuseDetails"));
    }

    @Test
    void testResetFields2() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("newAllegationsOfHarmSubstanceAbuseDetails","test");
        caseDataMap.put("newAllegationsOfHarmOtherConcernsDetails", "test");
        caseDataMap.put("childAbuses",List.of(ChildAbuseEnum.physicalAbuse,ChildAbuseEnum.emotionalAbuse));
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder()
            .allChildrenAreRiskFinancialAbuse(YesOrNo.No)
            .newAllegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
            .newAllegationsOfHarmSubstanceAbuseYesNo(YesOrNo.Yes)
            .newAllegationsOfHarmOtherConcerns(YesOrNo.Yes)
            .build();
        allegationOfHarmService
            .resetFields(CaseData.builder().allegationOfHarmRevised(allegationOfHarmRevised).build(),caseDataMap);
        assertNotNull(caseDataMap.get("childAbuses"));
        assertEquals("test",caseDataMap.get("newAllegationsOfHarmSubstanceAbuseDetails"));
    }

    @Test
    void testResetFields3() {
        allegationOfHarmService
            .resetFields(CaseData.builder().allegationOfHarmRevised(null).build(),null);
        assertNull(null);
    }

}
