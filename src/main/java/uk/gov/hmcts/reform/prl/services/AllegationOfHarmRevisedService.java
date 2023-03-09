package uk.gov.hmcts.reform.prl.services;

import com.launchdarkly.shaded.com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class AllegationOfHarmRevisedService {


    public static final String CASE_FIELD_WHICH_CHILDREN_ARE_RISK = "whichChildrenAreRisk";

    public CaseData updateChildAbuses(CaseData caseData) {

        Optional<AllegationOfHarmRevised> allegationOfHarmRevised = Optional.ofNullable(caseData.getAllegationOfHarmRevised());
        log.info("service called");
        if (allegationOfHarmRevised.isPresent() && YesOrNo.Yes.equals(caseData.getAllegationOfHarmRevised()
                .getNewAllegationsOfHarmChildAbuseYesNo())) {

            log.info("service called its true");

            List<Element<ChildAbuseBehaviour>> childAbuses = new ArrayList<>();

            for (ChildAbuseEnum eachBehavior : allegationOfHarmRevised.get().getChildAbuseBehaviours()) {

                log.info("service called its true with behaviour {}", eachBehavior.name());
                switch (eachBehavior.name()) {
                    case "physicalAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get().getChildPhysicalAbuse(), eachBehavior));
                        break;
                    case "psychologicalAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get().getChildPsychologicalAbuse(), eachBehavior));
                        break;
                    case "sexualAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get().getChildSexualAbuse(), eachBehavior));
                        break;
                    case "emotionalAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get().getChildEmotionalAbuse(), eachBehavior));
                        break;
                    case "financialAbuse":
                        childAbuses.add(getChildBehaviour(allegationOfHarmRevised.get().getChildFinancialAbuse(), eachBehavior));
                        break;
                    default:

                }
            }
            log.info("allgetahm data :{}", new Gson()
                    .toJson(allegationOfHarmRevised.get().toBuilder().childAbuses(childAbuses).build()));

            log.info("allgetahm  with case data :{}", new Gson()
                    .toJson(caseData.toBuilder().allegationOfHarmRevised(allegationOfHarmRevised.get()
                            .toBuilder().childAbuses(childAbuses).build()).build()));
            return caseData.toBuilder().allegationOfHarmRevised(allegationOfHarmRevised.get().toBuilder().childAbuses(childAbuses).build()).build();

        }

        return caseData;
    }

    private Element<ChildAbuseBehaviour> getChildBehaviour(ChildAbuse childAbuse, ChildAbuseEnum eachBehavior) {
        return Element.<ChildAbuseBehaviour>builder().value(ChildAbuseBehaviour.builder()
                .abuseNatureDescription(childAbuse.getAbuseNatureDescription())
                .typeOfAbuse(eachBehavior)
                .behavioursApplicantHelpSoughtWho(childAbuse.getBehavioursApplicantHelpSoughtWho())
                .behavioursApplicantSoughtHelp(childAbuse.getBehavioursApplicantSoughtHelp())
                .behavioursStartDateAndLength(childAbuse.getBehavioursStartDateAndLength())
                .allChildrenAreRisk(childAbuse.getAllChildrenAreRisk())
                .whichChildrenAreRisk(YesOrNo.No.equals(childAbuse.getAllChildrenAreRisk()) ? childAbuse
                        .getWhichChildrenAreRisk().getValue().stream().map(DynamicMultiselectListElement::getCode)
                        .collect(Collectors.joining(COMMA_SEPARATOR)) : null)
                .build()).build();

    }

    public Map<String, Object> getPrePopulatedChildData(CaseData caseData) {
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        caseData.getNewChildDetails().forEach(eachChild ->
            listItems.add(DynamicMultiselectListElement.builder().code(eachChild.getId().toString())
                    .label(eachChild.getValue().getFirstName() + " " + eachChild.getValue().getLastName()).build())
        );
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("childPsychologicalAbuse", Map.of(CASE_FIELD_WHICH_CHILDREN_ARE_RISK, DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build()));
        caseDataMap.put("childPhysicalAbuse", Map.of(CASE_FIELD_WHICH_CHILDREN_ARE_RISK, DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build()));
        caseDataMap.put("childFinancialAbuse", Map.of(CASE_FIELD_WHICH_CHILDREN_ARE_RISK, DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build()));
        caseDataMap.put("childSexualAbuse", Map.of(CASE_FIELD_WHICH_CHILDREN_ARE_RISK, DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build()));
        caseDataMap.put("childEmotionalAbuse", Map.of(CASE_FIELD_WHICH_CHILDREN_ARE_RISK, DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build()));
        return caseDataMap;
    }
}
