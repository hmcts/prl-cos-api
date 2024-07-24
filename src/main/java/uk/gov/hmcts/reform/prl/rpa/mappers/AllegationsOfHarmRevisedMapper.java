package uk.gov.hmcts.reform.prl.rpa.mappers;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.NewPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildPassportDetails;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.services.AllegationOfHarmRevisedService;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonCollectors;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.COMMA_SEPARATOR;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllegationsOfHarmRevisedMapper {
    private final AllegationOfHarmRevisedService allegationOfHarmRevisedService;


    public JsonObject map(CaseData caseData) {


        AllegationOfHarmRevised allegationOfHarmRevised = caseData.getAllegationOfHarmRevised();

        return new NullAwareJsonObjectBuilder()
                .add("newAllegationsOfHarmYesNo", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewAllegationsOfHarmYesNo()))
                .add("newAllegationsOfHarmDomesticAbuseYesNo", CommonUtils.getYesOrNoValue(allegationOfHarmRevised
                        .getNewAllegationsOfHarmDomesticAbuseYesNo()))
                .add("newAllegationsOfHarmChildAbuseYesNo", CommonUtils.getYesOrNoValue(allegationOfHarmRevised
                        .getNewAllegationsOfHarmChildAbuseYesNo()))
                .add("newAllegationsOfHarmChildAbductionYesNo", CommonUtils.getYesOrNoValue(allegationOfHarmRevised
                        .getNewAllegationsOfHarmChildAbductionYesNo()))
                .add("newAllegationsOfHarmSubstanceAbuseYesNo", CommonUtils.getYesOrNoValue(allegationOfHarmRevised
                        .getNewAllegationsOfHarmSubstanceAbuseYesNo()))
                .add("newAllegationsOfHarmSubstanceAbuseDetails", allegationOfHarmRevised.getNewAllegationsOfHarmSubstanceAbuseDetails())
                .add("newAllegationsOfHarmOtherConcerns", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewAllegationsOfHarmOtherConcerns()))
                .add("newAllegationsOfHarmOtherConcernsDetails", allegationOfHarmRevised.getNewAllegationsOfHarmOtherConcernsDetails())
                .add("newOrdersNonMolestation", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewOrdersNonMolestation()))
                .add("newOrdersNonMolestationDateIssued", String.valueOf(allegationOfHarmRevised.getNewOrdersNonMolestationDateIssued()))
                .add("newOrdersNonMolestationEndDate", String.valueOf(allegationOfHarmRevised.getNewOrdersNonMolestationEndDate()))
                .add("newOrdersNonMolestationCurrent", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewOrdersNonMolestationCurrent()))
                .add("newOrdersNonMolestationCourtName", String.valueOf(allegationOfHarmRevised.getNewOrdersNonMolestationCourtName()))
                .add("newOrdersNonMolestationCaseNumber", String.valueOf(allegationOfHarmRevised.getNewOrdersNonMolestationCaseNumber()))
                .add("newOrdersOccupation", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewOrdersOccupation()))
                .add("newOrdersOccupationDateIssued", String.valueOf(allegationOfHarmRevised.getNewOrdersOccupationDateIssued()))
                .add("newOrdersOccupationEndDate", String.valueOf(allegationOfHarmRevised.getNewOrdersOccupationEndDate()))
                .add("newOrdersOccupationCurrent", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewOrdersOccupationCurrent()))
                .add("newOrdersOccupationCourtName", String.valueOf(allegationOfHarmRevised.getNewOrdersOccupationCourtName()))
                .add("newOrdersOccupationCaseNumber", String.valueOf(allegationOfHarmRevised.getNewOrdersOccupationCaseNumber()))
                .add("newOrdersForcedMarriageProtection", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewOrdersForcedMarriageProtection()))
                .add("newOrdersForcedMarriageProtectionDateIssued", String.valueOf(allegationOfHarmRevised
                        .getNewOrdersForcedMarriageProtectionDateIssued()))
                .add("newOrdersForcedMarriageProtectionEndDate", String.valueOf(allegationOfHarmRevised
                        .getNewOrdersForcedMarriageProtectionEndDate()))
                .add("newOrdersForcedMarriageProtectionCurrent", CommonUtils.getYesOrNoValue(allegationOfHarmRevised
                        .getNewOrdersForcedMarriageProtectionCurrent()))
                .add("newOrdersForcedMarriageProtectionCourtName", String.valueOf(allegationOfHarmRevised
                        .getNewOrdersForcedMarriageProtectionCourtName()))
                .add("newOrdersForcedMarriageProtectionCaseNumber", String.valueOf(allegationOfHarmRevised
                        .getNewOrdersForcedMarriageProtectionCaseNumber()))
                .add("newOrdersRestraining", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewOrdersRestraining()))
                .add("newOrdersRestrainingDateIssued", String.valueOf(allegationOfHarmRevised.getNewOrdersRestrainingDateIssued()))
                .add("newOrdersRestrainingEndDate", String.valueOf(allegationOfHarmRevised.getNewOrdersRestrainingEndDate()))
                .add("newOrdersRestrainingCurrent", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewOrdersRestrainingCurrent()))
                .add("newOrdersRestrainingCourtName", String.valueOf(allegationOfHarmRevised.getNewOrdersRestrainingCourtName()))
                .add("newOrdersRestrainingCaseNumber", String.valueOf(allegationOfHarmRevised.getNewOrdersRestrainingCaseNumber()))
                .add("newOrdersOtherInjunctive", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewOrdersOtherInjunctive()))
                .add("newOrdersOtherInjunctiveDateIssued", String.valueOf(allegationOfHarmRevised.getNewOrdersOtherInjunctiveDateIssued()))
                .add("newOrdersOtherInjunctiveEndDate", String.valueOf(allegationOfHarmRevised.getNewOrdersOtherInjunctiveEndDate()))
                .add("newOrdersOtherInjunctiveCurrent", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewOrdersOtherInjunctiveCurrent()))
                .add("newOrdersOtherInjunctiveCourtName", String.valueOf(allegationOfHarmRevised.getNewOrdersOtherInjunctiveCourtName()))
                .add("newOrdersOtherInjunctiveCaseNumber", String.valueOf(allegationOfHarmRevised.getNewOrdersOtherInjunctiveCaseNumber()))
                .add("newOrdersUndertakingInPlace", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewOrdersUndertakingInPlace()))
                .add("newOrdersUndertakingInPlaceDateIssued", String.valueOf(allegationOfHarmRevised.getNewOrdersUndertakingInPlaceDateIssued()))
                .add("newOrdersUndertakingInPlaceEndDate", String.valueOf(allegationOfHarmRevised.getNewOrdersUndertakingInPlaceEndDate()))
                .add("newOrdersUndertakingInPlaceCurrent", CommonUtils.getYesOrNoValue(allegationOfHarmRevised
                        .getNewOrdersUndertakingInPlaceCurrent()))
                .add("newOrdersUndertakingInPlaceCourtName", String.valueOf(allegationOfHarmRevised.getNewOrdersUndertakingInPlaceCourtName()))
                .add("newOrdersUndertakingInPlaceCaseNumber", String.valueOf(allegationOfHarmRevised.getNewOrdersUndertakingInPlaceCaseNumber()))
                .add("domesticBehaviours", mapDomesticAbuseBehaviours(allegationOfHarmRevised.getDomesticBehaviours()))
                .add("childAbuseBehaviours", mapChildAbuseBehaviours(allegationOfHarmRevised))
                .add("newChildAbductionReasons", allegationOfHarmRevised.getNewChildAbductionReasons())
                .add("newPreviousAbductionThreats", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewPreviousAbductionThreats()))
                .add("newPreviousAbductionThreatsDetails", allegationOfHarmRevised.getNewPreviousAbductionThreatsDetails())
                .add("newChildrenLocationNow", allegationOfHarmRevised.getNewChildrenLocationNow())
                .add("newAbductionPassportOfficeNotified", CommonUtils.getYesOrNoValue(allegationOfHarmRevised
                        .getNewAbductionPassportOfficeNotified()))
                .add("newAbductionPreviousPoliceInvolvement", CommonUtils.getYesOrNoValue(allegationOfHarmRevised
                        .getNewAbductionPreviousPoliceInvolvement()))
                .add("newAbductionPreviousPoliceInvolvementDetails", allegationOfHarmRevised.getNewAbductionPreviousPoliceInvolvementDetails())
                .add("newAbductionChildHasPassport", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewAbductionChildHasPassport()))
                .add("childPassportDetails", mapChildPassportDetails(allegationOfHarmRevised.getChildPassportDetails()))
                .add("newAllegationsOfHarmOtherConcernsCourtActions", allegationOfHarmRevised.getNewAllegationsOfHarmOtherConcernsCourtActions())
                .add("newAgreeChildUnsupervisedTime", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewAgreeChildUnsupervisedTime()))
                .add("newAgreeChildSupervisedTime", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewAgreeChildSupervisedTime()))
                .add("newAgreeChildOtherContact", CommonUtils.getYesOrNoValue(allegationOfHarmRevised.getNewAgreeChildOtherContact()))
                .build();
    }

    private JsonObject mapChildPassportDetails(ChildPassportDetails childPassportDetails) {

        if (nonNull(childPassportDetails)) {
            return new NullAwareJsonObjectBuilder()
                    .add("newChildHasMultiplePassports", CommonUtils.getYesOrNoValue(childPassportDetails.getNewChildHasMultiplePassports()))
                    .add("newChildPassportPossessionOtherDetails", childPassportDetails.getNewChildPassportPossessionOtherDetails())
                    .add("newChildPassportPossession", childPassportDetails.getNewChildPassportPossession() != null ? childPassportDetails
                            .getNewChildPassportPossession().stream()
                            .map(NewPassportPossessionEnum::getDisplayedValue)
                            .collect(Collectors.joining(COMMA_SEPARATOR)) : null).build();
        }
        return JsonValue.EMPTY_JSON_OBJECT;
    }

    private JsonArray mapDomesticAbuseBehaviours(List<Element<DomesticAbuseBehaviours>> domesticBehaviours) {

        Optional<List<Element<DomesticAbuseBehaviours>>> domesticBehavioursElementsCheck = ofNullable(domesticBehaviours);
        if (domesticBehavioursElementsCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<DomesticAbuseBehaviours> domesticBehavioursList = domesticBehaviours.stream()
                .map(Element::getValue)
                .toList();
        return domesticBehavioursList.stream().map(domesticBehaviour -> new NullAwareJsonObjectBuilder()
                .add("typeOfAbuse", domesticBehaviour.getTypeOfAbuse().getDisplayedValue())
                .add("newAbuseNatureDescription", domesticBehaviour.getNewAbuseNatureDescription())
                .add("newBehavioursStartDateAndLength", domesticBehaviour.getNewBehavioursStartDateAndLength())
                .add("newBehavioursApplicantSoughtHelp", CommonUtils.getYesOrNoValue(domesticBehaviour.getNewBehavioursApplicantSoughtHelp()))
                .add("newBehavioursApplicantHelpSoughtWho", domesticBehaviour.getNewBehavioursApplicantHelpSoughtWho())
                .build()).collect(JsonCollectors.toJsonArray());
    }


    private JsonArray mapChildAbuseBehaviours(AllegationOfHarmRevised allegationOfHarmRevised) {


        Optional<ChildAbuse> childPhysicalAbuse =
                ofNullable(allegationOfHarmRevised.getChildPhysicalAbuse());


        Optional<ChildAbuse> childPsychologicalAbuse =
                ofNullable(allegationOfHarmRevised.getChildPsychologicalAbuse());


        Optional<ChildAbuse> childEmotionalAbuse =
                ofNullable(allegationOfHarmRevised.getChildEmotionalAbuse());


        Optional<ChildAbuse> childSexualAbuse =
                ofNullable(allegationOfHarmRevised.getChildSexualAbuse());


        Optional<ChildAbuse> childFinancialAbuse =
                ofNullable(allegationOfHarmRevised.getChildFinancialAbuse());


        if (childPhysicalAbuse.isEmpty() && childFinancialAbuse.isEmpty()
                && childSexualAbuse.isEmpty() && childEmotionalAbuse.isEmpty()
                && childPsychologicalAbuse.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<ChildAbuse> childAbuseBehavioursList = new ArrayList<>();

        childPhysicalAbuse.ifPresent(abuse -> {
            if (Objects.nonNull(abuse.getTypeOfAbuse())) {
                childAbuseBehavioursList.add(abuse);
                }
            }
        );

        childPsychologicalAbuse.ifPresent(abuse -> {
            if (Objects.nonNull(abuse.getTypeOfAbuse())) {
                childAbuseBehavioursList.add(abuse);
                }
            }
        );

        childSexualAbuse.ifPresent(abuse -> {
            if (Objects.nonNull(abuse.getTypeOfAbuse())) {
                childAbuseBehavioursList.add(abuse);
                }
            }
        );

        childEmotionalAbuse.ifPresent(abuse -> {
            if (Objects.nonNull(abuse.getTypeOfAbuse())) {
                childAbuseBehavioursList.add(abuse);
                }
            }
        );

        childFinancialAbuse.ifPresent(abuse -> {
            if (Objects.nonNull(abuse.getTypeOfAbuse())) {
                    childAbuseBehavioursList.add(abuse);
                }
            }
        );
        return childAbuseBehavioursList.stream().map(childAbuseBehaviour -> {
            Optional<DynamicMultiSelectList> whichChildrenAreRisk = ofNullable(
                    allegationOfHarmRevisedService.getWhichChildrenAreInRisk(childAbuseBehaviour.getTypeOfAbuse(), allegationOfHarmRevised));
            return new NullAwareJsonObjectBuilder()
                    .add("abuseNatureDescription", childAbuseBehaviour.getAbuseNatureDescription())
                    .add("behavioursStartDateAndLength", childAbuseBehaviour.getBehavioursStartDateAndLength())
                    .add("behavioursApplicantSoughtHelp", CommonUtils.getYesOrNoValue(childAbuseBehaviour.getBehavioursApplicantSoughtHelp()))
                    .add("behavioursApplicantHelpSoughtWho", childAbuseBehaviour.getBehavioursApplicantHelpSoughtWho())
                    .add("allChildrenAreRisk", CommonUtils.getYesOrNoValue(allegationOfHarmRevisedService
                            .getIfAllChildrenAreRisk(
                                    childAbuseBehaviour.getTypeOfAbuse(), allegationOfHarmRevised)))
                    .add("whichChildrenAreRisk", whichChildrenAreRisk.map(dynamicMultiSelectList -> dynamicMultiSelectList
                            .getValue().stream()
                            .map(DynamicMultiselectListElement::getLabel)
                            .collect(Collectors.joining(","))).orElse(""))
                    .build();
        }).collect(JsonCollectors.toJsonArray());

    }

}
