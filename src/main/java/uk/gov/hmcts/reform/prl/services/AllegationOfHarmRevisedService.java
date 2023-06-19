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
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get().getWhichChildrenAreRiskPhysicalAbuse(),
                                                          allegationOfHarmRevised.get().getChildPhysicalAbuse(), eachBehavior));
                        break;
                    case "psychologicalAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get().getWhichChildrenAreRiskPsychologicalAbuse(),
                                                          allegationOfHarmRevised.get().getChildPsychologicalAbuse(), eachBehavior));
                        break;
                    case "sexualAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get().getWhichChildrenAreRiskSexualAbuse(),
                                                          allegationOfHarmRevised.get().getChildSexualAbuse(), eachBehavior));
                        break;
                    case "emotionalAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get().getWhichChildrenAreRiskEmotionalAbuse(),
                                                          allegationOfHarmRevised.get().getChildEmotionalAbuse(), eachBehavior));
                        break;
                    case "financialAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get().getWhichChildrenAreRiskFinancialAbuse(),
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

    private Element<ChildAbuseBehaviour> getChildBehaviour(DynamicMultiSelectList dynamicMultiSelectList, ChildAbuse childAbuse,
                                                           ChildAbuseEnum eachBehavior) {
        return Element.<ChildAbuseBehaviour>builder().value(ChildAbuseBehaviour.builder()
                .abuseNatureDescription(childAbuse.getAbuseNatureDescription())
                //.typeOfAbuse(eachBehavior)
                .behavioursApplicantHelpSoughtWho(childAbuse.getBehavioursApplicantHelpSoughtWho())
                .behavioursApplicantSoughtHelp(childAbuse.getBehavioursApplicantSoughtHelp())
                .behavioursStartDateAndLength(childAbuse.getBehavioursStartDateAndLength())
                                                                /*.allChildrenAreRisk(childAbuse.getAllChildrenAreRisk())
                .whichChildrenAreRisk(YesOrNo.No.equals(childAbuse.getAllChildrenAreRisk())
                                          ? dynamicMultiSelectList.getValue().stream().map(DynamicMultiselectListElement::getCode)
                 .collect(Collectors.joining(COMMA_SEPARATOR)) : null)*/
                                                                .build()).build();

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
