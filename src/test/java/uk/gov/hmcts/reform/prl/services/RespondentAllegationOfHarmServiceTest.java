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
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.RespChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespChildAbuseBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.c100respondentsolicitor.RespondentSolicitorData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;


@RunWith(MockitoJUnitRunner.class)
public class RespondentAllegationOfHarmServiceTest {

    @InjectMocks
    RespondentAllegationOfHarmService respondentAllegationOfHarmService;


    @Test
    public void testUpdateChildAbuses() {
        RespChildAbuse childAbuse = RespChildAbuse.builder().respAbuseNatureDescription("test")
                .build();
        RespondentAllegationsOfHarmData data = RespondentAllegationsOfHarmData.builder()
                .respChildAbuses(List.of(ChildAbuseEnum.physicalAbuse, ChildAbuseEnum.emotionalAbuse, ChildAbuseEnum
                        .psychologicalAbuse, ChildAbuseEnum.sexualAbuse, ChildAbuseEnum
                        .financialAbuse))
                .respAohChildAbuseYesNo(YesOrNo.Yes)
                .respAllChildrenAreRiskPhysicalAbuse(YesOrNo.Yes)
                .respAllChildrenAreRiskPsychologicalAbuse(YesOrNo.Yes)
                .respAllChildrenAreRiskEmotionalAbuse(YesOrNo.Yes)
                .respAllChildrenAreRiskFinancialAbuse(YesOrNo.Yes)
                .respAllChildrenAreRiskSexualAbuse(YesOrNo.Yes)
                .respWhichChildrenAreRiskPhysicalAbuse(DynamicMultiSelectList.builder()
                        .value(List.of(DynamicMultiselectListElement.builder()
                                .label("John (Child 1)")
                                .code("00000000-0000-0000-0000-000000000000")
                                .build())).build())
                .respWhichChildrenAreRiskPsychologicalAbuse(DynamicMultiSelectList.builder()
                        .value(List.of(DynamicMultiselectListElement.builder()
                                .label("John (Child 1)")
                                .code("00000000-0000-0000-0000-000000000000")
                                .build())).build())
                .respWhichChildrenAreRiskSexualAbuse(DynamicMultiSelectList.builder()
                        .value(List.of(DynamicMultiselectListElement.builder()
                                .label("John (Child 1)")
                                .code("00000000-0000-0000-0000-000000000000")
                                .build())).build())
                .respWhichChildrenAreRiskEmotionalAbuse(DynamicMultiSelectList.builder()
                        .value(List.of(DynamicMultiselectListElement.builder()
                                .label("John (Child 1)")
                                .code("00000000-0000-0000-0000-000000000000")
                                .build())).build())
                .respWhichChildrenAreRiskFinancialAbuse(DynamicMultiSelectList.builder()
                        .value(List.of(DynamicMultiselectListElement.builder()
                                .label("John (Child 1)")
                                .code("00000000-0000-0000-0000-000000000000")
                                .build())).build())
                .respChildPhysicalAbuse(childAbuse)
                .respChildPsychologicalAbuse(childAbuse)
                .respChildEmotionalAbuse(childAbuse)
                .respChildFinancialAbuse(childAbuse)
                .respChildSexualAbuse(childAbuse).build();

        List<Element<RespChildAbuseBehaviour>> response = respondentAllegationOfHarmService.updateChildAbusesForDocmosis(data);
        Assert.assertFalse(response.isEmpty());

    }


    @Test
    public void testPrePopulateChildData() {
        List<DynamicMultiselectListElement> valueElements = new ArrayList<>();
        valueElements.add(DynamicMultiselectListElement.builder().code("test").label("test name").build());

        List<DynamicMultiselectListElement> listItemsElements = new ArrayList<>();
        listItemsElements.add(DynamicMultiselectListElement.builder().code("test1").label("test1 name").build());
        listItemsElements.add(DynamicMultiselectListElement.builder().code("test2").label("test2 name").build());

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().value(valueElements).listItems(listItemsElements).build();

        RespondentAllegationsOfHarmData allegationOfHarmRevised = RespondentAllegationsOfHarmData.builder()
                .respWhichChildrenAreRiskPhysicalAbuse(dynamicMultiSelectList)
                .respWhichChildrenAreRiskPsychologicalAbuse(dynamicMultiSelectList)
                .respWhichChildrenAreRiskSexualAbuse(dynamicMultiSelectList)
                .respWhichChildrenAreRiskEmotionalAbuse(dynamicMultiSelectList)
                .respWhichChildrenAreRiskFinancialAbuse(dynamicMultiSelectList)
                .build();

        ChildDetailsRevised childDetailsRevised = ChildDetailsRevised.builder().firstName("child 1").lastName("last").build();
        Element<ChildDetailsRevised> childDetailsRevisedElement = Element.<ChildDetailsRevised>builder()
                .value(childDetailsRevised).id(UUID.randomUUID()).build();

        Map<String, Object> data = new HashMap<>();
        respondentAllegationOfHarmService
                .prePopulatedChildData(CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2)
                        .respondentSolicitorData(RespondentSolicitorData.builder()
                                .respondentAllegationsOfHarmData(allegationOfHarmRevised).build())
                        .newChildDetails(List.of(childDetailsRevisedElement)).build(),data,allegationOfHarmRevised);
        Assert.assertFalse(data.isEmpty());

    }

    @Test
    public void testPrePopulateChildDataWithoutAnyAbuses() {
        List<DynamicMultiselectListElement> valueElements = new ArrayList<>();
        valueElements.add(DynamicMultiselectListElement.builder().code("test").label("test name").build());

        List<DynamicMultiselectListElement> listItemsElements = new ArrayList<>();
        listItemsElements.add(DynamicMultiselectListElement.builder().code("test1").label("test1 name").build());
        listItemsElements.add(DynamicMultiselectListElement.builder().code("test2").label("test2 name").build());

        RespondentAllegationsOfHarmData allegationOfHarmRevised = RespondentAllegationsOfHarmData.builder()
                .build();

        ChildDetailsRevised childDetailsRevised = ChildDetailsRevised.builder().firstName("child 1").lastName("last").build();
        Element<ChildDetailsRevised> childDetailsRevisedElement = Element.<ChildDetailsRevised>builder()
                .value(childDetailsRevised).id(UUID.randomUUID()).build();
        Child child = Child.builder().firstName("child 1").lastName("last").build();
        Element<Child> childElement = Element.<Child>builder()
                .value(child).id(UUID.randomUUID()).build();

        Map<String, Object> data = new HashMap<>();
        respondentAllegationOfHarmService
                .prePopulatedChildData(CaseData.builder()
                        .respondentSolicitorData(RespondentSolicitorData.builder()
                                .respondentAllegationsOfHarmData(allegationOfHarmRevised).build())
                        .newChildDetails(List.of(childDetailsRevisedElement)).children(List.of(childElement))
                        .build(),data,allegationOfHarmRevised);
        Assert.assertFalse(data.isEmpty());

    }

    @Test
    public void testGetWhichChildrenAreInRiskPhysicalAbuse() {
        RespondentAllegationsOfHarmData allegationOfHarmRevised = RespondentAllegationsOfHarmData.builder()
                .respAllChildrenAreRiskPhysicalAbuse(YesOrNo.No)
                .respWhichChildrenAreRiskPhysicalAbuse(DynamicMultiSelectList.builder()
                        .value(List.of(DynamicMultiselectListElement.builder()
                                .label("John (Child 1)")
                                .code("00000000-0000-0000-0000-000000000000")
                                .build())).build())
                .build();
        DynamicMultiSelectList childrenInRisk = respondentAllegationOfHarmService
                .getWhichChildrenAreInRisk(ChildAbuseEnum.physicalAbuse,allegationOfHarmRevised);
        Assert.assertNotNull(childrenInRisk);

    }

    @Test
    public void testGetWhichChildrenAreInRiskPsychologicalAbuse() {
        RespondentAllegationsOfHarmData allegationOfHarmRevised = RespondentAllegationsOfHarmData.builder()
                .respAllChildrenAreRiskPsychologicalAbuse(YesOrNo.No)
                .respWhichChildrenAreRiskPsychologicalAbuse(DynamicMultiSelectList.builder()
                        .value(List.of(DynamicMultiselectListElement.builder()
                                .label("John (Child 1)")
                                .code("00000000-0000-0000-0000-000000000000")
                                .build())).build())
                .build();
        DynamicMultiSelectList childrenInRisk = respondentAllegationOfHarmService
                .getWhichChildrenAreInRisk(ChildAbuseEnum.psychologicalAbuse,allegationOfHarmRevised);
        Assert.assertNotNull(childrenInRisk);

    }

    @Test
    public void testGetWhichChildrenAreInRiskSexualAbuse() {
        RespondentAllegationsOfHarmData allegationOfHarmRevised = RespondentAllegationsOfHarmData.builder()
                .respAllChildrenAreRiskSexualAbuse(YesOrNo.No)
                .respWhichChildrenAreRiskSexualAbuse(DynamicMultiSelectList.builder()
                        .value(List.of(DynamicMultiselectListElement.builder()
                                .label("John (Child 1)")
                                .code("00000000-0000-0000-0000-000000000000")
                                .build())).build())
                .build();
        DynamicMultiSelectList childrenInRisk = respondentAllegationOfHarmService
                .getWhichChildrenAreInRisk(ChildAbuseEnum.sexualAbuse,allegationOfHarmRevised);
        Assert.assertNotNull(childrenInRisk);

    }

    @Test
    public void testGetWhichChildrenAreInRiskEmotionalAbuse() {
        RespondentAllegationsOfHarmData allegationOfHarmRevised = RespondentAllegationsOfHarmData.builder()
                .respAllChildrenAreRiskEmotionalAbuse(YesOrNo.No)
                .respWhichChildrenAreRiskEmotionalAbuse(DynamicMultiSelectList.builder()
                        .value(List.of(DynamicMultiselectListElement.builder()
                                .label("John (Child 1)")
                                .code("00000000-0000-0000-0000-000000000000")
                                .build())).build())
                .build();
        DynamicMultiSelectList childrenInRisk = respondentAllegationOfHarmService
                .getWhichChildrenAreInRisk(ChildAbuseEnum.emotionalAbuse,allegationOfHarmRevised);
        Assert.assertNotNull(childrenInRisk);

    }

    @Test
    public void testGetWhichChildrenAreInRiskFinancialAbuse() {
        RespondentAllegationsOfHarmData allegationOfHarmRevised = RespondentAllegationsOfHarmData.builder()
                .respAllChildrenAreRiskFinancialAbuse(YesOrNo.No)
                .respWhichChildrenAreRiskFinancialAbuse(DynamicMultiSelectList.builder()
                        .value(List.of(DynamicMultiselectListElement.builder()
                                .label("John (Child 1)")
                                .code("00000000-0000-0000-0000-000000000000")
                                .build())).build())
                .build();
        DynamicMultiSelectList childrenInRisk = respondentAllegationOfHarmService
                .getWhichChildrenAreInRisk(ChildAbuseEnum.financialAbuse,allegationOfHarmRevised);
        Assert.assertNotNull(childrenInRisk);

    }


}
