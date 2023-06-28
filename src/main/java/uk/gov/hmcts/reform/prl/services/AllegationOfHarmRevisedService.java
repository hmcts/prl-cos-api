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
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMOTIONAL_ABUSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINANCIAL_ABUSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PHYSICAL_ABUSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PSYCHOLOGICAL_ABUSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SEXUAL_ABUSE;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.COMMA_SEPARATOR;

@Slf4j
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
                    case PHYSICAL_ABUSE:
                        childAbuses.add(getChildBehaviour(
                            allegationOfHarmRevised.get(),
                            allegationOfHarmRevised.get().getChildPhysicalAbuse(),
                            eachBehavior
                        ));
                        break;
                    case PSYCHOLOGICAL_ABUSE:
                        childAbuses.add(getChildBehaviour(
                            allegationOfHarmRevised.get(),
                            allegationOfHarmRevised.get().getChildPsychologicalAbuse(),
                            eachBehavior
                        ));
                        break;
                    case SEXUAL_ABUSE:
                        childAbuses.add(getChildBehaviour(
                            allegationOfHarmRevised.get(),
                            allegationOfHarmRevised.get().getChildSexualAbuse(),
                            eachBehavior
                        ));
                        break;
                    case EMOTIONAL_ABUSE:
                        childAbuses.add(getChildBehaviour(
                            allegationOfHarmRevised.get(),
                            allegationOfHarmRevised.get().getChildEmotionalAbuse(),
                            eachBehavior
                        ));
                        break;
                    case FINANCIAL_ABUSE:
                        childAbuses.add(getChildBehaviour(
                            allegationOfHarmRevised.get(),
                            allegationOfHarmRevised.get().getChildFinancialAbuse(),
                            eachBehavior
                        ));
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
                                                                .allChildrenAreRisk(getIfAllChildrenAreRisk(
                                                                    childAbuseEnum,
                                                                    allegationOfHarmRevised
                                                                ))
                                                                .whichChildrenAreRisk(YesOrNo.No.equals(
                                                                    getIfAllChildrenAreRisk(
                                                                        childAbuseEnum,
                                                                        allegationOfHarmRevised
                                                                    ))
                                                                                          ? (getWhichChildrenAreInRisk(
                                                                    childAbuseEnum,
                                                                    allegationOfHarmRevised
                                                                )).getValue().stream()
                                                                    .map(DynamicMultiselectListElement::getLabel)
                                                                    .collect(Collectors.joining(COMMA_SEPARATOR)) : null)
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
                .forEach(eachChild -> log.info(eachChild.getLabel()));

            caseData.getAllegationOfHarmRevised().getWhichChildrenAreRiskPhysicalAbuse().getListItems()
                .forEach(eachChild ->
                             physicalAbuseChildList.add(DynamicMultiselectListElement.builder().code(eachChild.getCode())
                                                            .label(eachChild.getLabel()).build()));
            caseDataMap.put("whichChildrenAreRiskPhysicalAbuse", DynamicMultiSelectList.builder()
                .listItems(physicalAbuseChildList)
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
                .listItems(psychologicalAbuseChildList)
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
                .listItems(sexualAbuseChildList)
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
                .listItems(emotionalAbuseChildList)
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
                .listItems(financialAbuseChildList)
                .build());
        } else {
            caseDataMap.put("whichChildrenAreRiskFinancialAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        }


        return caseDataMap;
    }
}
