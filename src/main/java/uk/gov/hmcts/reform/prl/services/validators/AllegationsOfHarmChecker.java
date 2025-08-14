package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum.other;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllegationsOfHarmChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished = validateFields(caseData);

        if (finished) {
            taskErrorService.removeError(ALLEGATIONS_OF_HARM_ERROR);
            return true;
        }
        taskErrorService.addEventError(ALLEGATIONS_OF_HARM, ALLEGATIONS_OF_HARM_ERROR,
                                       ALLEGATIONS_OF_HARM_ERROR.getError()
        );
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        Optional<YesOrNo> allegationsOfHarm = ofNullable(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo());

        return allegationsOfHarm.isPresent() && allegationsOfHarm.get().equals(Yes);
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }


    public boolean validateFields(CaseData caseData) {
        Optional<YesOrNo> allegationsOfHarmYesNo = ofNullable(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo());

        boolean isFinished;
        if (allegationsOfHarmYesNo.isPresent() && allegationsOfHarmYesNo.get().equals(Yes)) {
            boolean behavioursCompleted = true;

            if (abusePresent(caseData)) {
                Optional<List<Element<Behaviours>>> behavioursWrapped = ofNullable(caseData.getAllegationOfHarm().getBehaviours());
                if (behavioursWrapped.isPresent()
                    && !behavioursWrapped.get().isEmpty()) {
                    behavioursCompleted =  behavioursWrapped.get()
                            .stream().allMatch(behavioursElement -> validateBehaviour(behavioursElement.getValue()));
                    if (!behavioursCompleted) {
                        return false;
                    }
                }
            }



            Optional<YesOrNo> ordersNonMolestation = ofNullable(caseData.getAllegationOfHarm().getOrdersNonMolestation());
            Optional<YesOrNo> ordersOccupation = ofNullable(caseData.getAllegationOfHarm().getOrdersOccupation());
            Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(caseData.getAllegationOfHarm().getOrdersForcedMarriageProtection());
            Optional<YesOrNo> ordersRestraining = ofNullable(caseData.getAllegationOfHarm().getOrdersRestraining());
            Optional<YesOrNo> ordersOtherInjunctive = ofNullable(caseData.getAllegationOfHarm().getOrdersOtherInjunctive());
            Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(caseData.getAllegationOfHarm().getOrdersUndertakingInPlace());

            boolean previousOrders = isPreviousOrdersFinished(
                ordersNonMolestation,
                ordersOccupation,
                ordersForcedMarriageProtection,
                ordersRestraining,
                ordersOtherInjunctive,
                ordersUndertakingInPlace
            );

            isFinished = isSectionsFinished(caseData, behavioursCompleted, previousOrders);
        } else {
            isFinished = allegationsOfHarmYesNo.isPresent();
        }

        return isFinished;
    }

    public boolean isPreviousOrdersFinished(Optional<YesOrNo> ordersNonMolestation,
                                            Optional<YesOrNo> ordersOccupation,
                                            Optional<YesOrNo> ordersForcedMarriageProtection,
                                            Optional<YesOrNo> ordersRestraining,
                                            Optional<YesOrNo> ordersOtherInjunctive,
                                            Optional<YesOrNo> ordersUndertakingInPlace) {
        return ordersNonMolestation.isPresent()
            && ordersOccupation.isPresent()
            && ordersForcedMarriageProtection.isPresent()
            && ordersRestraining.isPresent()
            && ordersOtherInjunctive.isPresent()
            && ordersUndertakingInPlace.isPresent();
    }

    public boolean isSectionsFinished(CaseData caseData, boolean behavioursCompleted, boolean previousOrders) {
        boolean isFinished;
        isFinished = validateDomesticAbuseSection(caseData)
            && validateOrders(caseData)
            && previousOrders
            && behavioursCompleted
            && validateAbductionSection(caseData)
            && validateOtherConcerns(caseData)
            && validateChildContact(caseData);
        return isFinished;
    }

    public boolean abusePresent(CaseData caseData) {
        Optional<YesOrNo> domesticAbuse = ofNullable(caseData.getAllegationOfHarm().getAllegationsOfHarmDomesticAbuseYesNo());
        Optional<YesOrNo> childAbduction = ofNullable(caseData.getAllegationOfHarm().getAllegationsOfHarmChildAbductionYesNo());
        Optional<YesOrNo> childAbuse = ofNullable(caseData.getAllegationOfHarm().getAllegationsOfHarmChildAbuseYesNo());
        Optional<YesOrNo> substanceAbuse = ofNullable(caseData.getAllegationOfHarm().getAllegationsOfHarmSubstanceAbuseYesNo());
        Optional<YesOrNo> otherAbuse = ofNullable(caseData.getAllegationOfHarm().getAllegationsOfHarmOtherConcernsYesNo());

        return anyNonEmpty(
            domesticAbuse,
            childAbduction,
            childAbuse,
            substanceAbuse,
            otherAbuse
        );
    }


    public boolean validateDomesticAbuseSection(CaseData caseData) {
        Optional<YesOrNo> domesticAbuse = ofNullable(caseData.getAllegationOfHarm().getAllegationsOfHarmDomesticAbuseYesNo());
        Optional<List<ApplicantOrChildren>> physicalAbuseVictim = ofNullable(caseData.getAllegationOfHarm().getPhysicalAbuseVictim());
        Optional<List<ApplicantOrChildren>> emotionalAbuseVictim = ofNullable(caseData.getAllegationOfHarm().getEmotionalAbuseVictim());
        Optional<List<ApplicantOrChildren>> psychologicalAbuseVictim = ofNullable(caseData.getAllegationOfHarm().getPsychologicalAbuseVictim());
        Optional<List<ApplicantOrChildren>> sexualAbuseVictim = ofNullable(caseData.getAllegationOfHarm().getSexualAbuseVictim());
        Optional<List<ApplicantOrChildren>> financialAbuseVictim = ofNullable(caseData.getAllegationOfHarm().getPhysicalAbuseVictim());

        List<ApplicantOrChildren> emptyList = Collections.emptyList();

        boolean abuseVictimCompleted = true;
        boolean behaviourRequired = true;

        if (domesticAbuse.isPresent() && domesticAbuse.get().equals(Yes)) {
            abuseVictimCompleted = physicalAbuseVictim.isPresent() && !(physicalAbuseVictim.get().equals(emptyList))
                || emotionalAbuseVictim.isPresent() && !(emotionalAbuseVictim.get().equals(emptyList))
                || psychologicalAbuseVictim.isPresent() && !(psychologicalAbuseVictim.get().equals(emptyList))
                || sexualAbuseVictim.isPresent() && !(sexualAbuseVictim.get().equals(emptyList))
                || financialAbuseVictim.isPresent() && !(financialAbuseVictim.get().equals(emptyList));

            Optional<List<Element<Behaviours>>> behavioursWrapped = ofNullable(caseData.getAllegationOfHarm().getBehaviours());

            behaviourRequired = !behavioursWrapped.isEmpty()
                && (!behavioursWrapped.get().isEmpty());

        }
        return abuseVictimCompleted && behaviourRequired;
    }


    public boolean validateAbductionSection(CaseData caseData) {

        Optional<YesOrNo> childAbduction = ofNullable(caseData.getAllegationOfHarm().getAllegationsOfHarmChildAbductionYesNo());
        Optional<String> childAbductionReasons = ofNullable(caseData.getAllegationOfHarm().getChildAbductionReasons());
        Optional<YesOrNo> previousAbductionThreats = ofNullable(caseData.getAllegationOfHarm().getPreviousAbductionThreats());
        Optional<String> previousAbductionThreatsDetails = ofNullable(caseData.getAllegationOfHarm().getPreviousAbductionThreatsDetails());
        Optional<YesOrNo> abductionPassportOfficeNotified = ofNullable(caseData.getAllegationOfHarm().getAbductionPassportOfficeNotified());
        Optional<YesOrNo> abductionChildHasPassport = ofNullable(caseData.getAllegationOfHarm().getAbductionChildHasPassport());
        Optional<AbductionChildPassportPossessionEnum> abductionChildPassportPosession = ofNullable(caseData.getAllegationOfHarm()
                                                                                                        .getAbductionChildPassportPosession());
        Optional<String> abductionChildPassportPosessionOtherDetail = ofNullable(caseData.getAllegationOfHarm()
                                                                                     .getAbductionChildPassportPosessionOtherDetail());
        Optional<YesOrNo> abductionPreviousPoliceInvolvement = ofNullable(caseData.getAllegationOfHarm()
                                                                              .getAbductionPreviousPoliceInvolvement());
        Optional<String> abductionPreviousPoliceInvolvementDetails = ofNullable(caseData.getAllegationOfHarm()
                                                                                    .getAbductionPreviousPoliceInvolvementDetails());

        boolean abductionSectionCompleted;
        boolean previousThreatSectionComplete = false;
        boolean passportCompleted = abductionPassportOfficeNotified.isPresent();
        boolean hasPassportCompleted = abductionChildHasPassport.isPresent();
        boolean passportPossessionCompleted = false;
        boolean policeCompleted = false;

        if (childAbduction.isPresent() && No.equals(childAbduction.get())) {
            return true;
        }

        if (childAbduction.isPresent()) {
            if (Yes.equals(childAbduction.get())) {
                abductionSectionCompleted = childAbductionReasons.isPresent();

                boolean previousAbductionThreatsCompleted = previousAbductionThreats.isPresent();

                previousThreatSectionComplete = isPreviousThreatSectionComplete(
                    previousAbductionThreats,
                    previousAbductionThreatsDetails,
                    previousThreatSectionComplete,
                    previousAbductionThreatsCompleted
                );

                boolean abductionChildPassportPosessionCompleted = abductionChildPassportPosession.isPresent();

                passportPossessionCompleted = isPassportPossessionCompleted(
                    abductionChildPassportPosession,
                    abductionChildPassportPosessionOtherDetail,
                    passportPossessionCompleted,
                    abductionChildPassportPosessionCompleted
                );

                boolean abductionPreviousPoliceInvolvementCompleted = abductionPreviousPoliceInvolvement.isPresent();

                policeCompleted = isPoliceInvolvementCompleted(
                    abductionPreviousPoliceInvolvement,
                    abductionPreviousPoliceInvolvementDetails,
                    policeCompleted,
                    abductionPreviousPoliceInvolvementCompleted
                );

            } else {
                abductionSectionCompleted = true;
            }
            return abductionSectionCompleted
                && previousThreatSectionComplete
                && passportCompleted
                && hasPassportCompleted
                && passportPossessionCompleted
                && policeCompleted;

        } else {
            return false;
        }
    }

    private boolean isPoliceInvolvementCompleted(
        Optional<YesOrNo> abductionPreviousPoliceInvolvement,
        Optional<String> abductionPreviousPoliceInvolvementDetails,
        boolean policeCompleted,
        boolean abductionPreviousPoliceInvolvementCompleted) {

        if (abductionPreviousPoliceInvolvementCompleted) {
            if (!abductionPreviousPoliceInvolvement.isEmpty()
                && Yes.equals(abductionPreviousPoliceInvolvement.get())) {
                policeCompleted = abductionPreviousPoliceInvolvementDetails.isPresent();
            } else {
                policeCompleted = true;
            }
        }
        return policeCompleted;
    }

    private boolean isPassportPossessionCompleted(
        Optional<AbductionChildPassportPossessionEnum> abductionChildPassportPossession,
        Optional<String> abductionChildPassportPossessionOtherDetail,
        boolean passportPossessionCompleted,
        boolean abductionChildPassportPossessionCompleted) {

        if (abductionChildPassportPossessionCompleted) {
            if (!abductionChildPassportPossession.isEmpty()
                && other.equals(abductionChildPassportPossession.get())) {
                passportPossessionCompleted = abductionChildPassportPossessionOtherDetail.isPresent();
            } else {
                passportPossessionCompleted = true;
            }
        }
        return passportPossessionCompleted;
    }

    private boolean isPreviousThreatSectionComplete(
        Optional<YesOrNo> previousAbductionThreats,
        Optional<String> previousAbductionThreatsDetails,
        boolean previousThreatSectionComplete,
        boolean previousAbductionThreatsCompleted) {

        if (previousAbductionThreatsCompleted) {
            if (!previousAbductionThreats.isEmpty()
                && Yes.equals(previousAbductionThreats.get())) {
                previousThreatSectionComplete = previousAbductionThreatsDetails.isPresent();
            } else {
                previousThreatSectionComplete = true;
            }
        }
        return previousThreatSectionComplete;
    }

    public boolean validateBehaviour(Behaviours behaviour) {

        Optional<String> abuseNatureDescription = ofNullable(behaviour.getAbuseNatureDescription());
        Optional<String> behavioursStartDateAndLength = ofNullable(behaviour.getBehavioursStartDateAndLength());
        Optional<String> behavioursNature = ofNullable(behaviour.getBehavioursNature());
        Optional<YesOrNo> behavioursApplicantSoughtHelp = ofNullable(behaviour.getBehavioursApplicantSoughtHelp());
        Optional<String> behavioursApplicantHelpSoughtWho = ofNullable(behaviour.getBehavioursApplicantHelpSoughtWho());
        Optional<String> behavioursApplicantHelpAction = ofNullable(behaviour.getBehavioursApplicantHelpAction());

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(abuseNatureDescription);
        fields.add(behavioursStartDateAndLength);
        fields.add(behavioursNature);
        fields.add(behavioursApplicantSoughtHelp);
        if (behavioursApplicantSoughtHelp.isPresent()
            && behavioursApplicantSoughtHelp.get().equals(Yes)) {
            fields.add(behavioursApplicantHelpSoughtWho);
            fields.add(behavioursApplicantHelpAction);
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent)
            .map(Optional::get).noneMatch(field -> field.equals(""));

    }


    public boolean validateNonMolestationOrder(CaseData caseData) {
        Optional<YesOrNo> ordersNonMolestationCurrent = ofNullable(caseData.getAllegationOfHarm().getOrdersNonMolestationCurrent());
        return ordersNonMolestationCurrent.isPresent();
    }

    public boolean validateOccupationOrder(CaseData caseData) {
        Optional<YesOrNo> ordersOccupationCurrent = ofNullable(caseData.getAllegationOfHarm().getOrdersOccupationCurrent());
        return ordersOccupationCurrent.isPresent();
    }

    public boolean validateForcedMarriageProtectionOrder(CaseData caseData) {
        Optional<YesOrNo> ordersForcedMarriageProtectionCurrent = ofNullable(caseData.getAllegationOfHarm()
                                                                                 .getOrdersForcedMarriageProtectionCurrent());
        return ordersForcedMarriageProtectionCurrent.isPresent();
    }

    public boolean validateRestrainingOrder(CaseData caseData) {
        Optional<YesOrNo> ordersRestrainingCurrent = ofNullable(caseData.getAllegationOfHarm().getOrdersRestrainingCurrent());
        return ordersRestrainingCurrent.isPresent();
    }

    public boolean validateOtherInjunctiveOrder(CaseData caseData) {
        Optional<YesOrNo> ordersOtherInjunctiveCurrent = ofNullable(caseData.getAllegationOfHarm().getOrdersOtherInjunctiveCurrent());
        return ordersOtherInjunctiveCurrent.isPresent();
    }

    public boolean validateUndertakingInPlaceOrder(CaseData caseData) {
        Optional<YesOrNo> ordersUndertakingInPlaceCurrent = ofNullable(caseData.getAllegationOfHarm().getOrdersUndertakingInPlaceCurrent());
        return ordersUndertakingInPlaceCurrent.isPresent();
    }

    public boolean validateOrders(CaseData caseData) {

        boolean nonMolesComplete = true;
        boolean occupationComplete = true;
        boolean forcedMarComplete = true;
        boolean restrainComplete = true;
        boolean otherComplete = true;
        boolean underComplete = true;

        Optional<YesOrNo> ordersNonMolestation = ofNullable(caseData.getAllegationOfHarm().getOrdersNonMolestation());
        if (ordersNonMolestation.isPresent() && ordersNonMolestation.get().equals(Yes)) {
            nonMolesComplete = validateNonMolestationOrder(caseData);
        }
        Optional<YesOrNo> ordersOccupation = ofNullable(caseData.getAllegationOfHarm().getOrdersOccupation());
        if (ordersOccupation.isPresent() && ordersOccupation.get().equals(Yes)) {
            occupationComplete = validateOccupationOrder(caseData);
        }
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(caseData.getAllegationOfHarm().getOrdersForcedMarriageProtection());
        if (ordersForcedMarriageProtection.isPresent() && ordersForcedMarriageProtection.get().equals(Yes)) {
            forcedMarComplete = validateForcedMarriageProtectionOrder(caseData);
        }
        Optional<YesOrNo> ordersRestraining = ofNullable(caseData.getAllegationOfHarm().getOrdersRestraining());
        if (ordersRestraining.isPresent() && ordersRestraining.get().equals(Yes)) {
            restrainComplete = validateRestrainingOrder(caseData);
        }
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(caseData.getAllegationOfHarm().getOrdersOtherInjunctive());
        if (ordersOtherInjunctive.isPresent() && ordersOtherInjunctive.get().equals(Yes)) {
            otherComplete = validateOtherInjunctiveOrder(caseData);
        }
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(caseData.getAllegationOfHarm().getOrdersUndertakingInPlace());
        if (ordersUndertakingInPlace.isPresent() && ordersUndertakingInPlace.get().equals(Yes)) {
            underComplete = validateUndertakingInPlaceOrder(caseData);
        }

        return nonMolesComplete
            && occupationComplete
            && forcedMarComplete
            && restrainComplete
            && otherComplete
            && underComplete;
    }

    public boolean validateOtherConcerns(CaseData caseData) {
        Optional<YesOrNo> allegationsOfHarmOtherConcerns = ofNullable(caseData.getAllegationOfHarm()
                                                                          .getAllegationsOfHarmOtherConcerns());
        Optional<String> allegationsOfHarmOtherConcernsDetails = ofNullable(caseData.getAllegationOfHarm()
                                                                                .getAllegationsOfHarmOtherConcernsDetails());
        Optional<String> allegationsOfHarmOtherConcernsCourtActions = ofNullable(caseData.getAllegationOfHarm()
                .getAllegationsOfHarmOtherConcernsCourtActions());

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(allegationsOfHarmOtherConcerns);
        if (allegationsOfHarmOtherConcerns.isPresent() && allegationsOfHarmOtherConcerns.get().equals(Yes)) {
            fields.add(allegationsOfHarmOtherConcernsDetails);
        }
        fields.add(allegationsOfHarmOtherConcernsCourtActions);

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

    public boolean validateChildContact(CaseData caseData) {

        Optional<YesOrNo> agreeChildUnsupervisedTime = ofNullable(caseData.getAllegationOfHarm().getAgreeChildUnsupervisedTime());
        Optional<YesOrNo> agreeChildSupervisedTime = ofNullable(caseData.getAllegationOfHarm().getAgreeChildSupervisedTime());
        Optional<YesOrNo> agreeChildOtherContact = ofNullable(caseData.getAllegationOfHarm().getAgreeChildOtherContact());


        List<Optional<?>> fields = new ArrayList<>();
        fields.add(agreeChildUnsupervisedTime);
        fields.add(agreeChildSupervisedTime);
        fields.add(agreeChildOtherContact);

        return fields.stream().noneMatch(Optional::isEmpty);

    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }

}
