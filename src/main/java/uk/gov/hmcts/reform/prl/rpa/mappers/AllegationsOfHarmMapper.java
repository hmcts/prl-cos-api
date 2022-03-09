package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonCollectors;

import static java.util.Optional.ofNullable;

@Component
public class AllegationsOfHarmMapper {

    public JsonObject map(CaseData caseData) {
        return new NullAwareJsonObjectBuilder()
            .add("allegationsOfHarmYesNo", CommonUtils.getYesOrNoValue(caseData.getAllegationsOfHarmYesNo()))
            .add(
                "allegationsOfHarmDomesticAbuseYesNo",
                CommonUtils.getYesOrNoValue(caseData.getAllegationsOfHarmChildAbuseYesNo())
            )
            .add(
                "physicalAbuseVictim",
                caseData.getPhysicalAbuseVictim().isEmpty() ? null : caseData.getPhysicalAbuseVictim().stream()
                    .map(ApplicantOrChildren::getDisplayedValue).collect(Collectors.joining(", "))
            )
            .add(
                "emotionalAbuseVictim",
                caseData.getEmotionalAbuseVictim().isEmpty() ? null : caseData.getEmotionalAbuseVictim().stream()
                    .map(ApplicantOrChildren::getDisplayedValue).collect(Collectors.joining(", "))
            )
            .add(
                "financialAbuseVictim",
                caseData.getFinancialAbuseVictim().isEmpty() ? null : caseData.getFinancialAbuseVictim().stream()
                    .map(ApplicantOrChildren::getDisplayedValue).collect(Collectors.joining(", "))
            )
            .add(
                "psychologicalAbuseVictim",
                caseData.getPhysicalAbuseVictim().isEmpty() ? null : caseData.getPhysicalAbuseVictim().stream()
                    .map(ApplicantOrChildren::getDisplayedValue).collect(Collectors.joining(", "))
            )
            .add(
                "sexualAbuseVictim",
                caseData.getSexualAbuseVictim().isEmpty() ? null : caseData.getSexualAbuseVictim().stream()
                    .map(ApplicantOrChildren::getDisplayedValue).collect(Collectors.joining(", "))
            )
            .add(
                "allegationsOfHarmChildAbductionYesNo",
                CommonUtils.getYesOrNoValue(caseData.getAllegationsOfHarmChildAbductionYesNo())
            )
            .add("childAbductionReasons", caseData.getChildAbductionReasons())
            .add("previousAbductionThreats", CommonUtils.getYesOrNoValue(caseData.getPreviousAbductionThreats()))
            .add("previousAbductionThreatsDetails", caseData.getPreviousAbductionThreatsDetails())
            .add(
                "allegationsOfHarmOtherConcernsDetails",
                caseData.getAllegationsOfHarmOtherConcernsDetails()
            )
            .add("allegationsOfHarmOtherConcernsCourtActions",
                 caseData.getAllegationsOfHarmOtherConcernsCourtActions())
            .add("agreeChildUnsupervisedTime",
                 CommonUtils.getYesOrNoValue(caseData.getAgreeChildUnsupervisedTime()))
            .add("agreeChildSupervisedTime", CommonUtils.getYesOrNoValue(caseData.getAgreeChildSupervisedTime()))
            .add("agreeChildOtherContact", CommonUtils.getYesOrNoValue(caseData.getAgreeChildOtherContact()))
            .add("childrenLocationNow", caseData.getChildrenLocationNow())
            .add(
                "abductionPassportOfficeNotified",
                CommonUtils.getYesOrNoValue(caseData.getAbductionPassportOfficeNotified())
            )
            .add("abductionChildHasPassport",
                 CommonUtils.getYesOrNoValue(caseData.getAbductionChildHasPassport()))
            .add("abductionChildPassportPosession",
                 String.valueOf(caseData.getAbductionChildPassportPosession()))
            .add("abductionChildPassportPosessionOtherDetail",
                 caseData.getAbductionChildPassportPosessionOtherDetail())
            .add(
                "abductionPreviousPoliceInvolvement",
                CommonUtils.getYesOrNoValue(caseData.getAbductionPreviousPoliceInvolvement())
            )
            .add("abductionPreviousPoliceInvolvementDetails",
                 caseData.getAbductionPreviousPoliceInvolvementDetails())
            .add(
                "allegationsOfHarmChildAbuseYesNo",
                CommonUtils.getYesOrNoValue(caseData.getAllegationsOfHarmChildAbuseYesNo())
            )
            .add(
                "allegationsOfHarmSubstanceAbuseYesNo",
                CommonUtils.getYesOrNoValue(caseData.getAllegationsOfHarmSubstanceAbuseYesNo())
            )
            .add(
                "allegationsOfHarmOtherConcernsYesNo",
                CommonUtils.getYesOrNoValue(caseData.getAllegationsOfHarmOtherConcernsYesNo())
            )
            .add("ordersNonMolestation", CommonUtils.getYesOrNoValue(caseData.getOrdersNonMolestation()))
            .add("ordersNonMolestationDateIssued", String.valueOf(caseData.getOrdersNonMolestationDateIssued()))
            .add("ordersNonMolestationEndDate", String.valueOf(caseData.getOrdersNonMolestationEndDate()))
            .add("ordersOccupationDateIssued", String.valueOf(caseData.getOrdersOccupationDateIssued()))
            .add("ordersOccupationEndDate", String.valueOf(caseData.getOrdersOccupationEndDate()))
            .add(
                "ordersForcedMarriageProtectionDateIssued",
                String.valueOf(caseData.getOrdersForcedMarriageProtectionDateIssued())
            )
            .add(
                "ordersForcedMarriageProtectionEndDate",
                String.valueOf(caseData.getOrdersForcedMarriageProtectionEndDate())
            )
            .add("ordersRestrainingDateIssued", String.valueOf(caseData.getOrdersRestrainingDateIssued()))
            .add("ordersRestrainingEndDate", String.valueOf(caseData.getOrdersRestrainingEndDate()))
            .add(
                "ordersOtherInjunctiveDateIssued",
                String.valueOf(caseData.getOrdersOtherInjunctiveDateIssued())
            )
            .add("ordersOtherInjunctiveEndDate", String.valueOf(caseData.getOrdersOtherInjunctiveEndDate()))
            .add("ordersUndertakingInPlaceDateIssued",
                 String.valueOf(caseData.getOrdersUndertakingInPlaceDateIssued()))
            .add("ordersUndertakingInPlaceEndDate",
                 String.valueOf(caseData.getOrdersUndertakingInPlaceEndDate()))
            .add("ordersNonMolestationCurrent",
                 CommonUtils.getYesOrNoValue(caseData.getOrdersNonMolestationCurrent()))
            .add("ordersNonMolestationCourtName", caseData.getOrdersNonMolestationCourtName())
            .add("ordersOccupation", CommonUtils.getYesOrNoValue(caseData.getOrdersOccupation()))
            .add("ordersOccupationCurrent", CommonUtils.getYesOrNoValue(caseData.getOrdersOccupationCurrent()))
            .add(
                "ordersOtherInjunctiveCurrent",
                CommonUtils.getYesOrNoValue(caseData.getOrdersOtherInjunctiveCurrent())
            )
            .add(
                "allegationsOfHarmOtherConcerns",
                CommonUtils.getYesOrNoValue(caseData.getAllegationsOfHarmOtherConcerns())
            )
            .add("ordersOccupationCourtName", caseData.getOrdersOccupationCourtName())
            .add("ordersRestrainingCourtName", caseData.getOrdersRestrainingCourtName())
            .add("ordersOtherInjunctiveCourtName", caseData.getOrdersOtherInjunctiveCourtName())
            .add("ordersUndertakingInPlaceCourtName", caseData.getOrdersUndertakingInPlaceCourtName())
            .add(
                "ordersForcedMarriageProtection",
                CommonUtils.getYesOrNoValue(caseData.getOrdersForcedMarriageProtection())
            )
            .add(
                "ordersForcedMarriageProtectionCurrent",
                CommonUtils.getYesOrNoValue(caseData.getOrdersForcedMarriageProtectionCurrent())
            )
            .add("ordersRestrainingCurrent", CommonUtils.getYesOrNoValue(caseData.getOrdersRestrainingCurrent()))
            .add(
                "ordersUndertakingInPlaceCurrent",
                CommonUtils.getYesOrNoValue(caseData.getOrdersUndertakingInPlaceCurrent())
            )
            .add("ordersForcedMarriageProtectionCourtName",
                 caseData.getOrdersForcedMarriageProtectionCourtName())
            .add("ordersRestraining", CommonUtils.getYesOrNoValue(caseData.getOrdersRestraining()))
            .add("ordersOtherInjunctive", CommonUtils.getYesOrNoValue(caseData.getOrdersOtherInjunctive()))
            .add("ordersUndertakingInPlace", CommonUtils.getYesOrNoValue(caseData.getOrdersUndertakingInPlace()))
            .add("behaviours", mapBehaviours(caseData.getBehaviours()))
            .build();
    }

    private JsonArray mapBehaviours(List<Element<Behaviours>> behaviours) {

        Optional<List<Element<Behaviours>>> behavioursElementsCheck = ofNullable(behaviours);
        if (behavioursElementsCheck.isEmpty()) {
            return JsonArray.EMPTY_JSON_ARRAY;
        }
        List<Behaviours> behavioursList = behaviours.stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        return behavioursList.stream().map(behaviour -> new NullAwareJsonObjectBuilder()
            .add("behavioursNature", behaviour.getBehavioursNature())
            .add("abuseNatureDescription", behaviour.getAbuseNatureDescription())
            .add("behavioursStartDateAndLength", behaviour.getBehavioursStartDateAndLength())
            .add("behavioursApplicantHelpAction", behaviour.getBehavioursApplicantHelpAction())
            .add(
                "behavioursApplicantSoughtHelp",
                CommonUtils.getYesOrNoValue(behaviour.getBehavioursApplicantSoughtHelp())
            )
            .add("behavioursApplicantHelpSoughtWho", behaviour.getBehavioursApplicantHelpSoughtWho())
            .build()).collect(JsonCollectors.toJsonArray());
    }
}
