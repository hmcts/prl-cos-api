package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonCollectors;

import static java.util.Optional.ofNullable;

@Component
public class AllegationsOfHarmMapper {

    public JsonObject map(CaseData caseData) {

        String physicalAbuseVictimJson = null;
        String emotionalAbuseVictimJson = null;
        String financialAbuseVictimJson = null;
        String psychologicalAbuseVictimJson = null;
        String sexualAbuseVictimJson = null;

        AllegationOfHarm allegationOfHarm = caseData.getAllegationOfHarm();
        if (allegationOfHarm.getPhysicalAbuseVictim() != null && !allegationOfHarm.getPhysicalAbuseVictim().isEmpty()) {
            physicalAbuseVictimJson = allegationOfHarm.getPhysicalAbuseVictim()
                    .stream()
                    .map(ApplicantOrChildren::getDisplayedValue)
                    .collect(Collectors.joining(", "));
        }

        if (allegationOfHarm.getEmotionalAbuseVictim() != null && !allegationOfHarm.getEmotionalAbuseVictim().isEmpty()) {
            emotionalAbuseVictimJson = allegationOfHarm.getEmotionalAbuseVictim()
                .stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        if (allegationOfHarm.getFinancialAbuseVictim() != null && !allegationOfHarm.getFinancialAbuseVictim().isEmpty()) {
            financialAbuseVictimJson = allegationOfHarm.getFinancialAbuseVictim()
                .stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        if (allegationOfHarm.getPsychologicalAbuseVictim() != null && !allegationOfHarm.getPsychologicalAbuseVictim().isEmpty()) {
            psychologicalAbuseVictimJson = allegationOfHarm.getPhysicalAbuseVictim()
                .stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        if (allegationOfHarm.getSexualAbuseVictim() != null && !allegationOfHarm.getSexualAbuseVictim().isEmpty()) {
            sexualAbuseVictimJson = allegationOfHarm.getSexualAbuseVictim()
                .stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        return new NullAwareJsonObjectBuilder()
            .add("allegationsOfHarmYesNo", CommonUtils.getYesOrNoValue(allegationOfHarm.getAllegationsOfHarmYesNo()))
            .add(
                "allegationsOfHarmDomesticAbuseYesNo",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getAllegationsOfHarmChildAbuseYesNo())
            )
            .add("physicalAbuseVictim", physicalAbuseVictimJson)
            .add("emotionalAbuseVictim", emotionalAbuseVictimJson)
            .add("financialAbuseVictim",  financialAbuseVictimJson)
            .add("psychologicalAbuseVictim", psychologicalAbuseVictimJson)
            .add("sexualAbuseVictim", sexualAbuseVictimJson)
            .add(
                "allegationsOfHarmChildAbductionYesNo",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getAllegationsOfHarmChildAbductionYesNo())
            )
            .add("childAbductionReasons", allegationOfHarm.getChildAbductionReasons())
            .add("previousAbductionThreats", CommonUtils.getYesOrNoValue(allegationOfHarm.getPreviousAbductionThreats()))
            .add("previousAbductionThreatsDetails", allegationOfHarm.getPreviousAbductionThreatsDetails())
            .add(
                "allegationsOfHarmOtherConcernsDetails",
                allegationOfHarm.getAllegationsOfHarmOtherConcernsDetails()
            )
            .add("allegationsOfHarmOtherConcernsCourtActions",
                 allegationOfHarm.getAllegationsOfHarmOtherConcernsCourtActions())
            .add("agreeChildUnsupervisedTime",
                 CommonUtils.getYesOrNoValue(allegationOfHarm.getAgreeChildUnsupervisedTime()))
            .add("agreeChildSupervisedTime", CommonUtils.getYesOrNoValue(allegationOfHarm.getAgreeChildSupervisedTime()))
            .add("agreeChildOtherContact", CommonUtils.getYesOrNoValue(allegationOfHarm.getAgreeChildOtherContact()))
            .add("childrenLocationNow", allegationOfHarm.getChildrenLocationNow())
            .add(
                "abductionPassportOfficeNotified",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getAbductionPassportOfficeNotified())
            )
            .add("abductionChildHasPassport",
                 CommonUtils.getYesOrNoValue(allegationOfHarm.getAbductionChildHasPassport()))
            .add("abductionChildPassportPosession",
                 String.valueOf(allegationOfHarm.getAbductionChildPassportPosession()))
            .add("abductionChildPassportPosessionOtherDetail",
                 allegationOfHarm.getAbductionChildPassportPosessionOtherDetail())
            .add(
                "abductionPreviousPoliceInvolvement",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getAbductionPreviousPoliceInvolvement())
            )
            .add("abductionPreviousPoliceInvolvementDetails",
                 allegationOfHarm.getAbductionPreviousPoliceInvolvementDetails())
            .add(
                "allegationsOfHarmChildAbuseYesNo",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getAllegationsOfHarmChildAbuseYesNo())
            )
            .add(
                "allegationsOfHarmSubstanceAbuseYesNo",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getAllegationsOfHarmSubstanceAbuseYesNo())
            )
            .add(
                "allegationsOfHarmOtherConcernsYesNo",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getAllegationsOfHarmOtherConcernsYesNo())
            )
            .add("ordersNonMolestation", CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersNonMolestation()))
            .add("ordersNonMolestationDateIssued", String.valueOf(allegationOfHarm.getOrdersNonMolestationDateIssued()))
            .add("ordersNonMolestationEndDate", String.valueOf(allegationOfHarm.getOrdersNonMolestationEndDate()))
            .add("ordersOccupationDateIssued", String.valueOf(allegationOfHarm.getOrdersOccupationDateIssued()))
            .add("ordersOccupationEndDate", String.valueOf(allegationOfHarm.getOrdersOccupationEndDate()))
            .add(
                "ordersForcedMarriageProtectionDateIssued",
                String.valueOf(allegationOfHarm.getOrdersForcedMarriageProtectionDateIssued())
            )
            .add(
                "ordersForcedMarriageProtectionEndDate",
                String.valueOf(allegationOfHarm.getOrdersForcedMarriageProtectionEndDate())
            )
            .add("ordersRestrainingDateIssued", String.valueOf(allegationOfHarm.getOrdersRestrainingDateIssued()))
            .add("ordersRestrainingEndDate", String.valueOf(allegationOfHarm.getOrdersRestrainingEndDate()))
            .add(
                "ordersOtherInjunctiveDateIssued",
                String.valueOf(allegationOfHarm.getOrdersOtherInjunctiveDateIssued())
            )
            .add("ordersOtherInjunctiveEndDate", String.valueOf(allegationOfHarm.getOrdersOtherInjunctiveEndDate()))
            .add("ordersUndertakingInPlaceDateIssued",
                 String.valueOf(allegationOfHarm.getOrdersUndertakingInPlaceDateIssued()))
            .add("ordersUndertakingInPlaceEndDate",
                 String.valueOf(allegationOfHarm.getOrdersUndertakingInPlaceEndDate()))
            .add("ordersNonMolestationCurrent",
                 CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersNonMolestationCurrent()))
            .add("ordersNonMolestationCourtName", allegationOfHarm.getOrdersNonMolestationCourtName())
            .add("ordersOccupation", CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersOccupation()))
            .add("ordersOccupationCurrent", CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersOccupationCurrent()))
            .add(
                "ordersOtherInjunctiveCurrent",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersOtherInjunctiveCurrent())
            )
            .add(
                "allegationsOfHarmOtherConcerns",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getAllegationsOfHarmOtherConcerns())
            )
            .add("ordersOccupationCourtName", allegationOfHarm.getOrdersOccupationCourtName())
            .add("ordersRestrainingCourtName", allegationOfHarm.getOrdersRestrainingCourtName())
            .add("ordersOtherInjunctiveCourtName", allegationOfHarm.getOrdersOtherInjunctiveCourtName())
            .add("ordersUndertakingInPlaceCourtName", allegationOfHarm.getOrdersUndertakingInPlaceCourtName())
            .add(
                "ordersForcedMarriageProtection",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersForcedMarriageProtection())
            )
            .add(
                "ordersForcedMarriageProtectionCurrent",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersForcedMarriageProtectionCurrent())
            )
            .add("ordersRestrainingCurrent", CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersRestrainingCurrent()))
            .add(
                "ordersUndertakingInPlaceCurrent",
                CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersUndertakingInPlaceCurrent())
            )
            .add("ordersForcedMarriageProtectionCourtName",
                 allegationOfHarm.getOrdersForcedMarriageProtectionCourtName())
            .add("ordersRestraining", CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersRestraining()))
            .add("ordersOtherInjunctive", CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersOtherInjunctive()))
            .add("ordersUndertakingInPlace", CommonUtils.getYesOrNoValue(allegationOfHarm.getOrdersUndertakingInPlace()))
            .add("behaviours", mapBehaviours(allegationOfHarm.getBehaviours()))
            .build();
    }

    private JsonArray mapBehaviours(List<Element<Behaviours>> behaviours) {

        Optional<List<Element<Behaviours>>> behavioursElementsCheck = ofNullable(behaviours);
        if (behavioursElementsCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<Behaviours> behavioursList = behaviours.stream()
            .map(Element::getValue)
            .toList();
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
