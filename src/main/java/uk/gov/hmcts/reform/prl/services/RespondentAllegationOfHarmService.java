package uk.gov.hmcts.reform.prl.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.RespChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespChildAbuseBehaviour;

import java.util.ArrayList;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;

@Slf4j
@Service
public class RespondentAllegationOfHarmService {


    public List<Element<RespChildAbuseBehaviour>> updateChildAbusesForDocmosis(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        List<Element<RespChildAbuseBehaviour>> childAbuseBehaviourList = new ArrayList<>();
        Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarm = Optional.ofNullable(respondentAllegationsOfHarmData);
        if (respondentAllegationsOfHarm.isPresent() && YesOrNo.Yes.equals(respondentAllegationsOfHarm.get().getRespAohChildAbuseYesNo())) {
            Optional<RespChildAbuse> childPhysicalAbuse =
                    ofNullable(respondentAllegationsOfHarm.get().getRespChildPhysicalAbuse());

            Optional<RespChildAbuse> childPsychologicalAbuse =
                    ofNullable(respondentAllegationsOfHarm.get().getRespChildPsychologicalAbuse());


            Optional<RespChildAbuse> childEmotionalAbuse =
                    ofNullable(respondentAllegationsOfHarm.get().getRespChildEmotionalAbuse());


            Optional<RespChildAbuse> childSexualAbuse =
                    ofNullable(respondentAllegationsOfHarm.get().getRespChildSexualAbuse());


            Optional<RespChildAbuse> childFinancialAbuse =
                    ofNullable(respondentAllegationsOfHarm.get().getRespChildFinancialAbuse());



            for (ChildAbuseEnum eachBehavior : respondentAllegationsOfHarm.get().getRespChildAbuses()) {

                switch (eachBehavior.name()) {
                    case PHYSICAL_ABUSE : childPhysicalAbuse.ifPresent(abuse ->
                            checkAndAddChildAbuse(respondentAllegationsOfHarm.get(), childAbuseBehaviourList, eachBehavior, abuse)

                    );
                        break;
                    case PSYCHOLOGICAL_ABUSE : childPsychologicalAbuse.ifPresent(abuse ->
                            checkAndAddChildAbuse(respondentAllegationsOfHarm.get(), childAbuseBehaviourList, eachBehavior, abuse)

                    );
                        break;
                    case SEXUAL_ABUSE : childSexualAbuse.ifPresent(abuse ->
                            checkAndAddChildAbuse(respondentAllegationsOfHarm.get(), childAbuseBehaviourList, eachBehavior, abuse)

                    );
                        break;
                    case EMOTIONAL_ABUSE : childEmotionalAbuse.ifPresent(abuse ->
                            checkAndAddChildAbuse(respondentAllegationsOfHarm.get(), childAbuseBehaviourList, eachBehavior, abuse)

                    );
                        break;
                    case FINANCIAL_ABUSE : childFinancialAbuse.ifPresent(abuse ->
                            checkAndAddChildAbuse(respondentAllegationsOfHarm.get(), childAbuseBehaviourList, eachBehavior, abuse)

                    );
                        break;
                    default : {
                        //
                    }
                }
            }
            respondentAllegationsOfHarmData.toBuilder().respChildAbuseBehavioursDocmosis(childAbuseBehaviourList);

        }

        return childAbuseBehaviourList;
    }

    private void checkAndAddChildAbuse(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData,
                                       List<Element<RespChildAbuseBehaviour>> childAbuseBehaviourList,
                                       ChildAbuseEnum eachBehavior, RespChildAbuse abuse) {
        if (Objects.nonNull(abuse.getRespAbuseNatureDescription())
                || Objects.nonNull(abuse.getRespBehavioursApplicantSoughtHelp())
                || Objects.nonNull(abuse.getRespBehavioursStartDateAndLength())) {
            childAbuseBehaviourList.add(getChildBehaviour(
                    respondentAllegationsOfHarmData,
                    abuse,
                    eachBehavior
            ));
        }
    }

    private Element<RespChildAbuseBehaviour> getChildBehaviour(RespondentAllegationsOfHarmData allegationOfHarmRevised, RespChildAbuse childAbuse,
                                                               ChildAbuseEnum childAbuseEnum) {

        Optional<DynamicMultiSelectList> whichChildrenAreRisk = ofNullable(
                getWhichChildrenAreInRisk(childAbuseEnum, allegationOfHarmRevised));
        return Element.<RespChildAbuseBehaviour>builder().value(RespChildAbuseBehaviour.builder()
                .abuseNatureDescription(childAbuse.getRespAbuseNatureDescription())
                .typeOfAbuse(childAbuseEnum.getDisplayedValue())
                .behavioursApplicantHelpSoughtWho(childAbuse.getRespBehavioursApplicantHelpSoughtWho())
                .behavioursApplicantSoughtHelp(childAbuse.getRespBehavioursApplicantSoughtHelp())
                .behavioursStartDateAndLength(childAbuse.getRespBehavioursStartDateAndLength())
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


    public YesOrNo getIfAllChildrenAreRisk(ChildAbuseEnum childAbuseEnum, RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        YesOrNo areAllChildrenAtRisk = YesOrNo.Yes;
        switch (childAbuseEnum.name()) {
            case PHYSICAL_ABUSE:
                areAllChildrenAtRisk = respondentAllegationsOfHarmData.getRespAllChildrenAreRiskPhysicalAbuse();
                break;
            case PSYCHOLOGICAL_ABUSE:
                areAllChildrenAtRisk = respondentAllegationsOfHarmData.getRespAllChildrenAreRiskPsychologicalAbuse();
                break;
            case SEXUAL_ABUSE:
                areAllChildrenAtRisk = respondentAllegationsOfHarmData.getRespAllChildrenAreRiskSexualAbuse();
                break;
            case EMOTIONAL_ABUSE:
                areAllChildrenAtRisk = respondentAllegationsOfHarmData.getRespAllChildrenAreRiskEmotionalAbuse();
                break;
            case FINANCIAL_ABUSE:
                areAllChildrenAtRisk = respondentAllegationsOfHarmData.getRespAllChildrenAreRiskFinancialAbuse();
                break;
            default:

        }
        return areAllChildrenAtRisk;
    }

    public DynamicMultiSelectList getWhichChildrenAreInRisk(ChildAbuseEnum childAbuseEnum,
                                                            RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        DynamicMultiSelectList dynamicMultiSelectList = null;
        if (YesOrNo.No.equals(getIfAllChildrenAreRisk(childAbuseEnum,respondentAllegationsOfHarmData))) {
            switch (childAbuseEnum.name()) {
                case PHYSICAL_ABUSE:
                    dynamicMultiSelectList = respondentAllegationsOfHarmData.getRespWhichChildrenAreRiskPhysicalAbuse();
                    break;
                case PSYCHOLOGICAL_ABUSE:
                    dynamicMultiSelectList = respondentAllegationsOfHarmData.getRespWhichChildrenAreRiskPsychologicalAbuse();
                    break;
                case SEXUAL_ABUSE:
                    dynamicMultiSelectList = respondentAllegationsOfHarmData.getRespWhichChildrenAreRiskSexualAbuse();
                    break;
                case EMOTIONAL_ABUSE:
                    dynamicMultiSelectList = respondentAllegationsOfHarmData.getRespWhichChildrenAreRiskEmotionalAbuse();
                    break;
                case FINANCIAL_ABUSE:
                    dynamicMultiSelectList = respondentAllegationsOfHarmData.getRespWhichChildrenAreRiskFinancialAbuse();
                    break;
                default:

            }
        }
        return dynamicMultiSelectList;
    }

    public void prePopulatedChildData(CaseData caseData, Map<String, Object> caseDataMap,
                                      RespondentAllegationsOfHarmData solicitorRepresentedRespondentAllegationsOfHarmData) {
        List<DynamicMultiselectListElement> listItems = getChildrenDynamicList(caseData);

        //Retrieve child list for Physical Abuse
        if (null != solicitorRepresentedRespondentAllegationsOfHarmData
            &&  null != solicitorRepresentedRespondentAllegationsOfHarmData
                .getRespWhichChildrenAreRiskPhysicalAbuse()) {
            List<DynamicMultiselectListElement> physicalAbuseChildList = new ArrayList<>();

            solicitorRepresentedRespondentAllegationsOfHarmData
                    .getRespWhichChildrenAreRiskPhysicalAbuse().getValue()
                .forEach(eachChild ->
                             physicalAbuseChildList.add(DynamicMultiselectListElement.builder().code(eachChild.getCode())
                                                            .label(eachChild.getLabel()).build()));
            caseDataMap.put("respWhichChildrenAreRiskPhysicalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .value(physicalAbuseChildList)
                .build());
        } else {
            caseDataMap.put("respWhichChildrenAreRiskPhysicalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        }

        //Retrieve child list for Psychological Abuse
        if (null != solicitorRepresentedRespondentAllegationsOfHarmData
            && solicitorRepresentedRespondentAllegationsOfHarmData.getRespWhichChildrenAreRiskPsychologicalAbuse() != null) {
            List<DynamicMultiselectListElement> psychologicalAbuseChildList = new ArrayList<>();
            solicitorRepresentedRespondentAllegationsOfHarmData.getRespWhichChildrenAreRiskPsychologicalAbuse().getValue()
                .forEach(eachChild ->
                             psychologicalAbuseChildList.add(DynamicMultiselectListElement.builder().code(eachChild.getCode())
                                                                 .label(eachChild.getLabel()).build()));
            caseDataMap.put("respWhichChildrenAreRiskPsychologicalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .value(psychologicalAbuseChildList)
                .build());
        } else {
            caseDataMap.put("respWhichChildrenAreRiskPsychologicalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        }

        //Retrieve child list for Sexual Abuse
        if (null != solicitorRepresentedRespondentAllegationsOfHarmData
            && solicitorRepresentedRespondentAllegationsOfHarmData.getRespWhichChildrenAreRiskSexualAbuse() != null) {
            List<DynamicMultiselectListElement> sexualAbuseChildList = new ArrayList<>();
            solicitorRepresentedRespondentAllegationsOfHarmData.getRespWhichChildrenAreRiskSexualAbuse().getValue()
                .forEach(eachChild ->
                             sexualAbuseChildList.add(DynamicMultiselectListElement.builder().code(eachChild.getCode())
                                                          .label(eachChild.getLabel()).build()));
            caseDataMap.put("respWhichChildrenAreRiskSexualAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .value(sexualAbuseChildList)
                .build());
        } else {
            caseDataMap.put("respWhichChildrenAreRiskSexualAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        }

        //Retrieve child list for Emotional Abuse
        if (null != solicitorRepresentedRespondentAllegationsOfHarmData
            && solicitorRepresentedRespondentAllegationsOfHarmData.getRespWhichChildrenAreRiskEmotionalAbuse() != null) {
            List<DynamicMultiselectListElement> emotionalAbuseChildList = new ArrayList<>();
            solicitorRepresentedRespondentAllegationsOfHarmData.getRespWhichChildrenAreRiskEmotionalAbuse().getValue()
                .forEach(eachChild ->
                             emotionalAbuseChildList.add(DynamicMultiselectListElement.builder().code(eachChild.getCode())
                                                             .label(eachChild.getLabel()).build()));
            caseDataMap.put("respWhichChildrenAreRiskEmotionalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .value(emotionalAbuseChildList)
                .build());
        } else {
            caseDataMap.put("respWhichChildrenAreRiskEmotionalAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        }

        //Retrieve child list for Financial Abuse
        if (null != solicitorRepresentedRespondentAllegationsOfHarmData
            && solicitorRepresentedRespondentAllegationsOfHarmData.getRespWhichChildrenAreRiskFinancialAbuse() != null) {
            List<DynamicMultiselectListElement> financialAbuseChildList = new ArrayList<>();
            solicitorRepresentedRespondentAllegationsOfHarmData.getRespWhichChildrenAreRiskFinancialAbuse().getValue()
                .forEach(eachChild ->
                             financialAbuseChildList.add(DynamicMultiselectListElement.builder().code(eachChild.getCode())
                                                             .label(eachChild.getLabel()).build()));
            caseDataMap.put("respWhichChildrenAreRiskFinancialAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .value(financialAbuseChildList)
                .build());
        } else {
            caseDataMap.put("respWhichChildrenAreRiskFinancialAbuse", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build());
        }

    }

    private static List<DynamicMultiselectListElement> getChildrenDynamicList(CaseData caseData) {
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            caseData.getNewChildDetails().forEach(eachChild ->
                    listItems.add(DynamicMultiselectListElement.builder()
                            .code(eachChild.getId().toString())
                            .label(eachChild.getValue().getFirstName() + " "
                                    + eachChild.getValue().getLastName()).build())
            );
        } else {
            caseData.getChildren().forEach(eachChild ->
                    listItems.add(DynamicMultiselectListElement.builder()
                            .code(eachChild.getId().toString())
                            .label(eachChild.getValue().getFirstName() + " "
                                    + eachChild.getValue().getLastName()).build())
            );
        }
        return listItems;
    }
}
