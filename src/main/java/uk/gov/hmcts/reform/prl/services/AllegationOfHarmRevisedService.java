package uk.gov.hmcts.reform.prl.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildAbuseBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.COMMA_SEPARATOR;

@Service
public class AllegationOfHarmRevisedService {


    public static final String CASE_FIELD_WHICH_CHILDREN_ARE_RISK = "whichChildrenAreRisk";

    public CaseData updateChildAbuses(CaseData caseData) {

        Optional<AllegationOfHarmRevised> allegationOfHarmRevised = Optional.ofNullable(caseData.getAllegationOfHarmRevised());
        if (allegationOfHarmRevised.isPresent() && YesOrNo.Yes.equals(caseData.getAllegationOfHarmRevised()
                .getNewAllegationsOfHarmChildAbuseYesNo())) {

            List<Element<ChildAbuseBehaviour>> childAbuses = new ArrayList<>();

            for (ChildAbuseEnum eachBehavior : allegationOfHarmRevised.get().getChildAbuseBehaviours()) {

                switch (eachBehavior.name()) {
                    case "physicalAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get(),
                                                          allegationOfHarmRevised.get().getChildPhysicalAbuse(),eachBehavior));
                        break;
                    case "psychologicalAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get(),
                                                          allegationOfHarmRevised.get().getChildPsychologicalAbuse(), eachBehavior));
                        break;
                    case "sexualAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get(),
                                                          allegationOfHarmRevised.get().getChildSexualAbuse(), eachBehavior));
                        break;
                    case "emotionalAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get(),
                                                          allegationOfHarmRevised.get().getChildEmotionalAbuse(), eachBehavior));
                        break;
                    case "financialAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get(),
                                                          allegationOfHarmRevised.get().getChildFinancialAbuse(), eachBehavior));
                        break;
                    default:

                }
            }
            return caseData.toBuilder().allegationOfHarmRevised(allegationOfHarmRevised.get()
                                                                    .toBuilder().childAbuses(childAbuses).build()).build();

        }

        return caseData;
    }

    private Element<ChildAbuseBehaviour> getChildBehaviour(AllegationOfHarmRevised allegationOfHarmRevised, ChildAbuse childAbuse,
                                                           ChildAbuseEnum childAbuseEnum) {
        return Element.<ChildAbuseBehaviour>builder().value(ChildAbuseBehaviour.builder()
                .abuseNatureDescription(childAbuse.getAbuseNatureDescription())
                .typeOfAbuse(childAbuseEnum)
                .behavioursApplicantHelpSoughtWho(childAbuse.getBehavioursApplicantHelpSoughtWho())
                .behavioursApplicantSoughtHelp(childAbuse.getBehavioursApplicantSoughtHelp())
                .behavioursStartDateAndLength(childAbuse.getBehavioursStartDateAndLength())
                                                                .allChildrenAreRisk(getIfAllChildrenAreRisk(childAbuseEnum,allegationOfHarmRevised))
                .whichChildrenAreRisk(YesOrNo.No.equals(getIfAllChildrenAreRisk(childAbuseEnum,allegationOfHarmRevised))
                                          ? (getWhichChildrenAreInRisk(childAbuseEnum,allegationOfHarmRevised)).getValue().stream()
                                          .map(DynamicMultiselectListElement::getCode)
                                          .collect(Collectors.joining(COMMA_SEPARATOR)) : null)
                                                                .build()).build();

    }

    public YesOrNo getIfAllChildrenAreRisk(ChildAbuseEnum childAbuseEnum, AllegationOfHarmRevised allegationOfHarmRevised) {
        YesOrNo areAllChildrenAtRisk = YesOrNo.Yes;
        switch (childAbuseEnum.name()) {
            case "physicalAbuse":
                areAllChildrenAtRisk =  allegationOfHarmRevised.getAllChildrenAreRiskPhysicalAbuse();
                break;
            case "psychologicalAbuse":
                areAllChildrenAtRisk =  allegationOfHarmRevised.getAllChildrenAreRiskPsychologicalAbuse();
                break;
            case "sexualAbuse":
                areAllChildrenAtRisk =  allegationOfHarmRevised.getAllChildrenAreRiskSexualAbuse();
                break;
            case "emotionalAbuse":
                areAllChildrenAtRisk =  allegationOfHarmRevised.getAllChildrenAreRiskEmotionalAbuse();
                break;
            case "financialAbuse":
                areAllChildrenAtRisk =  allegationOfHarmRevised.getAllChildrenAreRiskFinancialAbuse();
                break;
            default:

        }
        return  areAllChildrenAtRisk;
    }

    public DynamicMultiSelectList getWhichChildrenAreInRisk(ChildAbuseEnum childAbuseEnum, AllegationOfHarmRevised allegationOfHarmRevised) {
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().build();
        switch (childAbuseEnum.name()) {
            case "physicalAbuse":
                dynamicMultiSelectList =  allegationOfHarmRevised.getWhichChildrenAreRiskPhysicalAbuse();
                break;
            case "psychologicalAbuse":
                dynamicMultiSelectList =  allegationOfHarmRevised.getWhichChildrenAreRiskPsychologicalAbuse();
                break;
            case "sexualAbuse":
                dynamicMultiSelectList =  allegationOfHarmRevised.getWhichChildrenAreRiskSexualAbuse();
                break;
            case "emotionalAbuse":
                dynamicMultiSelectList =  allegationOfHarmRevised.getWhichChildrenAreRiskEmotionalAbuse();
                break;
            case "financialAbuse":
                dynamicMultiSelectList =  allegationOfHarmRevised.getWhichChildrenAreRiskFinancialAbuse();
                break;
            default:

        }
        return  dynamicMultiSelectList;
    }

    public Map<String, Object> getPrePopulatedChildData(CaseData caseData) {
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        caseData.getNewChildDetails().forEach(eachChild ->
            listItems.add(DynamicMultiselectListElement.builder().code(eachChild.getId().toString())
                    .label(eachChild.getValue().getFirstName() + " " + eachChild.getValue().getLastName()).build())
        );
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("whichChildrenAreRiskPhysicalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        caseDataMap.put("whichChildrenAreRiskPsychologicalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        caseDataMap.put("whichChildrenAreRiskSexualAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        caseDataMap.put("whichChildrenAreRiskEmotionalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        caseDataMap.put("whichChildrenAreRiskFinancialAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        return caseDataMap;
    }
}
