package uk.gov.hmcts.reform.prl.services;

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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMOTIONAL_ABUSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINANCIAL_ABUSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PHYSICAL_ABUSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PSYCHOLOGICAL_ABUSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SEXUAL_ABUSE;

@Slf4j
@Service
public class AllegationOfHarmRevisedService {


    public static final String CASE_FIELD_WHICH_CHILDREN_ARE_RISK = "whichChildrenAreRisk";

    public CaseData updateChildAbusesForDocmosis(CaseData caseData) {

        Optional<AllegationOfHarmRevised> allegationOfHarmRevised = Optional.ofNullable(caseData.getAllegationOfHarmRevised());
        if (allegationOfHarmRevised.isPresent() && YesOrNo.Yes.equals(caseData.getAllegationOfHarmRevised()
                                                                          .getNewAllegationsOfHarmChildAbuseYesNo())) {
            Optional<ChildAbuse> childPhysicalAbuse =
                    ofNullable(allegationOfHarmRevised.get().getChildPhysicalAbuse());

            Optional<ChildAbuse> childPsychologicalAbuse =
                    ofNullable(allegationOfHarmRevised.get().getChildPsychologicalAbuse());


            Optional<ChildAbuse> childEmotionalAbuse =
                    ofNullable(allegationOfHarmRevised.get().getChildEmotionalAbuse());


            Optional<ChildAbuse> childSexualAbuse =
                    ofNullable(allegationOfHarmRevised.get().getChildSexualAbuse());


            Optional<ChildAbuse> childFinancialAbuse =
                    ofNullable(allegationOfHarmRevised.get().getChildFinancialAbuse());

            List<Element<ChildAbuseBehaviour>> childAbuseBehaviourList = new ArrayList<>();

            for (ChildAbuseEnum eachBehavior : allegationOfHarmRevised.get().getChildAbuses()) {

                switch (eachBehavior.name()) {
                    case PHYSICAL_ABUSE : childPhysicalAbuse.ifPresent(abuse ->
                                checkAndAddChildAbuse(allegationOfHarmRevised.get(), childAbuseBehaviourList, eachBehavior, abuse)

                    );
                    break;
                    case PSYCHOLOGICAL_ABUSE : childPsychologicalAbuse.ifPresent(abuse ->
                                checkAndAddChildAbuse(allegationOfHarmRevised.get(), childAbuseBehaviourList, eachBehavior, abuse)

                    );
                    break;
                    case SEXUAL_ABUSE : childSexualAbuse.ifPresent(abuse ->
                                checkAndAddChildAbuse(allegationOfHarmRevised.get(), childAbuseBehaviourList, eachBehavior, abuse)

                    );
                    break;
                    case EMOTIONAL_ABUSE : childEmotionalAbuse.ifPresent(abuse ->
                                checkAndAddChildAbuse(allegationOfHarmRevised.get(), childAbuseBehaviourList, eachBehavior, abuse)

                    );
                    break;
                    case FINANCIAL_ABUSE : childFinancialAbuse.ifPresent(abuse ->
                                checkAndAddChildAbuse(allegationOfHarmRevised.get(), childAbuseBehaviourList, eachBehavior, abuse)

                    );
                    break;
                    default : {
                        //
                    }
                }
            }
            return caseData.toBuilder().allegationOfHarmRevised(allegationOfHarmRevised.get()
                    .toBuilder().childAbuseBehavioursDocmosis(childAbuseBehaviourList).build()).build();

        }

        return caseData;
    }

    private void checkAndAddChildAbuse(AllegationOfHarmRevised allegationOfHarmRevised, List<Element<ChildAbuseBehaviour>> childAbuseBehaviourList,
                                       ChildAbuseEnum eachBehavior, ChildAbuse abuse) {
        if (Objects.nonNull(abuse.getTypeOfAbuse())) {
            childAbuseBehaviourList.add(getChildBehaviour(
                    allegationOfHarmRevised,
                    abuse,
                    eachBehavior
            ));
        }
    }

    private Element<ChildAbuseBehaviour> getChildBehaviour(AllegationOfHarmRevised allegationOfHarmRevised, ChildAbuse childAbuse,
                                                           ChildAbuseEnum childAbuseEnum) {

        Optional<DynamicMultiSelectList> whichChildrenAreRisk = ofNullable(
                getWhichChildrenAreInRisk(childAbuse.getTypeOfAbuse(), allegationOfHarmRevised));
        return Element.<ChildAbuseBehaviour>builder().value(ChildAbuseBehaviour.builder()
                                                                .abuseNatureDescription(childAbuse.getAbuseNatureDescription())
                                                                .typeOfAbuse(childAbuseEnum)
                                                                .behavioursApplicantHelpSoughtWho(childAbuse.getBehavioursApplicantHelpSoughtWho())
                                                                .behavioursApplicantSoughtHelp(childAbuse.getBehavioursApplicantSoughtHelp())
                                                                .behavioursStartDateAndLength(childAbuse.getBehavioursStartDateAndLength())
                                                                .allChildrenAreRisk(getIfAllChildrenAreRisk(
                                                                    childAbuseEnum,
                                                                    allegationOfHarmRevised
                                                                ))
                                                                .whichChildrenAreRisk(whichChildrenAreRisk
                                                                        .map(dynamicMultiSelectList -> dynamicMultiSelectList.getValue()
                                                                                .stream()
                                                                        .map(DynamicMultiselectListElement::getLabel)
                                                                        .collect(Collectors.joining(","))).orElse(null))
                                                                .build()).build();

    }

    public YesOrNo getIfAllChildrenAreRisk(ChildAbuseEnum childAbuseEnum, AllegationOfHarmRevised allegationOfHarmRevised) {
        YesOrNo areAllChildrenAtRisk = YesOrNo.Yes;
        switch (childAbuseEnum.name()) {
            case PHYSICAL_ABUSE:
                areAllChildrenAtRisk = allegationOfHarmRevised.getAllChildrenAreRiskPhysicalAbuse();
                break;
            case PSYCHOLOGICAL_ABUSE:
                areAllChildrenAtRisk = allegationOfHarmRevised.getAllChildrenAreRiskPsychologicalAbuse();
                break;
            case SEXUAL_ABUSE:
                areAllChildrenAtRisk = allegationOfHarmRevised.getAllChildrenAreRiskSexualAbuse();
                break;
            case EMOTIONAL_ABUSE:
                areAllChildrenAtRisk = allegationOfHarmRevised.getAllChildrenAreRiskEmotionalAbuse();
                break;
            case FINANCIAL_ABUSE:
                areAllChildrenAtRisk = allegationOfHarmRevised.getAllChildrenAreRiskFinancialAbuse();
                break;
            default:

        }
        return areAllChildrenAtRisk;
    }

    public DynamicMultiSelectList getWhichChildrenAreInRisk(ChildAbuseEnum childAbuseEnum, AllegationOfHarmRevised allegationOfHarmRevised) {
        DynamicMultiSelectList dynamicMultiSelectList = null;
        if (YesOrNo.No.equals(getIfAllChildrenAreRisk(childAbuseEnum,allegationOfHarmRevised))) {
            switch (childAbuseEnum.name()) {
                case PHYSICAL_ABUSE:
                    dynamicMultiSelectList = allegationOfHarmRevised.getWhichChildrenAreRiskPhysicalAbuse();
                    break;
                case PSYCHOLOGICAL_ABUSE:
                    dynamicMultiSelectList = allegationOfHarmRevised.getWhichChildrenAreRiskPsychologicalAbuse();
                    break;
                case SEXUAL_ABUSE:
                    dynamicMultiSelectList = allegationOfHarmRevised.getWhichChildrenAreRiskSexualAbuse();
                    break;
                case EMOTIONAL_ABUSE:
                    dynamicMultiSelectList = allegationOfHarmRevised.getWhichChildrenAreRiskEmotionalAbuse();
                    break;
                case FINANCIAL_ABUSE:
                    dynamicMultiSelectList = allegationOfHarmRevised.getWhichChildrenAreRiskFinancialAbuse();
                    break;
                default:

            }
        }
        return dynamicMultiSelectList;
    }

    public Map<String, Object> getPrePopulatedChildData(CaseData caseData) {
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        caseData.getNewChildDetails().forEach(eachChild ->
                                                  listItems.add(DynamicMultiselectListElement.builder()
                                                                    .code(eachChild.getId().toString())
                                                                    .label(eachChild.getValue().getFirstName() + " "
                                                                               + eachChild.getValue().getLastName()).build())
        );
        Map<String, Object> caseDataMap = new HashMap<>();

        //Retrieve child list for Physical Abuse
        if (null != caseData.getAllegationOfHarmRevised()
            &&  null != caseData.getAllegationOfHarmRevised().getWhichChildrenAreRiskPhysicalAbuse()) {
            List<DynamicMultiselectListElement> physicalAbuseChildList = new ArrayList<>();

            caseData.getAllegationOfHarmRevised().getWhichChildrenAreRiskPhysicalAbuse().getValue()
                .forEach(eachChild ->
                             physicalAbuseChildList.add(DynamicMultiselectListElement.builder().code(eachChild.getCode())
                                                            .label(eachChild.getLabel()).build()));
            caseDataMap.put("whichChildrenAreRiskPhysicalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .value(physicalAbuseChildList)
                .build());
        } else {
            caseDataMap.put("whichChildrenAreRiskPhysicalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        }

        //Retrieve child list for Psychological Abuse
        if (null != caseData.getAllegationOfHarmRevised()
            && caseData.getAllegationOfHarmRevised().getWhichChildrenAreRiskPsychologicalAbuse() != null) {
            List<DynamicMultiselectListElement> psychologicalAbuseChildList = new ArrayList<>();
            caseData.getAllegationOfHarmRevised().getWhichChildrenAreRiskPsychologicalAbuse().getValue()
                .forEach(eachChild ->
                             psychologicalAbuseChildList.add(DynamicMultiselectListElement.builder().code(eachChild.getCode())
                                                                 .label(eachChild.getLabel()).build()));
            caseDataMap.put("whichChildrenAreRiskPsychologicalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .value(psychologicalAbuseChildList)
                .build());
        } else {
            caseDataMap.put("whichChildrenAreRiskPsychologicalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        }

        //Retrieve child list for Sexual Abuse
        if (null != caseData.getAllegationOfHarmRevised()
            && caseData.getAllegationOfHarmRevised().getWhichChildrenAreRiskSexualAbuse() != null) {
            List<DynamicMultiselectListElement> sexualAbuseChildList = new ArrayList<>();
            caseData.getAllegationOfHarmRevised().getWhichChildrenAreRiskSexualAbuse().getValue()
                .forEach(eachChild ->
                             sexualAbuseChildList.add(DynamicMultiselectListElement.builder().code(eachChild.getCode())
                                                          .label(eachChild.getLabel()).build()));
            caseDataMap.put("whichChildrenAreRiskSexualAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .value(sexualAbuseChildList)
                .build());
        } else {
            caseDataMap.put("whichChildrenAreRiskSexualAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        }

        //Retrieve child list for Emotional Abuse
        if (null != caseData.getAllegationOfHarmRevised()
            && caseData.getAllegationOfHarmRevised().getWhichChildrenAreRiskEmotionalAbuse() != null) {
            List<DynamicMultiselectListElement> emotionalAbuseChildList = new ArrayList<>();
            caseData.getAllegationOfHarmRevised().getWhichChildrenAreRiskEmotionalAbuse().getValue()
                .forEach(eachChild ->
                             emotionalAbuseChildList.add(DynamicMultiselectListElement.builder().code(eachChild.getCode())
                                                             .label(eachChild.getLabel()).build()));
            caseDataMap.put("whichChildrenAreRiskEmotionalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .value(emotionalAbuseChildList)
                .build());
        } else {
            caseDataMap.put("whichChildrenAreRiskEmotionalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        }

        //Retrieve child list for Financial Abuse
        if (null != caseData.getAllegationOfHarmRevised()
            && caseData.getAllegationOfHarmRevised().getWhichChildrenAreRiskFinancialAbuse() != null) {
            List<DynamicMultiselectListElement> financialAbuseChildList = new ArrayList<>();
            caseData.getAllegationOfHarmRevised().getWhichChildrenAreRiskFinancialAbuse().getValue()
                .forEach(eachChild ->
                             financialAbuseChildList.add(DynamicMultiselectListElement.builder().code(eachChild.getCode())
                                                             .label(eachChild.getLabel()).build()));
            caseDataMap.put("whichChildrenAreRiskFinancialAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .value(financialAbuseChildList)
                .build());
        } else {
            caseDataMap.put("whichChildrenAreRiskFinancialAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        }


        return caseDataMap;
    }
}
